package stream_data;

import java.awt.Dimension;
import java.awt.Font;
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
import java.util.stream.Stream;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.ColorUIResource;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

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

public class TwitterManager {

	private static Session session;
	private static ConfigurationBuilder cb;
	private static Icon icon =  new ImageIcon("config/icon.png");
	private static String topic;
	private Configuration config;
	private StatusListener listener;
	
	public TwitterManager(LinkedBlockingQueue<Status> queue){
		String[] arguments;
		try {
			arguments = readTwitterAuth();
			cb = new ConfigurationBuilder();
			cb.setDebugEnabled(true).setOAuthConsumerKey(arguments[0]).setOAuthConsumerSecret(arguments[1]).setOAuthAccessToken(arguments[2]).setOAuthAccessTokenSecret(arguments[3]);
			setConfig(cb.build());
			setListener(queue);
			
		} 
		catch (FileNotFoundException e) {
			System.out.println("File not found: twitter dev credentials");
			e.printStackTrace();
		}
		
	}
	
	public TwitterManager(){
		GraphDBManager gdbm = new GraphDBManager();
		this.session = gdbm.getSession();
	}
	
	
	
	
	
	public static ResponseList<User> lookupUsers(long id){
		ConfigurationBuilder cb = getConfigurationBuilder();
		Twitter twitter = new TwitterFactory(cb.build()).getInstance();
        try {
			return twitter.lookupUsers(id);
		} catch (TwitterException e) {
			System.out.print("Lookup failed");
			if(e.exceededRateLimitation())
				System.out.println(" due to exceeded rate limitation.");
			if(e.getErrorCode()==17)
			System.out.println(": user(s) not found.");
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
	auth = TwitterManager.readTwitterAuth("config/credenziali_twitter3.txt");
	ConfigurationBuilder cb = new ConfigurationBuilder();
	cb.setDebugEnabled(true).setOAuthConsumerKey(auth[0]).setOAuthConsumerSecret(auth[1])
			.setOAuthAccessToken(auth[2]).setOAuthAccessTokenSecret(auth[3]);
	return cb;
}
	
	
public static long[] extractTweets(GraphDBManager gdbm) {
		Session session = gdbm.getSession();
		String query = "MATCH (t:Tweet) WHERE NOT EXISTS(t.text) RETURN t.tweet_id as tweet_id";
		StatementResult sr = session.run(query);
		List<Long> ids = new ArrayList<Long>();
		for(Record r : sr.list()){
			long id = r.get(0).asLong();
			ids.add(id);
		}
		if(ids.size()==0){
			System.out.println("No tweet matched.");
		}
		else
			System.out.println("Matched tweet: "+ids.size());
		
			
		long[] result = ids.stream().mapToLong(l -> l).toArray();
		return result;
	}

//Extracts users with no other information but the user_id
	public static long[] extractUsers(GraphDBManager gdbm){
		Session session = gdbm.getSession();
		String query = "MATCH (u:User) WHERE NOT EXISTS(u.name) RETURN u.user_id as user_id order by user_id asc";
		StatementResult sr = session.run(query);
		List<Long> ids = new ArrayList<Long>();
		for(Record r : sr.list()){
			long id = r.get(0).asLong();
			ids.add(id);
		}
		if(ids.size()==0){
			System.out.println("No users matched.");
		}
		else
			System.out.println("Matched users: "+ids.size());
			
		long[] result = ids.stream().mapToLong(l -> l).toArray();
		return result;
	}
	
	public static void fillUpUser(User user){
		String query = 
				"MATCH (u:User{user_id:{user_id}})"
				+"\n SET u.name={name}, u.screen_name={screen_name}, u.location={location}, u.followers={followers}, u.following={following}";
		String location = "";
		if(user.getLocation()==null)
			location="null";
		else
			location=user.getLocation();
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("user_id", user.getId());
		parameters.put("name", user.getName());
		parameters.put("screen_name", user.getScreenName());
		parameters.put("location", location);
		parameters.put("followers", user.getFollowersCount());
		parameters.put("following", user.getFriendsCount());
		
		//Run the query
		session.run(query, parameters);
	}
	
	public static void fillUpStatus(Status status) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		String query = 
				"MATCH (t:Tweet{tweet_id:{tweet_id}})"
				+"\n SET t.text={text}, t.location={location}, t.created_at={created_at}, t.language={language}, "
				+ "t.retweetcount={retweetcount}, t.likecount={likecount}";
		String location = "";
		if(status.getPlace()!=null)
			location=status.getPlace().getName()+", "+status.getPlace().getCountry();
		else
			location="null";
		query += "\nCREATE (t)-[:SENT_FROM]->(s:Source{name:{source}})";
		query += "\nMERGE (u:User{user_id:{user_id}})-[:POSTS]->(t)";
		
		User user = status.getUser();
		String temp = status.getSource();
		temp = temp.split(">")[1];
		temp = temp.substring(0, temp.length()-3);
		
		int nHashtag = 0;
		for(HashtagEntity h : status.getHashtagEntities()){
			nHashtag++;
			parameters.put("tag"+nHashtag, h.getText().toLowerCase());
			query+="\nMERGE (t)-[:TAGS]->(h"+(nHashtag)+":Hashtag{tag:{tag"+nHashtag+"}})";
		}	
		
		
		int nMentions = 0;
		for(UserMentionEntity ume : status.getUserMentionEntities()){
			nMentions++;
			parameters.put("mentioned_id"+nMentions, ume.getId());
			query += "\nMERGE (user_mentioned"+nMentions+":User{user_id:{mentioned_id"+nMentions+"}})"
						+ " CREATE (t)-[:MENTIONS]->(user_mentioned"+nMentions+")";
		}
		
		parameters.put("tweet_id", status.getId());
		parameters.put("text", status.getText());
		parameters.put("location", location);
		parameters.put("created_at", status.getId());
		parameters.put("created_at", Utilities.convertDate(status.getCreatedAt()));
		parameters.put("retweetcount", status.getRetweetCount());
		parameters.put("likecount", status.getFavoriteCount());
		parameters.put("source", temp);
		parameters.put("user_id", user.getId());
		parameters.put("language", status.getLang());
		
		session.run(query, parameters);
		
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
				System.out.print("Lookup failed");
				if(e.exceededRateLimitation())
					System.out.println(" due to exceeded rate limitation.");
				if(e.getErrorCode()==17)
				System.out.println(": tweet(s) not found.");
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
				System.out.print("Lookup failed");
				if(e.exceededRateLimitation())
					System.out.println(" due to exceeded rate limitation.");
				if(e.getErrorCode()==17)
				System.out.println(": tweet(s) not found.");
			}
        	
