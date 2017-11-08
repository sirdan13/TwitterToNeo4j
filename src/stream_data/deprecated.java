package stream_data;

import java.awt.Dimension;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import java.awt.Font;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.ColorUIResource;

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
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.UserMentionEntity;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;


public class deprecated {
	
	static Icon icon =  new ImageIcon("config/icon.png");
	static Connection c;
	static ConfigurationBuilder cb;
	static Twitter twitter;
	static Driver driver;
	static Session session;
	static int contatore;
	static int cont_test;
	static double media;
	static long mediadiff;
	static long startRun;
	
	public static long lastLimitation = System.currentTimeMillis();
	
	static PreparedStatement psCreateIndexTweet;
	static PreparedStatement psCreateIndexHT;
	
	static String cqlCreateIndexTweet = "create index on :Tweet(tweet_id)";
	static String cqlCreateIndexHT = "create index on :Hashtag(text)";
	
	private static void openConnection() throws FileNotFoundException{
		Scanner sc = new Scanner(new File("config/connessione.txt"));
		String host, user, pass;
		host = sc.nextLine();
		user = sc.nextLine();
		pass = sc.nextLine();
		sc.close();
		driver = GraphDatabase.driver(host, AuthTokens.basic(user, pass));
		session = driver.session();
	}
	
	
	private static void insertData(Status status, String topic) throws TwitterException{
	
		if(status.isRetweet())
			insertRetweet(status, topic);
		else
			insertTweet(status, topic);
	}
	
