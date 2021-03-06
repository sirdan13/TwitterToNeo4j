package stream_data;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import javax.swing.JOptionPane;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.exceptions.TransientException;

import twitter4j.FilterQuery;
import twitter4j.HashtagEntity;
import twitter4j.ResponseList;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.UserMentionEntity;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import utilities.Utilities;

public class TwitterManager {

	private static Session session;
	private static ConfigurationBuilder cb;
	private static GraphDBManager gdbm;
	private static String topic;
	private Configuration config;
	private StatusListener listener;
	private static String [] keywords;
	private static List<String> languagesList;
	private static String timeFilter;
	private static String devAccount1;
	private static String devAccount2;
	private static String devAccount3;
	
	public TwitterManager(LinkedBlockingQueue<Status> queue){
		String[] credentials;
		try {
			readSettings();
			credentials = readTwitterAuth();
			cb = new ConfigurationBuilder();
			cb.setDebugEnabled(true).setOAuthConsumerKey(credentials[0]).setOAuthConsumerSecret(credentials[1]).setOAuthAccessToken(credentials[2]).setOAuthAccessTokenSecret(credentials[3]);
			setConfig(cb.build());
			setListener(queue);
			
			
		} 
		catch (FileNotFoundException e) {
			System.out.println("File not found: twitter dev credentials");
			e.printStackTrace();
		}
		
	}
	
	public TwitterManager(){
		gdbm = new GraphDBManager();
		session = gdbm.getSession();
	}
	
	private static void readSettings(){
		boolean success = false;
		String file = "setting.txt";
		languagesList = new ArrayList<>();
		while(!success){
			Scanner sc = null;
			try {
				sc = new Scanner(new File(file));
				success = true;
				while(sc.hasNextLine()){
					
					String line = sc.nextLine();
					if(line.length()>0){
						if(line.startsWith("keywords"))
							keywords=line.split("=")[1].split(",");
						if(line.startsWith("all") || line.startsWith("en") || line.startsWith("it") || line.startsWith("es") || line.startsWith("fr") || line.startsWith("pt") || line.startsWith("de") || line.startsWith("zh") || line.startsWith("tr") || line.startsWith("in") || line.startsWith("ja"))
							if(line.substring(3, line.length()).equals("true"))
								languagesList.add(line.substring(0, 2));
						
						if(line.startsWith("topic"))
							topic=line.split("=")[1];
						if(line.startsWith("timeFilter"))
							setTimeFilter(line.split("=")[1]);
						if(line.startsWith("devAccount1"))
							devAccount1=line.substring(12, line.length());
						if(line.startsWith("devAccount2"))
							devAccount2=line.substring(12, line.length());
						if(line.startsWith("devAccount3"))
							devAccount3=line.substring(12, line.length());
				
			}
				}		
				sc.close();
			} catch (FileNotFoundException e1) {
				file = JOptionPane.showInputDialog(null, "File not found. Please type its location below (press 0 to exit):");
				if(file.equals("0"))
					System.exit(0);
			}
			
			
			
		}
		
	}
	
	
	public static boolean checkTime(java.util.Date startDate, Status status, boolean checkTime){
		if(!checkTime)
			return true;
		else{
			if(status.isRetweet())
			status = status.getRetweetedStatus();
		if(startDate.before(status.getCreatedAt()))
			return true;
		return false;
		}
		
	}
	
	
	public static ResponseList<User> lookupUsers(long id){
		ConfigurationBuilder cb = getConfigurationBuilder();
		Twitter twitter = new TwitterFactory(cb.build()).getInstance();
        try {
			return twitter.lookupUsers(id);
		} catch (TwitterException e) {
			System.out.println("Lookup failed");
			if(e.exceededRateLimitation())
				System.out.println(" due to exceeded rate limitation.");
			if(e.getErrorCode()==17)
			System.out.println("user(s) not found.");
			System.exit(-1);
		}
		return null;
		
	}
	