        	for(int i = 1;i<idArrays.size();i++){
        		try {
					ResponseList<Status> currentStatuses = twitter.lookup(idArrays.get(i));
					Stream.of(previousStatuses, currentStatuses).forEach(totalStatuses::addAll);
					previousStatuses = currentStatuses;
				} catch (TwitterException e) {
					System.out.print("Lookup failed");
					if(e.exceededRateLimitation())
						System.out.println(" due to exceeded rate limitation.");
					if(e.getErrorCode()==17)
					System.out.println(": tweet(s) not found.");
				}
        	}
        	
        	Set<Status> statusSet = new HashSet<>();
    		statusSet.addAll(totalStatuses);
    		return statusSet;
        	
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
				System.out.print("Lookup failed");
				if(e.exceededRateLimitation())
					System.out.println(" due to exceeded rate limitation.");
				if(e.getErrorCode()==17)
				System.out.println(": user(s) not found.");
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
				System.out.print("Lookup failed");
				if(e.exceededRateLimitation())
					System.out.println(" due to exceeded rate limitation.");
				if(e.getErrorCode()==17)
				System.out.println(": user(s) not found.");
			}
        	
        	for(int i = 1;i<idArrays.size();i++){
        		try {
					ResponseList<User> currentUsers = twitter.lookupUsers(idArrays.get(i));
					Stream.of(previousUsers, currentUsers).forEach(totalUsers::addAll);
					previousUsers = currentUsers;
				} catch (TwitterException e) {
					System.out.print("Lookup failed");
					if(e.exceededRateLimitation())
						System.out.println(" due to exceeded rate limitation.");
					if(e.getErrorCode()==17)
					System.out.println(": user(s) not found.");
				}
        	}
        	
        	Set<User> userSet = new HashSet<>();
    		userSet.addAll(totalUsers);
    		return userSet;
        	
    		
        }
		
		
	}
	
	
	
