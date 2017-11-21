package utilities;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.types.Node;

import stream_data.GraphDBManager;

public class SentimentSetup {
	
	private static Session session;

	
	
	public static void main(String[] args) {

		extractData("leiene");
		

	}
	
	private static void extractData(String topic){
		GraphDBManager gdbm = new GraphDBManager();
		session = gdbm.getSession();
	//	List<PairUserTweet> userAndTweet= new ArrayList<>();
		String query = "MATCH (topic:Topic)<--(t:Tweet)<--(u:User) WHERE not exists(t.sentimentChecked) and topic.name='"+topic+"' RETURN t,u,id(u),id(t)";
		StatementResult sr = session.run(query);
		Record r;
		Node tweet, user;
		System.out.println("Loading results...");
		while(sr.hasNext()){
			r = sr.next();
			user = r.get("u").asNode();
			tweet = r.get("t").asNode();
			
			long nodeIDTweet = r.get("id(t)").asLong();
			long tweet_id = tweet.get("tweet_id").asLong();
			String created_at = tweet.get("created_at").asString();
			String location = tweet.get("location").asString();
			String text = tweet.get("text").asString();
			String language = tweet.get("language").asString();
			int likecount = tweet.get("likecount").asInt();
			int retweetcount = tweet.get("retweetcount").asInt();
			Tweet t = new Tweet(nodeIDTweet, tweet_id, created_at, location, text, language, likecount, retweetcount);
			
			//Calcolo e assegnazione del sentiment al tweet
			t.computeSentiment();
		
			long nodeIDUser = r.get("id(u)").asLong();
			long userID = user.get("user_id").asLong();
			String name = user.get("name").asString();
			String screen_name = user.get("screen_name").asString();
			String u_location = user.get("location").asString();
			String description = user.get("description").asString();
			String profileImage = user.get("profileImage").asString();
			boolean verified = user.get("verified").asBoolean();
			int followers = user.get("followers").asInt();
			int following = user.get("following").asInt();
			
			User u = new User(nodeIDUser, userID, name, screen_name, u_location, description, profileImage, verified, followers, following);
			
		//	userAndTweet.add(new PairUserTweet(u,t));
			query = 	"MATCH (u:User), (t:Topic) WHERE u.user_id={user_id} and t.name={topic} "
					+ 	"\n CREATE (u)-[r:COMMENTS]->(t) SET r.tweet={tweet_id}, r.sentiment={sentiment}";
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("user_id", u.getUserID());
			parameters.put("topic", topic);
			parameters.put("tweet_id", t.getTweetID());
			parameters.put("sentiment", t.getSentiment());
			session.run(query, parameters);
			
			query = "\n MATCH (t2:Tweet{tweet_id:"+t.getTweetID()+"}) SET t2 += {sentimentChecked:true}";
			session.run(query);
			
			
		}
		System.out.println("Task completed.");
	}
	
	
	



	public static Session getSession() {
		return session;
	}



	public static void setSession(Session session) {
		SentimentSetup.session = session;
	}

}