	public static ResponseList<User> lookupUsers(String screen_names){
		String[] auth = null;
		String file = "config/credenziali_twitter3.txt";
		auth = TwitterManager.readTwitterAuth(file);
		
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true).setOAuthConsumerKey(auth[0]).setOAuthConsumerSecret(auth[1])
				.setOAuthAccessToken(auth[2]).setOAuthAccessTokenSecret(auth[3]);
        Twitter twitter = new TwitterFactory(cb.build()).getInstance();
        try {
			return twitter.lookupUsers(screen_names);
		} catch (TwitterException e) {
			System.out.print("Lookup failed");
			if(e.exceededRateLimitation())
				System.out.println(" due to exceeded rate limitation.");
			if(e.getErrorCode()==17)
			System.out.println(": user(s) not found.");
		}
		return null;
		
	}
	
	private static List<long []> moreThan100Elements(long[] ids){
		int nUsersInt = ids.length;
		double nArrays = nUsersInt/100;	//<-----number of arrays which will have 100 users
		List<long []> arrays = new ArrayList<long []>();
		for(int i = 0;i<nArrays;i++){
			int start = i*100;
			int end = (i*100)+100;
			arrays.add(Arrays.copyOfRange(ids, start, end));
		}
		arrays.add(Arrays.copyOfRange(ids, (int) (nArrays*100), ids.length));
		return arrays;
	}
	
	
	private static ConfigurationBuilder getConfigurationBuilder(){
	String[] auth = null;
	int randomNum = ThreadLocalRandom.current().nextInt(2, 4 + 1);
	auth = TwitterManager.readTwitterAuth("config/credenziali_twitter"+randomNum+".txt");
	ConfigurationBuilder cb = new ConfigurationBuilder();
	cb.setDebugEnabled(true).setOAuthConsumerKey(auth[0]).setOAuthConsumerSecret(auth[1])
			.setOAuthAccessToken(auth[2]).setOAuthAccessTokenSecret(auth[3]);
	return cb;
}
	
	
public static long[] extractTweets(GraphDBManager gdbm) {
		Session session = gdbm.getSession();
		String query = "MATCH (t:Tweet) WHERE NOT EXISTS(t.language) RETURN t.tweet_id as tweet_id";
		StatementResult sr;
		try{
			sr = session.run(query);
			List<Long> ids = new ArrayList<Long>();
			for(Record r : sr.list()){
				long id = r.get(0).asLong();
				ids.add(id);
			}
			if(ids.size()==0){
				System.out.println();
				System.out.println("No tweet matched.");
			}
			else
				System.out.println("Matched tweet: "+ids.size());
			
				
			long[] result = ids.stream().mapToLong(l -> l).toArray();
			return result;
		}
		catch(TransientException e){
			System.out.println("TransientException: couldn't run the query succesfully. Transaction canceled.");
		}
		return null;
		
	}