	private static void insertTweet(Status status, String topic) throws TwitterException{
		
		String user = 	"MERGE (n:User{user_id:{1}}) "+
						"SET n.name={2}, n.screen_name={3}, n.followers={4}, n.following={5}, n.location={6}, n.profilePic={7}";
		String tweet =	"MERGE (t:Tweet{tweet_id:{9}})"+
						"\nSET t.text={8}, t.location={10}, t.retweetcount={11}, t.likecount={12}, t.language={13}, t.topic={14}, t.created_at={15} "+
						"\n CREATE (n)-[:POSTS]->(t)";
		String tweetSource = "\nMERGE (s:Source{name:{source_name}})"+
							"\nCREATE (t)-[:TWEETED_BY]->(s)";
		int cont = 16;
		String hashtag = "";
		for(HashtagEntity h : status.getHashtagEntities()){
			hashtag +=	"\nMERGE (h"+cont+":Hashtag{text:'"+h.getText().toLowerCase()+"'}) "
						+"CREATE (t)-[:TAGS]->(h"+cont+") \n";
			cont++;
		}
			
		
	//	twitter = new TwitterFactory(cb.build()).getInstance();
		
		String screen_names = "";
		ResponseList<User> users = null;
		for(UserMentionEntity ume : status.getUserMentionEntities())
			screen_names += ume.getScreenName()+",";
		if(screen_names.length()>1){
			screen_names = screen_names.substring(0, screen_names.length()-1);
			try{
				users = twitter.lookupUsers(screen_names);
			}
			catch(TwitterException e){
				screen_names="";
			}
			
			
		}
		
		String mentions = "";
		int j = 80;
			if(screen_names.length()>1)
				for(User u : users){
					String usersName = "";
						usersName = u.getName().replaceAll("'", " ").replaceAll("'", " ").replaceAll("\\\\", " ").replaceAll("'", " ");
					String usersLocation = "";
						usersLocation = u.getLocation().replaceAll("'", " ").replaceAll("'", " ").replaceAll("\\\\", " ").replaceAll("'", " ");
					mentions += 	"\nMERGE (u"+j+":User{user_id:"+u.getId()+"})"+
									"\nSET u"+j+".name='"+usersName+"', u"+j+".screen_name='"+u.getScreenName()+"', u"+j+".followers="+u.getFollowersCount()+", u"+j+".following="+u.getFriendsCount()+", u"+j+".location='"+usersLocation+"', u"+j+".profilePic='"+u.getBiggerProfileImageURL()+"'"+
									"\nCREATE (u"+j+")<-[:MENTIONS]-(t)";
					j++;
			}
		
		String location = null, created_at, source = null;
		
		String  userLocation = null;
		created_at = DateManager.dateToString(status.getCreatedAt());  
		if(status.getSource()!=null){
			source = status.getSource();
			try{
				source = source.split(">")[1];
				source = source.substring(0, source.length()-3);
			}
			catch(ArrayIndexOutOfBoundsException e){
				source = status.getSource();
			}
			
		}
			
		if(status.getPlace()!=null)
				location = status.getPlace().getName().replaceAll("'", " ").replaceAll("'", " ").replaceAll("\\\\", " ").replaceAll("'", " ")+", "+status.getPlace().getCountry();
		User author = status.getUser();
		if(author.getLocation()!=null)
			userLocation = author.getLocation().replaceAll("'", " ").replaceAll("'", " ").replaceAll("\\\\", " ").replaceAll("'", " ");

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("1", author.getId()); parameters.put("2", author.getName().replaceAll("'", " ").replaceAll("'", " ").replaceAll("\\\\", " ").replaceAll("'", " ")); parameters.put("3", author.getScreenName()); parameters.put("4", author.getFollowersCount()); parameters.put("5", author.getFriendsCount());
		parameters.put("6", userLocation); parameters.put("7", author.getBiggerProfileImageURL()); parameters.put("8", status.getText().replaceAll("'", " ").replaceAll("'", " ").replaceAll("\\\\", " ").replaceAll("'", " ")); parameters.put("9", status.getId()); parameters.put("10", location);
		parameters.put("11", status.getRetweetCount()); parameters.put("12", status.getFavoriteCount()); parameters.put("13", status.getLang()); parameters.put("14", topic); parameters.put("15", created_at); parameters.put("source_name", source);
		cont = 16;
		
		String tweetToProgram = "MERGE (tv:TV_Program{name:'"+topic+"'})"+
				"\nCREATE (t)-[:ABOUT]->(tv) ";
	//	String reply = "";
		//IF IT'S NOT A REPLY, IT WILL RETURN -1
		String replyQuery = "";
		//TODO TOO SLOW
		if(status.getInReplyToStatusId()>0){
			ResponseList<Status> replied = twitter.lookup(status.getInReplyToStatusId());
			int k = 700;
			for(Status repliedStatus : replied){
				String locationTweet = null;
				if(repliedStatus.getPlace()!=null)
					locationTweet = repliedStatus.getPlace().getName();
				replyQuery += "\nMERGE (t"+k+":Tweet{tweet_id:"+repliedStatus.getId()+"})"+
						"\nSET t"+k+".text='"+repliedStatus.getText().replaceAll("'", " ").replaceAll("'", " ").replaceAll("\\\\", " ").replaceAll("'", " ")+"', t"+k+".location='"+locationTweet+"', t"+k+".retweetcount="+repliedStatus.getRetweetCount()+", t"+k+".likecount="+repliedStatus.getFavoriteCount()+", t"+k+".language='"+repliedStatus.getLang()+"', t"+k+".topic='"+topic+"', t"+k+".created_at='"+DateManager.dateToString(repliedStatus.getCreatedAt())+"' "+
						"\nCREATE (t"+k+")<-[:REPLIES_TO]-(t)";
				User user_replied = repliedStatus.getUser();
				long uid = user_replied.getId(); String uname = user_replied.getName().replaceAll("'", " ").replaceAll("'", " ").replaceAll("\\\\", " ").replaceAll("'", " ");
				String uscreen = user_replied.getScreenName();
				String uloc = null;
				if(user_replied.getLocation()!=null)
					uloc = user_replied.getLocation().replaceAll("'", " ").replaceAll("'", " ").replaceAll("\\\\", " ").replaceAll("'", " ");
				int ufoll = user_replied.getFollowersCount(); int ufriends = user_replied.getFriendsCount();
				replyQuery += "\nMERGE (u"+k+":User{user_id:"+uid+"})"+
								"\nSET u"+k+".name='"+uname+"', u"+k+".screen_name='"+uscreen+"', u"+k+".followers="+ufoll+", u"+k+".following="+ufriends+", u"+k+".location='"+uloc+"'"+
								"\nCREATE (u"+k+")-[:POSTS]->(t"+k+")";
			}
			
		}
		
		
		String query = user+'\n'+tweet+'\n'+hashtag+'\n'+tweetSource+'\n'+tweetToProgram+'\n'+mentions+'\n'+replyQuery;
	//	System.out.println(query);
		session.run(query, parameters);
	
		contatore++;
		if(contatore%50==0)
			System.out.println("Tweet ricevuti: "+contatore);
		
			
	}
	
