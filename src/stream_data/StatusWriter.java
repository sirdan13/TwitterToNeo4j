package stream_data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;

import twitter4j.DirectMessage;
import twitter4j.FilterQuery;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class StatusWriter {
	
	static StatusListener listener;
	static Twitter twitter;
	static ConfigurationBuilder cb;
	static List<String> urls;
	static String topic;
	static int counter = 1;
	public static void main(String[] args) throws InterruptedException, TwitterException, IOException, ParseException{

		LinkedBlockingQueue<Status> queue = new LinkedBlockingQueue<Status>(100000);
		File statusesFolder = new File("statuses/");
		if(!statusesFolder.exists())
			statusesFolder.mkdir();
		topic = JOptionPane.showInputDialog("Input the topic:");
		int batchSize = Integer.parseInt(JOptionPane.showInputDialog("Input the batch size: "));
		setCredentials(readTwitterAuth("config/oauth.txt"));
		Configuration config = cb.build();
		TwitterFactory tf = new TwitterFactory(config);
		twitter = tf.getInstance();
		TwitterStream twitterStream = new TwitterStreamFactory(config).getInstance();
		FilterQuery query = new FilterQuery();
		urls = new ArrayList<>();
		String [] keywords = importKeywords("config/keywords.txt");
		String [] languages = importKeywords("config/lang.txt");
		//long[] users = importUsers("config/users.txt");
		query.track(keywords);
		//query.follow(users);
		if(!languages[0].equals("none"))
			query.language(languages);
		listener = getListener(queue);
		twitterStream.addListener(listener);
		twitterStream.filter(query);
		List<Status> statusList = new ArrayList<>();
		while(true){
			Status status = queue.poll();
			if(status==null)
				Thread.sleep(100);
			else{
				statusList.add(status);
				if(statusList.size()>=batchSize){
					writeFile(statusList);
					System.out.println("Current status created_at:\t"+status.getCreatedAt());
					System.out.println();
					statusList.clear();
				}
					
			}
		}
		
	}
	
	private static String [] readTwitterAuth(String file){
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
	
	
	private static void writeFile(List<Status> statusList) throws IOException, ParseException{
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		File folder = new File("statuses/"+topic);
		if(!folder.exists())
			folder.mkdirs();
		File filename = new File(folder+"/"+System.currentTimeMillis()+".ser");
		fos = new FileOutputStream(filename);
		out = new ObjectOutputStream(fos);
		out.writeObject(statusList);
		out.close();
		fos.close();
		System.out.println(statusList.size()*(counter++)+" tweets stored.");
	}
	
	
	//old method
	/*
	private static void writeFile(List<Status> statusList) throws IOException, ParseException{
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		String date = getCurrentDate().substring(0, 10);
		File folder = new File("statuses/"+topic+"/"+date+"/"+getCurrentDate().substring(11, 16).replaceAll(":", ".")+".00");
		if(!folder.exists())
			folder.mkdirs();
		String filename = folder+"/"+UUID.randomUUID().toString()+".ser";
		fos = new FileOutputStream(filename);
		out = new ObjectOutputStream(fos);
		out.writeObject(statusList);
		out.close();
		fos.close();
		System.out.println(statusList.size()*(counter++)+" tweets stored.");
	}*/
			
	public static String getCurrentDate() throws ParseException{
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		Date date = new Date();
		return dateFormat.format(date);
	}
		
	
	private static String[] importKeywords(String filePath) throws FileNotFoundException {
		
		List<String> list = new ArrayList<>();
		Scanner sc = new Scanner(new File(filePath));
		while(sc.hasNextLine())
			list.add(sc.nextLine());
		String [] keywords = new String[list.size()];
		for(int i = 0;i<list.size();i++)
			keywords[i]=list.get(i);
		sc.close();
		return keywords;
	}
	
	@SuppressWarnings("unused")
	private static long[] importUsers(String filePath) throws FileNotFoundException {
		
		List<Long> list = new ArrayList<>();
		Scanner sc = new Scanner(new File(filePath));
		while(sc.hasNextLine())
			list.add(Long.valueOf(sc.nextLine()));
		long [] users = new long[list.size()];
		for(int i = 0;i<list.size();i++)
			users[i]=list.get(i);
		sc.close();
		return users;
	}

	
	@SuppressWarnings({ "unused", "unchecked", "rawtypes" })
	private static void loadTweets() {
		String filename="tweets.ser";
		FileInputStream fis = null;
		ObjectInputStream in = null;
		List<Status> list = new ArrayList<>();
		try{
			System.out.println("Start loading");
			fis = new FileInputStream(filename);
			in = new ObjectInputStream(fis);
			list = (ArrayList) in.readObject();
			in.close();
			System.out.println("Loading completed");
			
		}
		catch(IOException | ClassNotFoundException ex){
			ex.printStackTrace();
		}
		int contatore = 1;
		for(Status s : list){
			System.out.println("#"+contatore+"\t"+s.getUser().getName());
			contatore++;
		}
			
		
	}

	private static void setCredentials(String [] credentials){

		cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true).setOAuthConsumerKey(credentials[0]).setOAuthConsumerSecret(credentials[1]).setOAuthAccessToken(credentials[2]).setOAuthAccessTokenSecret(credentials[3]).setJSONStoreEnabled(true);;

	}
	
	public static StatusListener getListener(LinkedBlockingQueue<Status> queue) {
		return listener = new StatusListener() {
			
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


	
	public static String createTweet(String tweet) throws TwitterException {
		//Twitter twitter = getTwitterinstance();
		Status status = twitter.updateStatus(tweet);
	    return status.getText();
	}
	
	public static List<String> getTimeLine() throws TwitterException {
		List<Status> statuses = twitter.getHomeTimeline();
		return statuses.stream().map(
				item -> item.getText()).collect(
						Collectors.toList());
	}
	
	public static String sendDirectMessage(String recipientName, String msg) throws TwitterException {
	        DirectMessage message = twitter.sendDirectMessage(recipientName, msg);
	        return message.getText();
	}
	
	public static List<String> searchtweets() throws TwitterException {
	        Query query = new Query("source:twitter4j baeldung");
	        QueryResult result = twitter.search(query);
	        List<Status> statuses = result.getTweets();
	        return statuses.stream().map(
				item -> item.getText()).collect(
						Collectors.toList());
	}
	
	public static void streamFeed() {
		
		StatusListener listener = new StatusListener(){

			@Override
			public void onException(Exception e) {
				e.printStackTrace();
			}

			@Override
			public void onDeletionNotice(StatusDeletionNotice arg) {
                                System.out.println("Got a status deletion notice id:" + arg.getStatusId());
			}

			@Override
			public void onScrubGeo(long userId, long upToStatusId) {
                                System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
			}

			@Override
			public void onStallWarning(StallWarning warning) {
                                System.out.println("Got stall warning:" + warning);
			}

			@Override
			public void onStatus(Status status) {
                                System.out.println(status.getUser().getName() + " : " + status.getText());
			}

			@Override
			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                                System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
			}
		};
	
		TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
		
	        twitterStream.addListener(listener);
	    
	        twitterStream.sample();
		
	}
	
}