//Extracts users with no other information but the user_id
	public static long[] extractUsers(GraphDBManager gdbm){
		Session session = gdbm.getSession();
		String query = "MATCH (u:User) WHERE NOT EXISTS(u.verified) RETURN u.user_id as user_id order by user_id asc";
		StatementResult sr = null;
		//TODO
		try{
			sr = session.run(query);
			List<Long> ids = new ArrayList<Long>();
			for(Record r : sr.list()){
				if(r.get(0)!=null){
					long id = r.get(0).asLong();
					ids.add(id);
				}
				
			}
			if(ids.size()==0){
				System.out.println("No users matched.");
			}
			else
				System.out.println("Matched users: "+ids.size());
			long[] result = ids.stream().mapToLong(l -> l).toArray();
			return result;
		}
		catch(TransientException e){
			System.out.println("TransientException: couldn't run the query succesfully. Transaction canceled.");
		}
		return new long[0];
		
		
	}
	
	public static void fillUpUser(User user){
		String query ="\nMERGE (ou:User{user_id:{ouser_id}})"
				+ " SET ou.followers={ofollowers}, ou.following={ofollowing}, ou.screen_name={oscreen_name}, ou.location={ouser_location}, ou.name={oname}, ou.verified={overified}, ou.profileImage={oprofileImage}, ou.description={odescription}";
			
		String location = "";
		if(user.getLocation()==null)
			location="N/A";
		else
			location=user.getLocation();
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("ouser_id", user.getId());
		parameters.put("oname", user.getName());
		parameters.put("oscreen_name", user.getScreenName());
		parameters.put("ouser_location", location);
		parameters.put("ofollowers", user.getFollowersCount());
		parameters.put("ofollowing", user.getFriendsCount());
		parameters.put("overified", user.isVerified());
		parameters.put("odescription", user.getDescription());
		parameters.put("oprofileImage", user.getBiggerProfileImageURL());
		
		
		//Run the query
		try{
			session.run(query, parameters);
		}
		catch(TransientException e){
			System.out.println("TransientException: couldn't run the query succesfully. Transaction canceled.");
		}
		
	}
	
	public static void fillUpStatus(Status status) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		
		//Finds the shortest path from a tweet and the topic the tweet is related to
		String topicQuery = "MATCH p=shortestPath((t:Tweet)-[*..2]-(m:Topic)) WHERE t.tweet_id={tweet_id} RETURN m.name";
		Map<String, Object> paramTopicQuery = new HashMap<>();
		paramTopicQuery.put("tweet_id", status.getId());
		StatementResult sr = null;
		String topic = "null";
		try {
			sr = session.run(topicQuery, paramTopicQuery);
			if(sr.hasNext()){
				Record r = sr.next();
				topic = r.get("m.name").asString();
			}
			
		} catch (TransientException e) {
			System.out.println("TransientException: couldn't run the query succesfully. Transaction canceled.");
		}
		
		
		String query = 
				"MATCH (t:Tweet{tweet_id:{tweet_id}})"
				+"\n SET t.text={text}, t.location={location}, t.created_at={created_at}, t.language={language}, "
				+ "t.retweetcount={retweetcount}, t.likecount={likecount}";
		String location = "";
		if(status.getPlace()!=null)
			location=status.getPlace().getName()+", "+status.getPlace().getCountry();
		else
			location="N/A";
		query += "\nMERGE (s:Source{application:{source}}) ";
		query += "\nCREATE (t)-[:SENT_FROM]->(s)";
		query += "\nMERGE (u:User{user_id:{user_id}})"
				+ "\nSET u.name={name}, u.screen_name={screen_name}, u.location={u_location}, u.followers={followers}, u.following={following}, u.verified={verified}, u.description={description}, u.profileImage={profileImage}";
		query += "\nMERGE (u)-[:POSTS]->(t)";
		
		if(!topic.equals("null")){
			query += "\nMERGE (topic:Topic{name:{topic}})";
			query += "\nMERGE (t)-[:ABOUT]->(topic)";
			parameters.put("topic", topic);
		}
			
		
		
		User user = status.getUser();
		String temp = status.getSource();
		if(temp.contains(">")){
			temp = temp.split(">")[1];
			temp = temp.substring(0, temp.length()-3);
			parameters.put("source", temp);
		}
		else
			parameters.put("source", "N/A");
		
		
		int nHashtag = 0;
		for(HashtagEntity h : status.getHashtagEntities()){
			nHashtag++;
			parameters.put("tag"+nHashtag, h.getText().toLowerCase());
			query += "\nMERGE (h"+(nHashtag)+":Hashtag{tag:{tag"+nHashtag+"}})";
			query+="\nMERGE (t)-[:TAGS]->(h"+(nHashtag)+")";
		}	
		
		
		int nMentions = 0;
		for(UserMentionEntity ume : status.getUserMentionEntities()){
			nMentions++;
			parameters.put("mentioned_id"+nMentions, ume.getId());
			query += "\nMERGE (user_mentioned"+nMentions+":User{user_id:{mentioned_id"+nMentions+"}})"
						+ " MERGE (t)-[:MENTIONS]->(user_mentioned"+nMentions+")";
		}
		
		
		if(status.getInReplyToStatusId()!=-1){
			long replies;
			replies=status.getInReplyToStatusId();
			query += "\nMERGE (replied:Tweet{tweet_id:{replied}})";
			query += "\nMERGE (t)-[:REPLIES_TO]->(replied)";
			parameters.put("replied", replies);
		}
		
		//Tweet properties
		parameters.put("tweet_id", status.getId());
		parameters.put("text", status.getText());
		parameters.put("location", location);
		parameters.put("created_at", Utilities.convertDate(status.getCreatedAt()));
		parameters.put("retweetcount", status.getRetweetCount());
		parameters.put("likecount", status.getFavoriteCount());
		parameters.put("language", status.getLang());
		
	
		
		String u_location = "";
		if(user.getLocation()!=null)
			u_location=user.getLocation();
		else
			u_location="N/A";
		
		
		//User properties
		parameters.put("user_id", user.getId());
		parameters.put("name", user.getName());
		parameters.put("screen_name", user.getScreenName());
		parameters.put("u_location", u_location);
		parameters.put("followers", user.getFollowersCount());
		parameters.put("following", user.getFriendsCount());
		parameters.put("verified", user.isVerified());
		parameters.put("description", user.getDescription());
		parameters.put("profileImage", user.getBiggerProfileImageURL());
		
		
		try{
			session.run(query, parameters);
		}
		catch(TransientException e){
			System.out.println("TransientException: couldn't run the query succesfully. Transaction canceled.");
		}
		
	}


	public static Set<Status> lookupTweets(long[] ids){

		ConfigurationBuilder cb = getConfigurationBuilder();
        Twitter twitter = new TwitterFactory(cb.build()).getInstance();
        if(ids.length<=100){
        	
        	try {
        		Set<Status> statusSet = new HashSet<Status>();
        		statusSet.addAll(twitter.lookup(ids));
        		return statusSet;
        	//	statusSet = (Set<Status>) twitter.lookup(ids);
			} catch (TwitterException e) {
				System.out.println("Lookup failed");
				if(e.exceededRateLimitation())
					System.out.println("due to exceeded rate limitation.");
				if(e.getErrorCode()==17)
				System.out.println("tweet(s) not found.");
				return null;
			}
        }
        else{
        	List<long[]> idArrays = moreThan100Elements(ids);
        	ResponseList<Status> totalStatuses = null;
        	ResponseList<Status> previousStatuses = null;
        	try{
        		totalStatuses = twitter.lookup(idArrays.get(0));
        		previousStatuses = twitter.lookup(idArrays.get(0));
        	}
        	catch (TwitterException e) {
				System.out.println("Lookup failed");
				if(e.exceededRateLimitation())
					System.out.println("due to exceeded rate limitation.");
				if(e.getErrorCode()==17)
				System.out.println("tweet(s) not found.");
			}
        	
        	for(int i = 1;i<idArrays.size();i++){
        		try {
					ResponseList<Status> currentStatuses = twitter.lookup(idArrays.get(i));
					Stream.of(previousStatuses, currentStatuses).forEach(totalStatuses::addAll);
					previousStatuses = currentStatuses;
				} catch (TwitterException e) {
					System.out.println("Lookup failed");
					if(e.exceededRateLimitation())
						System.out.println("due to exceeded rate limitation.");
					if(e.getErrorCode()==17)
					System.out.println(": tweet(s) not found.");
				}
        	}
        	
        	Set<Status> statusSet = new HashSet<>();
        	try{
        		statusSet.addAll(totalStatuses);
        		return statusSet;
        	}
        	catch(NullPointerException e){
        		System.out.println("Failed to load any status.");
        		return null;
        	}
        	
    		
        	
    	}
	}
	
	
	public static Set<User> lookupUsers(long[] ids){
		ConfigurationBuilder cb = getConfigurationBuilder();
        Twitter twitter = new TwitterFactory(cb.build()).getInstance();
        if(ids.length<=100){
        	try {
        		Set<User> userSet = new HashSet<User>();
        		userSet.addAll(twitter.lookupUsers(ids));
				return userSet;
			} catch (TwitterException e) {
				System.out.println("Lookup failed");
				if(e.exceededRateLimitation())
					System.out.println("due to exceeded rate limitation.");
				if(e.getErrorCode()==17)
				System.out.println("user(s) not found.");
				return null;
			}
        	
        }
        else{
        	List<long[]> idArrays = moreThan100Elements(ids);
        	ResponseList<User> totalUsers = null;
        	ResponseList<User> previousUsers = null;
        	try{
        		totalUsers = twitter.lookupUsers(idArrays.get(0));
        		previousUsers = twitter.lookupUsers(idArrays.get(0));
        	}
        	catch (TwitterException e) {
				System.out.println("Lookup failed");
				if(e.exceededRateLimitation())
					System.out.println("due to exceeded rate limitation.");
				if(e.getErrorCode()==17)
				System.out.println("user(s) not found.");
			}
        	
        	for(int i = 1;i<idArrays.size();i++){
        		try {
					ResponseList<User> currentUsers = twitter.lookupUsers(idArrays.get(i));
					Stream.of(previousUsers, currentUsers).forEach(totalUsers::addAll);
					previousUsers = currentUsers;
				} catch (TwitterException e) {
					System.out.println("Lookup failed");
					if(e.exceededRateLimitation())
						System.out.println(" due to exceeded rate limitation.");
					if(e.getErrorCode()==17)
					System.out.println("user(s) not found.");
				}
        	}
        	
        	Set<User> userSet = new HashSet<>();
    		
    		try{
    			userSet.addAll(totalUsers);
        		return userSet;
        	}
        	catch(NullPointerException e){
        		System.out.println("Failed to load any user.");
        		return null;
        	}
        	
    		
        }
		
		
	}
	
	
	
