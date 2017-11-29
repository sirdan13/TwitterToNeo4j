package utilities;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.types.Node;

import stream_data.GraphDBManager;

public class SentimentSetup {
	
	private static Session session;

	
	
	public static void main(String[] args) {

		GraphDBManager gdbm = new GraphDBManager();
		session = gdbm.getSession();
	//	extractData(JOptionPane.showInputDialog("Inserire topic di interesse per calcolare il sentiment: "));
		while(true)
			extractData();
		

	}
	
	private static void extractData(){
		
		String query = "MATCH (topic:Topic)<--(t:Tweet) WHERE not exists(t.sentiment) RETURN t,id(t)";
		StatementResult sr = session.run(query);
		Record r;
		Node tweet;
		System.out.println("Loading results...");
		int counter = 0;
		while(sr.hasNext()){
			r = sr.next();
			tweet = r.get("t").asNode();
			
			long nodeIDTweet = r.get("id(t)").asLong();
			String text = tweet.get("text").asString();
			Tweet t = new Tweet(nodeIDTweet, text);
			
			//Calcolo e assegnazione del sentiment al tweet
			t.computeSentiment();
			query = "MATCH (t:Tweet) where id(t)={idNode} SET t += {sentiment:{sentiment}}";
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("idNode", t.getNodeID());
			parameters.put("sentiment", t.getSentiment());
			session.run(query, parameters);
			counter++;
			if(counter%100==0)
				System.out.println(counter+" tweets processed.");
			}
		System.out.println("Task completed.");
		System.out.println("Total tweets processed: "+counter);
		System.out.println();
	}
	
	@SuppressWarnings("unused")
	private static void extractData(String topic){
		String query = "MATCH (topic:Topic)<--(t:Tweet) WHERE not exists(t.sentiment) and topic.name='"+topic+"' RETURN t,id(t)";
		StatementResult sr = session.run(query);
		Record r;
		Node tweet;
		System.out.println("Loading results...");
		int counter = 0;
		while(sr.hasNext()){
			r = sr.next();
			tweet = r.get("t").asNode();
			
			long nodeIDTweet = r.get("id(t)").asLong();
			String text = tweet.get("text").asString();
			Tweet t = new Tweet(nodeIDTweet, text);
			
			//Calcolo e assegnazione del sentiment al tweet
			t.computeSentiment();
			query = "MATCH (t:Tweet) where id(t)={idNode} SET t += {sentiment:{sentiment}}";
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("idNode", t.getNodeID());
			parameters.put("sentiment", t.getSentiment());
			session.run(query, parameters);
			counter++;
			if(counter%100==0)
				System.out.println(counter+" tweets processed.");
			}
		System.out.println("Task completed.");
		System.out.println("Total tweet processed: "+counter);
		System.out.println();
	}
	
	
	



	public static Session getSession() {
		return session;
	}



	public static void setSession(Session session) {
		SentimentSetup.session = session;
	}

}