public static void insertTweet(Session session, String about, Status status) {
		
		String location;
		if(status.getPlace()!=null)
			location = status.getPlace().getName();
		else
			location = "null";
		
		String query = "";
		
		//Query
				query += 	
						"\nCREATE (t:Tweet{tweet_id:{tweet_id}})"
						+ " SET t.text={text}, t.created_at={created_at}, t.about={about}, t.retweetcount={retweetcount}, t.likecount={likecount}, t.location={location}";
				query += 
						"\nMERGE (u:User{user_id:{user_id}})"
						+ " SET u.screen_name={screen_name}, u.name={name}, u.location={user_location}, u.followers={followers}, u.following={following}";
				query += 
						"\nMERGE (u)-[:POSTS]->(t)";
				query += ""
						+ "\nMERGE (t)-[:SENT_FROM]->(source:Source{name:{source}})";
		
		//Tweet properties
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("tweet_id", status.getId());
		parameters.put("text", status.getText());
		parameters.put("created_at", Utilities.convertDate(status.getCreatedAt()));
		parameters.put("about", about);
		parameters.put("retweetcount", status.getRetweetCount());
		parameters.put("likecount", status.getFavoriteCount());
		parameters.put("location", location);
		
		
		//User properties
		User user = status.getUser();
		parameters.put("user_id", user.getId());
		parameters.put("screen_name", user.getScreenName());
		parameters.put("name", user.getName());
		parameters.put("user_location", user.getLocation());
		parameters.put("followers", user.getFollowersCount());
		parameters.put("following", user.getFriendsCount());
		
		//Hashtag property
		int nHashtag = 0;
		for(HashtagEntity h : status.getHashtagEntities()){
			nHashtag++;
			parameters.put("tag"+nHashtag, h.getText().toLowerCase());
			query+="\nMERGE (t)-[:TAGS]->(h"+(nHashtag)+":Hashtag{tag:{tag"+nHashtag+"}})";
		}			
		
		//Mentions
		String storeMentions = "";
		int nMentions = 0;
		for(UserMentionEntity ume : status.getUserMentionEntities()){
			nMentions++;
			parameters.put("mentioned_id"+nMentions, ume.getId());
			query += "\nMERGE (user_mentioned"+nMentions+":User{user_id:{mentioned_id"+nMentions+"}})"
						+ " CREATE (t)-[:MENTIONS]->(user_mentioned"+nMentions+")";
		}

		//Source
		String temp = status.getSource();
		temp = temp.split(">")[1];
		temp = temp.substring(0, temp.length()-3);
		parameters.put("source", temp);
		
		//Replies to
				String repliesTo = "";
				if(status.getInReplyToStatusId()!=-1){
					parameters.put("replies", status.getInReplyToStatusId());
					query += "\nMERGE (replied:Tweet{tweet_id:{replies}})"
							+ "\n MERGE (t)-[:REPLIES_TO]->(replied)";
				}
		
		
		

		
		//Run the query
		session.run(query, parameters);
		
	}
	
	public static void insertRetweet(Session session, String about, Status status) {
		Status retweet = status.getRetweetedStatus();
		String re_location;
		if(retweet.getPlace()!=null)
			re_location = retweet.getPlace().getName();
		else
			re_location = "null";
		
		//Retweet properties
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("re_tweet_id", retweet.getId());
		parameters.put("re_text", retweet.getText());
		parameters.put("re_created_at", Utilities.convertDate(retweet.getCreatedAt()));
		parameters.put("re_user", retweet.getUser().getScreenName());
		parameters.put("re_about", about);
		parameters.put("re_retweetcount", retweet.getRetweetCount());
		parameters.put("re_likecount", retweet.getFavoriteCount());
		parameters.put("re_location", re_location);
		parameters.put("re_language", retweet.getLang());
		
		String location;
		if(status.getPlace()!=null)
			location = retweet.getPlace().getName();
		else
			location = "null";
		
		//Tweet properties
		parameters.put("tweet_id", status.getId());
		parameters.put("text", status.getText());
		parameters.put("created_at", Utilities.convertDate(status.getCreatedAt()));
		parameters.put("about", about);			
		parameters.put("retweetcount", status.getRetweetCount());			
		parameters.put("likecount", status.getFavoriteCount());			
		parameters.put("location", location);
		parameters.put("language", status.getLang());
		
		//Original User properties
		User o_user = retweet.getUser();
		parameters.put("ouser_id", o_user.getId());
		parameters.put("oscreen_name", o_user.getScreenName());
		parameters.put("oname", o_user.getName());
		parameters.put("ouser_location", o_user.getLocation());
		parameters.put("ofollowers", o_user.getFollowersCount());
		parameters.put("ofollowing", o_user.getFriendsCount());
		
		//User properties
		User user = status.getUser();
		parameters.put("user_id", user.getId());
		parameters.put("screen_name", user.getScreenName());
		parameters.put("name", user.getName());
		parameters.put("user_location", user.getLocation());
		parameters.put("followers", user.getFollowersCount());
		parameters.put("following", user.getFriendsCount());
		
		//Retweet: Hashtag property
		String storeRetweetHashtags = "";
		int nHashtag = 0;
		nHashtag = 0;
		for(HashtagEntity h : retweet.getHashtagEntities()){
			nHashtag++;
			parameters.put("tag"+nHashtag, h.getText().toLowerCase());
			storeRetweetHashtags="MERGE (t)-[:TAGS]->(h"+(nHashtag)+":Hashtag{tag:{tag"+nHashtag+"}})";
		}
		
		//Tweet: Hashtag property
		String storeHashtags = "";
		for(HashtagEntity h : status.getHashtagEntities()){
			nHashtag++;
			parameters.put("tag"+nHashtag, h.getText().toLowerCase());
			storeHashtags="MERGE (t)-[:TAGS]->(h"+(nHashtag)+":Hashtag{tag:{tag"+nHashtag+"}})";
		}	
		
		
		//Mentions
		String storeMentions = "";
		int nMentions = 0;
		for(UserMentionEntity ume : status.getUserMentionEntities()){
			nMentions++;
			parameters.put("mentioned_id"+nMentions, ume.getId());
			storeMentions = "MERGE (user_mentioned"+nMentions+":User{user_id:{mentioned_id"+nMentions+"}})"
						+ " CREATE (t)-[:MENTIONS]->(user_mentioned"+nMentions+")";
		}
		
		//Retweet Mentions
		String storeRetweetMentions = "";
		for(UserMentionEntity ume : retweet.getUserMentionEntities()){
			nMentions++;
			parameters.put("mentioned_id"+nMentions, ume.getId());
			storeMentions = "MERGE (user_mentioned"+nMentions+":User{user_id:{mentioned_id"+nMentions+"}})"
						+ " CREATE (t)-[:MENTIONS]->(user_mentioned"+nMentions+")";
		}
		
		//Source
		String temp = status.getSource();
		temp = temp.split(">")[1];
		temp = temp.substring(0, temp.length()-3);
		parameters.put("source", temp);
		//RetweetSource 
		temp = retweet.getSource();
		temp = temp.split(">")[1];
		temp = temp.substring(0, temp.length()-3);
		parameters.put("rtsource", temp);
		
		//Replies to
		String repliesTo = "";
		if(status.getInReplyToStatusId()!=-1){
			parameters.put("replies", status.getInReplyToStatusId());
			repliesTo = "MERGE (replied:Tweet{tweet_id:{replies}})"
					+ "\n MERGE (t)-[:REPLIES_TO]->(replied)";
		}
		
		//Retweet Replies to
				String rtRepliesTo = "";
				if(retweet.getInReplyToStatusId()!=-1){
					parameters.put("rtreplies", retweet.getInReplyToStatusId());
					repliesTo = "MERGE (rtreplied:Tweet{tweet_id:{rtreplies}})"
							+ "\n MERGE (t)-[:REPLIES_TO]->(rtreplied)";
				}	
		
				
		
		//Query
		String storeTweet = 	
				"CREATE (t:Tweet{tweet_id:{tweet_id}})"
				+ " SET t.text={text}, t.language={language}, t.created_at={created_at},  t.about={about}, t.retweetcount={retweetcount}, t.likecount={likecount}, t.location={location}";
		String storeReTweet = 	
				"MERGE (rt:Tweet{tweet_id:{re_tweet_id}})"
				+ " ON MATCH SET rt.retweetcount={re_retweetcount}, rt.likecount={re_likecount}"
				+ " ON CREATE SET rt.text={re_text}, rt.language={re_language}, rt.created_at={re_created_at}, rt.about={re_about}, rt.retweetcount={re_retweetcount}, rt.likecount={re_likecount}, rt.location={re_location}";
		String storeOriginalUser = 
				"MERGE (ou:User{user_id:{ouser_id}})"
				+ " SET ou.followers={ofollowers}, ou.following={ofollowing}, ou.screen_name={oscreen_name}, ou.location={ouser_location}, ou.name={oname}";
		String storeUser = 
				"MERGE (u:User{user_id:{user_id}})"
				+ " SET u.followers={followers}, u.following={following}, u.screen_name={screen_name}, u.location={user_location}, u.name={name}";
		String storeOriginalPosts = 
				"MERGE (ou)-[:POSTS]->(rt)";
		String storePosts = 
				"MERGE (u)-[:POSTS]->(t)";
		String storeRetweetRelationship = 
				"CREATE (t)-[:RETWEETS]->(rt)";
		String storeSource = ""
				+ "MERGE (t)-[:SENT_FROM]->(source:Source{name:{source}})";
		String storeRetweetSource = ""
				+ "MERGE (rt)-[:SENT_FROM]->(rtsource:Source{name:{rtsource}})";
		String finalQuery = storeTweet+"\n"+storeReTweet+"\n"+storeUser+"\n"+storeOriginalUser+"\n"+storeHashtags+
							"\n"+storeRetweetHashtags+"\n"+storeOriginalPosts+"\n"+storePosts+"\n"+storeRetweetRelationship+
							"\n"+storeMentions+"\n"+storeRetweetMentions+"\n"+storeSource+"\n"+storeRetweetSource+"\n"+repliesTo+"\n"+rtRepliesTo;
		
		//Run the query
		session.run(finalQuery, parameters);
		
	}
	
	
	public static String[] readTwitterAuth() throws FileNotFoundException {
		UIManager.put("OptionPane.background", new ColorUIResource(214,227,249));
		 UIManager.put("Panel.background",new ColorUIResource(214,227,249));
		 Dimension size = UIManager.getDimension("OptionPane.minimumSize");
		 size.width = 450;
		 size.height= 150;
		 UIManager.put("OptionPane.minimumSize", size);
		 JLabel message = new JLabel();
		 message.setText("Scegliere credenziali dev.twitter.com");
		 message.setFont(new Font("Calibri", Font.BOLD, 20));
		 String [] options = {"1", "2"};
		 int scelta = JOptionPane.showOptionDialog(null, message, "Credenziali", 2, 0, icon, options, options[0]);
		 if(scelta==0)
			 return readTwitterAuth("config/credenziali_twitter2.txt");
		 if(scelta==1)
			 return readTwitterAuth("config/credenziali_twitter3.txt");
		 if(scelta!=0 && scelta!=1){
			 System.out.println("Error: twitter dev account not chosen");
			 System.exit(-1);
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
	
	private static FilterQuery importLanguagesInQuery(FilterQuery query, String[] lang) {
		if(lang.length==0)
			return query.language();
		if(lang.length==1)
			return query.language(lang[0]);
		if(lang.length==2)
			return query.language(lang[0], lang[1]);
		if(lang.length==3)
			return query.language(lang[0], lang[1], lang[2]);
		if(lang.length==4)
			return query.language(lang[0], lang[1], lang[2], lang[3]);
		if(lang.length==5)
			return query.language(lang[0], lang[1], lang[2], lang[3], lang[4]);
		if(lang.length==6)
			return query.language(lang[0], lang[1], lang[2], lang[3], lang[4], lang[5]);
		if(lang.length==7)
			return query.language(lang[0], lang[1], lang[2], lang[3], lang[4], lang[5], lang[6]);
		if(lang.length==8)
			return query.language(lang[0], lang[1], lang[2], lang[3], lang[4], lang[5], lang[6], lang[7]);
		return null;
		
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
	
	public static List<Object> insertMultipleValues() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException{

		   JTextField key = new JTextField();
		   JTextField lang = new JTextField();
		   JTextField topic = new JTextField();
		   JLabel label = new JLabel();
		   label.setText("Inserire i parametri: ");
		   label.setFont(new Font("Calibri", Font.BOLD, 20));
		   Object[] message = {label, "Keywords:", key, "Languages:", lang, "Topic:", topic};
		   Dimension size = UIManager.getDimension("OptionPane.minimumSize");
		   size.width = 450;
		   size.height= 300;
		   UIManager.put("OptionPane.background", new ColorUIResource(214,227,249));
		   UIManager.put("Panel.background",new ColorUIResource(214,227,249));
		   int option = JOptionPane.showConfirmDialog(null, message, "Producer", JOptionPane.OK_CANCEL_OPTION, 0, icon);
		   
		   while(option != JOptionPane.OK_OPTION || key.getText().length()<1){
					if(noParamsChosen()==0){
						option = JOptionPane.showConfirmDialog(null, message, "Producer", JOptionPane.OK_CANCEL_OPTION, 0, icon);
						if(option == JOptionPane.OK_OPTION && key.getText().length()>1 && lang.getText().length()>=2)
							break;
					}
					else{
						System.exit(-1);
					}
						
				}
		   
		   String [] keywords = key.getText().split(",");
		   String [] languages = lang.getText().split(",");
		   String topicScelto = topic.getText();
		   List<Object> output = new ArrayList<Object>();
		   output.add(keywords);
		   output.add(languages);
		   output.add(topicScelto);
		   setTopic(topicScelto);
		   return output;
	}
	
	private static int noParamsChosen() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException{
		JLabel label2 = new JLabel("<html>Parametri non inseriti correttamente.<br>Riprovare?</html>");
		label2.setFont(new Font("Calibri", Font.BOLD, 20));
		String [] errorOptions = {"Riprova", "Esci"};
		return JOptionPane.showOptionDialog(null, label2, "Topic", 0, 0, icon, errorOptions, "Riprova");
		}
	
	
	


	public FilterQuery setQueryParameters() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		FilterQuery query = new FilterQuery();
		
		List<Object> params = insertMultipleValues();
		String [] lang = (String[]) params.get(1);
		String [] keyWords = (String[]) params.get(0);
		query.track(keyWords);
		if(lang.length>0)
			query = importLanguagesInQuery(query, lang);
		return query;
		
	}


	public static String getTopic() {
		return topic;
	}


	public static void setTopic(String topic) {
		TwitterManager.topic = topic;
	}

	

	
}