public static void insertTweet(Session session, String topic, Status status) {
		
		String location;
		if(status.getPlace()!=null)
			location = status.getPlace().getName();
		else
			location = "N/A";
		
		String query = "";
		
		//Query
				query += 	
						"\nMERGE (t:Tweet{tweet_id:{tweet_id}})"
						+ " SET t.text={text}, t.created_at={created_at}, t.retweetcount={retweetcount}, t.likecount={likecount}, t.location={location}, t.language={language}";
				query+="\nMERGE (u:User{user_id:{user_id}})"
						+ " SET u.followers={followers}, u.following={following}, u.screen_name={screen_name}, u.location={user_location}, u.name={name}, u.verified={verified}, u.profileImage={profileImage}, u.description={description}";
						
				query += 
						"\nMERGE (u)-[:POSTS]->(t)";
				query += "\nMERGE (s:Source{application:{source}}) ";
				query += ""
						+ "\nMERGE (t)-[:SENT_FROM]->(s)";
		
		//Tweet properties
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("tweet_id", status.getId());
		parameters.put("text", status.getText());
		parameters.put("created_at", Utilities.convertDate(status.getCreatedAt()));
		parameters.put("retweetcount", status.getRetweetCount());
		parameters.put("likecount", status.getFavoriteCount());
		parameters.put("location", location);
		parameters.put("language", status.getLang());
		
		//User properties
		User user = status.getUser();
		parameters.put("user_id", user.getId());
		parameters.put("screen_name", user.getScreenName());
		parameters.put("name", user.getName());
		String user_location = "N/A";
		if(user.getLocation()!=null)
			user_location=user.getLocation();
		parameters.put("user_location", user_location);
		parameters.put("followers", user.getFollowersCount());
		parameters.put("following", user.getFriendsCount());
		parameters.put("verified", user.isVerified());
		parameters.put("description", user.getDescription());
		parameters.put("profileImage", user.getBiggerProfileImageURL());
		
		//Hashtag property
		int nHashtag = 0;
		for(HashtagEntity h : status.getHashtagEntities()){
			nHashtag++;
			parameters.put("tag"+nHashtag, h.getText().toLowerCase());
			query += "\nMERGE (h"+(nHashtag)+":Hashtag{tag:{tag"+nHashtag+"}})";
			query += "\nMERGE (t)-[:TAGS]->(h"+(nHashtag)+")";
		}			
		
		//Mentions
		int nMentions = 0;
		for(UserMentionEntity ume : status.getUserMentionEntities()){
			nMentions++;
			parameters.put("mentioned_id"+nMentions, ume.getId());
			query += "\nMERGE (user_mentioned"+nMentions+":User{user_id:{mentioned_id"+nMentions+"}})"
						+ " CREATE (t)-[:MENTIONS]->(user_mentioned"+nMentions+")";
		}

	
		//Source
		String temp = status.getSource();
		if(temp.contains(">")){
			temp = temp.split(">")[1];
			temp = temp.substring(0, temp.length()-3);
			parameters.put("source", temp);
		}
		else
			parameters.put("source", "N/A");

		
		
		//Replies to
				if(status.getInReplyToStatusId()!=-1){
					parameters.put("replies", status.getInReplyToStatusId());
					query += "\nMERGE (replied:Tweet{tweet_id:{replies}})"
							+ "\n MERGE (t)-[:REPLIES_TO]->(replied)";
				}
		
		query += "\nMERGE (tv:Topic{name:{topic}})";
		query += "\nMERGE (t)-[:ABOUT]->(tv)";
		
		parameters.put("topic", topic);
				
		

		
		//Run the query
		try{
			session.run(query, parameters);
		}
		catch(TransientException e){
			System.out.println("TransientException: couldn't run the query succesfully. Transaction canceled.");
		}
		
	}
	
	public static void insertRetweet(Session session, String topic, Status status) {
		Status retweet = status.getRetweetedStatus();
		String re_location;
		String query = "";
		
		query+="\nMERGE (t:Tweet{tweet_id:{tweet_id}})"
				+ "ON CREATE SET "
				+ "		t.text={text}, "
				+ "		t.language={language}, "
				+ "		t.created_at={created_at}, "
				+ "     t.retweetcount={retweetcount},"
				+ "     t.likecount={likecount},"
				+ "     t.location={location} "
				+ "	ON MATCH SET "
				+ "	    t.retweetcount={retweetcount}, "
				+ "	    t.likecount={likecount} ";
		
		query+="\nMERGE (rt:Tweet{tweet_id:{re_tweet_id}})"
				+ "ON CREATE SET "
				+ "		rt.text={re_text}, "
				+ "		rt.language={re_language}, "
				+ "		rt.created_at={re_created_at}, "
				+ "     rt.retweetcount={re_retweetcount},"
				+ "     rt.likecount={re_likecount},"
				+ "     rt.location={re_location} "
				+ "	ON MATCH SET "
				+ "	    rt.retweetcount={re_retweetcount}, "
				+ "	    rt.likecount={re_likecount} ";
		
		if(retweet.getPlace()!=null)
			re_location = retweet.getPlace().getName();
		else
			re_location = "N/A";
		
		//Retweet properties
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("re_tweet_id", retweet.getId());
		parameters.put("re_text", retweet.getText());
		parameters.put("re_created_at", Utilities.convertDate(retweet.getCreatedAt()));
		parameters.put("re_retweetcount", retweet.getRetweetCount());
		parameters.put("re_likecount", retweet.getFavoriteCount());
		parameters.put("re_location", re_location);
		parameters.put("re_language", retweet.getLang());
		
		String location;
		if(status.getPlace()!=null)
			location = retweet.getPlace().getName();
		else
			location = "N/A";
		
		//Tweet properties
		parameters.put("tweet_id", status.getId());
		parameters.put("text", status.getText());
		parameters.put("created_at", Utilities.convertDate(status.getCreatedAt()));	
		parameters.put("retweetcount", status.getRetweetCount());			
		parameters.put("likecount", status.getFavoriteCount());			
		parameters.put("location", location);
		parameters.put("language", status.getLang());
		
		String o_user_location = "N/A";
		
		//Original User properties
		User o_user = retweet.getUser();
		parameters.put("ouser_id", o_user.getId());
		parameters.put("oscreen_name", o_user.getScreenName());
		parameters.put("oname", o_user.getName());
		if(o_user.getLocation()!=null)
			o_user_location=o_user.getLocation();
		parameters.put("ouser_location", o_user_location);
		parameters.put("ofollowers", o_user.getFollowersCount());
		parameters.put("ofollowing", o_user.getFriendsCount());
		parameters.put("overified", o_user.isVerified());
		parameters.put("oprofileImage", o_user.getBiggerProfileImageURL());
		String desc = "N/A";
		if(o_user.getDescription()!=null)
			desc=o_user.getDescription();
		parameters.put("odescription", desc);
		
		String user_location = "N/A";
		//User properties
		User user = status.getUser();
		parameters.put("user_id", user.getId());
		parameters.put("screen_name", user.getScreenName());
		parameters.put("name", user.getName());
		if(user.getLocation()!=null)
			user_location=user.getLocation();
		parameters.put("user_location", user_location);
		parameters.put("followers", user.getFollowersCount());
		parameters.put("following", user.getFriendsCount());
		parameters.put("verified", user.isVerified());
		parameters.put("profileImage", user.getBiggerProfileImageURL());
		parameters.put("description", user.getDescription());
		
		//Retweet: Hashtag property
		int nHashtag = 0;
		nHashtag = 0;
		for(HashtagEntity h : retweet.getHashtagEntities()){
			if(!h.getText().equals(null)){
				nHashtag++;
				parameters.put("tag"+nHashtag, h.getText().toLowerCase());
				query += "\nMERGE (h"+(nHashtag)+":Hashtag{tag:{tag"+nHashtag+"}})";
				query += "\nMERGE (rt)-[:TAGS]->(h"+(nHashtag)+")";
			}
			
		}
		
		//Tweet: Hashtag property
		for(HashtagEntity h : status.getHashtagEntities()){
			if(!h.getText().equals(null)){
				nHashtag++;
				parameters.put("tag"+nHashtag, h.getText().toLowerCase());
				query += "\nMERGE (h"+(nHashtag)+":Hashtag{tag:{tag"+nHashtag+"}})";
				query += "\nMERGE (t)-[:TAGS]->(h"+(nHashtag)+")";
			}
			
		}	
		
		
		int nMentions = 0;
		//Mentions
		/*
		 * Deprecated; a retweet always contains a mention to the original user
		 * so storing the retweet's mentions is (very) likely to be trivial
		 */
		/*
		
		for(UserMentionEntity ume : status.getUserMentionEntities()){
			nMentions++;
			parameters.put("mentioned_id"+nMentions, ume.getId());
			query+="\nMERGE (user_mentioned"+nMentions+":User{user_id:{mentioned_id"+nMentions+"}})"
						+ "\nMERGE (t)-[:MENTIONS]->(user_mentioned"+nMentions+")";
		}*/
		
		//Retweet Mentions
		for(UserMentionEntity ume : retweet.getUserMentionEntities()){
			nMentions++;
			parameters.put("mentioned_id"+nMentions, ume.getId());
			query+="\nMERGE (user_mentioned"+nMentions+":User{user_id:{mentioned_id"+nMentions+"}})"
						+ "\nMERGE (rt)-[:MENTIONS]->(user_mentioned"+nMentions+")";
		}
		
		//Source
		String temp = status.getSource();
		if(temp.contains(">")){
			temp = temp.split(">")[1];
			temp = temp.substring(0, temp.length()-3);
			parameters.put("source", temp);
		}
		else
			parameters.put("source", "N/A");
		
		//RetweetSource 
		temp = retweet.getSource();
		if(temp.contains(">")){
			temp = temp.split(">")[1];
			temp = temp.substring(0, temp.length()-3);
			parameters.put("rtsource", temp);
		}
		else
			parameters.put("rtsource", "N/A");
		
		
		//Replies to
		if(status.getInReplyToStatusId()!=-1){
			parameters.put("replies", status.getInReplyToStatusId());
			query+="\nMERGE (replied:Tweet{tweet_id:{replies}})"
					+ "\n MERGE (t)-[:REPLIES_TO]->(replied)";
		}
		
		//Retweet Replies to
		if(retweet.getInReplyToStatusId()!=-1){
			parameters.put("rtreplies", retweet.getInReplyToStatusId());
			query+="\nMERGE (rtreplied:Tweet{tweet_id:{rtreplies}})"
					+ "\n MERGE (rt)-[:REPLIES_TO]->(rtreplied)";
		}	
		
		query+="\nMERGE (ou:User{user_id:{ouser_id}}) "
				+ "\nSET ou.followers={ofollowers}, "
				+ "ou.following={ofollowing}, "
				+ "ou.screen_name={oscreen_name}, "
				+ "ou.location={ouser_location}, "
				+ "ou.name={oname}, "
				+ "ou.verified={overified}, "
				+ "ou.profileImage={oprofileImage}, "
				+ "ou.description={odescription} ";
	
		query+="\nMERGE (u:User{user_id:{user_id}}) "
				+ "\nSET u.followers={followers}, "
				+ "u.following={following}, "
				+ "u.screen_name={screen_name}, "
				+ "u.location={user_location}, "
				+ "u.name={name}, "
				+ "u.verified={verified}, "
				+ "u.profileImage={profileImage}, "
				+ "u.description={description} ";
	
		//Query
		query+="\nMERGE (ou)-[:POSTS]->(rt)";
		query+="\nMERGE (u)-[:POSTS]->(t)";
		query+="\nMERGE (t)-[:RETWEETS]->(rt)";
		query += "\nMERGE (s:Source{application:{source}}) ";
		query+="\nMERGE (t)-[:SENT_FROM]->(s)";
		query += "\nMERGE (rs:Source{application:{rtsource}}) ";
		query+="\nMERGE (rt)-[:SENT_FROM]->(rs)";

		
		parameters.put("topic", topic);
		
		query += "\nMERGE (tv:Topic{name:{topic}})";
		query += "\nMERGE (t)-[:ABOUT]->(tv)";
		query += "\nMERGE (rt)-[:ABOUT]->(tv)";
		
		//Run the query
		try{
			session.run(query, parameters);
		}
		catch(TransientException e){
			System.out.println("TransientException: couldn't run the query succesfully. Transaction canceled.");
		}
		
	}
	
	
	
	
	public static String[] readTwitterAuth() throws FileNotFoundException {
		
		if(devAccount1.equals("true")){
			return readTwitterAuth("config/credenziali_twitter2.txt");
		}
		else{
			if(devAccount2.equals("true"))
				return readTwitterAuth("config/credenziali_twitter3.txt");
			else{
				if(devAccount3.equals("true"))
					return readTwitterAuth("config/credenziali_twitter4.txt");
				else
					JOptionPane.showMessageDialog(null, "Error: twitter dev account not chosen", "Error", JOptionPane.ERROR_MESSAGE);

				System.exit(-1);
			}
		}
			 
		return null;
	}
	
	public static String [] readTwitterAuth(String file){
		String [] output = new String[4];
		boolean success = false;
		while(!success){
			Scanner sc = null;
			try {
				sc = new Scanner(new File(file));
				success = true;
				int count = 0;
				while(sc.hasNextLine()){
					output[count++] = sc.nextLine();
				}
				sc.close();
			} catch (FileNotFoundException e1) {
				System.out.println("File not found. Please type its location below (press 0 to exit):");
				Scanner sc2 = new Scanner(System.in);
				file = sc2.nextLine();
				if(file.equals("0"))
					System.exit(-1);
				sc2.close();
			}
			
		}
		return output;
	}
	
	public Configuration getConfig() {
		return config;
	}


	public void setConfig(Configuration config) {
		this.config = config;
	}


	public StatusListener getListener() {
		return listener;
	}


	public void setListener(LinkedBlockingQueue<Status> queue) {
		this.listener = new StatusListener() {
			
			@Override
			public void onStatus(Status status) {
				queue.offer(status);
			}

			@Override
			public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
				System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
			}

			@Override
			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
		//		System.out.println("Got track limitation notice: " + numberOfLimitedStatuses);
			}

			@Override
			public void onScrubGeo(long userId, long upToStatusId) {
				System.out.println("Got scrub_geo event userId:" + userId + "upToStatusId:" + upToStatusId);
			}

			@Override
			public void onStallWarning(StallWarning warning) {
				System.out.println("Got stall warning:" + warning);
			}

			@Override
			public void onException(Exception ex) {
				ex.printStackTrace();
			}
		};
	}
	
	
	
	public FilterQuery setQueryParameters(){
		FilterQuery query = new FilterQuery();
		if(!languagesList.contains("all"))
			query.language(languagesList.toArray(new String[0]));
		query.track(keywords);
		return query;
	}


	public static String getTopic() {
		return topic;
	}


	public static void setTopic(String topic) {
		TwitterManager.topic = topic;
	}
	

	public String getTimeFilter() {
		return timeFilter;
	}

	public static void setTimeFilter(String timeFilter) {
		TwitterManager.timeFilter = timeFilter;
	}

	public GraphDBManager getGdbm() {
		return gdbm;
	}

	public static void setGdbm(GraphDBManager gdbm) {
		TwitterManager.gdbm = gdbm;
	}
	}
	

	