	private static void insertRetweet(Status status, String topic) throws TwitterException{
		
		String user = 	"MERGE (n:User{user_id:{1}}) "+
				"SET n.name={2}, n.screen_name={3}, n.followers={4}, n.following={5}, n.location={6}, n.profilePic={7} \n";
		String original_user = "MERGE (author:User{user_id:{8}}) "+
				"SET author.name={9}, author.screen_name={10}, author.followers={11}, author.following={12}, author.location={13}, author.profilePic={14} \n";
		String retweet =	"CREATE (n)-[:POSTS]->(t:Tweet{text:{15},tweet_id:{16},location:{17},retweetcount:{18},likecount:{19},language:{20},topic:{21},created_at:{22}}) \n";
		String original_tweet = "MERGE (author)-[:POSTS]->(original_tweet:Tweet{tweet_id:{23}}) "+
				"ON CREATE SET original_tweet.text={24}, original_tweet.tweet_id={23}, original_tweet.location={25}, original_tweet.retweetcount={26}, original_tweet.likecount={27}, original_tweet.language={28}, original_tweet.topic={29}, original_tweet.created_at={30} "+
				"ON MATCH SET original_tweet.retweetcount={26}, original_tweet.likecount={27} \n";
		String retweetToTweet = "CREATE (t)-[:RETWEETS]->(original_tweet) \n";
		String tweetToProgram = "MERGE (tv:TV_Program{name:'"+topic+"'})"+
								"\nCREATE (t)-[:ABOUT]->(tv)"+
								"\nCREATE (original_tweet)-[:ABOUT]->(tv)";
		
		int cont = 31;
		String hashtag = "";
		
		for(HashtagEntity h : status.getHashtagEntities()){
			hashtag +=	"\nMERGE (h"+cont+":Hashtag{text:'"+h.getText().toLowerCase()+"'}) "
						+"CREATE (t)-[:TAGS]->(h"+cont+") \n";
			cont++;
		}
		
		String mentions = "";
		
		String screen_names = "";
		ResponseList<User> users = null;
		for(UserMentionEntity ume : status.getUserMentionEntities())
			screen_names += ume.getScreenName()+",";
		if(screen_names.length()>1){
			screen_names = screen_names.substring(0, screen_names.length()-1);
			try{
				users = twitter.lookupUsers(screen_names);
			}
			catch(TwitterException e){
				screen_names="";
			}
			
		}
		
		int j = 80;
			if(screen_names.length()>1)
				for(User u : users){
					String usersName = "";
						usersName = u.getName().replaceAll("'", " ").replaceAll("'", " ").replaceAll("\\\\", " ").replaceAll("'", " ");
					String usersLocation = "";
						usersLocation = u.getLocation().replaceAll("'", " ").replaceAll("'", " ").replaceAll("\\\\", " ").replaceAll("'", " ");
					mentions += 	"\nMERGE (u"+j+":User{user_id:"+u.getId()+"})"+
									"\nSET u"+j+".name='"+usersName+"', u"+j+".screen_name='"+u.getScreenName()+"', u"+j+".followers="+u.getFollowersCount()+", u"+j+".following="+u.getFriendsCount()+", u"+j+".location='"+usersLocation+"', u"+j+".profilePic='"+u.getBiggerProfileImageURL()+"'"+
									"\nCREATE (u"+j+")<-[:MENTIONS]-(t)";
					j++;
			}
		
		
		//RETWEET
		String text, location = null, created_at, language;
		long tweet_id;
		int likecount, retweetcount;
		
		//RETWEETER
		String name, screen_name, profilePic, userLocation = null;
		long user_id;
		int followers, following;
		
		//ORIGINAL
		String o_text, o_location = null, o_created_at, o_language;
		long o_tweet_id;
		int o_likecount, o_retweetcount;
		
		//AUTHOR
		String a_name, a_screen_name, a_profilePic, a_userLocation = null;
		long a_user_id;
		int a_followers, a_following;
		
		
		//RETWEET
		created_at = DateManager.dateToString(status.getCreatedAt()); 
		
		if(status.getPlace()!=null)
				location = status.getPlace().getName().replaceAll("'", " ").replaceAll("'", " ").replaceAll("\\\\", " ").replaceAll("'", " ")+", "+status.getPlace().getCountry();
		//RETWEETER
		User author = status.getUser();
		if(author.getLocation()!=null)
				userLocation = author.getLocation().replaceAll("'", " ").replaceAll("'", " ").replaceAll("\\\\", " ").replaceAll("'", " ");
		
		//ORIGINAL
		o_text = status.getRetweetedStatus().getText().replaceAll("'", " ").replaceAll("'", " ").replaceAll("\\\\", " ").replaceAll("'", " "); o_tweet_id = status.getRetweetedStatus().getId(); o_created_at = DateManager.dateToString(status.getCreatedAt());  o_language = status.getRetweetedStatus().getLang();
		o_likecount = status.getRetweetedStatus().getFavoriteCount(); o_retweetcount = status.getRetweetedStatus().getRetweetCount();
		if(status.getRetweetedStatus().getPlace()!=null)
				o_location = status.getRetweetedStatus().getPlace().getName().replaceAll("'", " ").replaceAll("'", " ").replaceAll("\\\\", " ").replaceAll("'", " ");
			
		
		//AUTHOR
		User o_author = status.getRetweetedStatus().getUser();
			a_name = o_author.getName().replaceAll("'", " ").replaceAll("'", " ").replaceAll("\\\\", " ").replaceAll("'", " ");
		a_screen_name = o_author.getScreenName(); a_user_id = o_author.getId(); a_profilePic = o_author.getBiggerProfileImageURL();
		a_followers = o_author.getFollowersCount(); a_following = o_author.getFriendsCount();
		if(o_author.getLocation()!=null)
				a_userLocation = o_author.getLocation().replaceAll("'", " ").replaceAll("'", " ").replaceAll("\\\\", " ").replaceAll("'", " ");
		
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("1", author.getId()); parameters.put("2", author.getName().replaceAll("'", " ").replaceAll("'", " ").replaceAll("\\\\", " ").replaceAll("'", " ")); parameters.put("3", author.getScreenName()); parameters.put("4", author.getFollowersCount()); parameters.put("5", author.getFriendsCount());
		parameters.put("6", userLocation); parameters.put("7", author.getBiggerProfileImageURL()); parameters.put("15", status.getText().replaceAll("'", " ").replaceAll("'", " ").replaceAll("\\\\", " ").replaceAll("'", " ")); parameters.put("16", status.getId()); parameters.put("17", location);
		parameters.put("18", status.getRetweetCount()); parameters.put("19", status.getFavoriteCount()); parameters.put("20", status.getLang()); parameters.put("21", topic); parameters.put("22", created_at); 
		parameters.put("8", a_user_id); parameters.put("9", a_name); parameters.put("10", a_screen_name); parameters.put("11", a_followers); parameters.put("12", a_following);
		parameters.put("13", a_userLocation); parameters.put("14", a_profilePic); 
		parameters.put("24", o_text); parameters.put("23", o_tweet_id); parameters.put("25", o_location); parameters.put("26", o_retweetcount); parameters.put("27", o_likecount); parameters.put("28", o_language); parameters.put("29", topic); parameters.put("30", o_created_at);
		cont = 16;
		
		String query = user+'\n'+original_user+'\n'+retweet+'\n'+original_tweet+'\n'+retweetToTweet+'\n'+hashtag+'\n'+tweetToProgram+'\n'+mentions;
		
		session.run(query, parameters);
		
		contatore++;
		if(contatore%50==0)
			System.out.println("Tweet ricevuti: "+contatore);
		
			
		
	}
	

	
	
	public static void main(String[] args) throws Exception {
		
		final LinkedBlockingQueue<Status> queue = new LinkedBlockingQueue<Status>(1000);
		startRun = System.currentTimeMillis();
		contatore = 1;
		cont_test=1;
		media = 0;
		
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		Date startTime = new Date();
		System.out.println("deprecated launch time: "+df.format(startTime));
		
	//	String startTime = DateManager.getCurrentDate();
	//	System.out.println(startTime);

		String [] arguments  = readTwitterAuth();

		cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true).setOAuthConsumerKey(arguments[0]).setOAuthConsumerSecret(arguments[1]).setOAuthAccessToken(arguments[2]).setOAuthAccessTokenSecret(arguments[3]);
		/*
	//	apriConnessione();
		init();*/
		openConnection();
	//	session.run("CREATE CONSTRAINT ON (n:Tweet) ASSERT n.tweet_id IS UNIQUE");
		session.run("CREATE CONSTRAINT ON (n:User) ASSERT n.user_id IS UNIQUE");
		session.run("CREATE CONSTRAINT ON (n:Hashtag) ASSERT n.text IS UNIQUE");
		session.run("CREATE CONSTRAINT ON (n:Source) ASSERT n.name IS UNIQUE");
		session.run("CREATE CONSTRAINT ON (n:TV_Program) ASSERT n.name IS UNIQUE");
		Configuration config = cb.build();
	//	twitter = new TwitterFactory(cb.build()).getInstance();
		twitter = new TwitterFactory(config).getInstance();
	//	TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
		TwitterStream twitterStream = new TwitterStreamFactory(config).getInstance();
		StatusListener listener = new StatusListener() {
		
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
				if(System.currentTimeMillis()-lastLimitation>=30000){
					System.out.println();
					System.out.println("Got track limitation notice: " + numberOfLimitedStatuses);
					System.out.println();
					lastLimitation=System.currentTimeMillis();
				}
					
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

		FilterQuery query = new FilterQuery();
		
		List<Object> params = insertMultipleValues();
		String [] lang = (String[]) params.get(1);
		String [] keyWords = (String[]) params.get(0);
		String topic = (String) params.get(2);
		query.track(keyWords);
		if(lang.length>0)
			query = importLanguagesInQuery(query, lang);
		
		twitterStream.addListener(listener);
		
		twitterStream.filter(query);
		
		@SuppressWarnings("unused")
		int nTweets = 0;
		
		
		long lastUpdate = System.currentTimeMillis();
		int updateTime = 20000;
		int lastCount = 0;
		
		while (true) {
			Status status = queue.poll();
			
			if (status == null) {
				Thread.sleep(100);
			} 
			
			else{
				if(status.isRetweet()){
					long diff = startTime.getTime()-status.getRetweetedStatus().getCreatedAt().getTime();
					if(diff<0)
						insertData(status, topic);
				}
				else
					insertData(status, topic);
			}
			
			
			if(System.currentTimeMillis()-lastUpdate>=updateTime){
				System.out.println("Ritmo di acquisizione: "+(double) (contatore-lastCount)/((System.currentTimeMillis()-lastUpdate)/1000)+" T/s negli ultimi "+updateTime/1000+" secondi");
				lastUpdate = System.currentTimeMillis();
				lastCount=contatore;
			}
	}

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
	
	
	public static String [] readTwitterAuth(String file) throws FileNotFoundException{
		String [] output = new String[4];
		Scanner sc = new Scanner(new File(file));
		int count = 0;
		while(sc.hasNextLine()){
			output[count++] = sc.nextLine();
		}
		sc.close();
		return output;
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
		 String [] options = {"(default)", "2", "3"};
		 int scelta = JOptionPane.showOptionDialog(null, message, "Credenziali", 2, 0, icon, options, options[0]);
		 if(scelta==0)
			 return readTwitterAuth("config/credenziali_twitter.txt");
		 if(scelta==1)
			 return readTwitterAuth("config/credenziali_twitter2.txt");
		 if(scelta==2)
			 return readTwitterAuth("config/credenziali_twitter3.txt");
		 return null;
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
		   return output;
	}
	
	private static int noParamsChosen() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException{
		JLabel label2 = new JLabel("<html>Parametri non inseriti correttamente.<br>Riprovare?</html>");
		label2.setFont(new Font("Calibri", Font.BOLD, 20));
		String [] errorOptions = {"Riprova", "Esci"};
		return JOptionPane.showOptionDialog(null, label2, "Topic", 0, 0, icon, errorOptions, "Riprova");
		}
	
	public void chiudiConnessione() throws SQLException{
		c.close();
	}

	@SuppressWarnings("unused")
	private static void init() throws SQLException{
		
		
		psCreateIndexTweet = c.prepareStatement(cqlCreateIndexTweet);
		psCreateIndexHT = c.prepareStatement(cqlCreateIndexHT);
		psCreateIndexTweet.executeQuery();
		psCreateIndexHT.executeQuery();
	}

	
}