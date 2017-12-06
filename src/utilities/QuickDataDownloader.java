/**
 * 
 */
package utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;


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

public class QuickDataDownloader {
	
	static StatusListener listener;
	static Twitter twitter;
	static ConfigurationBuilder cb;
	
	public static void main(String[] args) throws InterruptedException, TwitterException{

	/*	loadTweets();
		System.exit(0);*/
		LinkedBlockingQueue<Status> queue = new LinkedBlockingQueue<Status>(100000);
		setCredentials(args);
		Configuration config = cb.build();
		twitter = new TwitterFactory(config).getInstance();
		
		TwitterStream twitterStream = new TwitterStreamFactory(config).getInstance();
		FilterQuery query = new FilterQuery();
		String topic = "trump";
		query.track("trump", "nba", "championsleague", "sport", "soccer", "football", "december6th", "6dicembre", "6decembre");
		listener = getListener(queue);
		twitterStream.addListener(listener);
		twitterStream.filter(query);
		
		List<Status> list = new ArrayList<>();
		int tweetsDownloaded = 0;
		
		while(true){
			Status status = queue.poll();
			
			if (status == null) {
				Thread.sleep(100);
			}
			
			if(status!=null){
				list.add(status);
			}
			
			if(list.size()==50){
				FileOutputStream fos = null;
				ObjectOutputStream out = null;
				try {
					String uuid = UUID.randomUUID().toString();
					int folder = (int) (Math.random()*10);
					
					String filename = "C:/users/daniele/desktop/tesi/eseguibili/statuses/"+folder+"/"+topic+"-"+uuid+".ser";
					fos = new FileOutputStream(filename);
					out = new ObjectOutputStream(fos);
					out.writeObject(list);
					tweetsDownloaded += list.size();
					if(tweetsDownloaded % 100==0)
						System.out.println(tweetsDownloaded+" tweets downloaded");
				//	System.out.println("File: "+filename+" written.");
					out.close();
					fos.close();
				} catch (IOException e) {
					
					e.printStackTrace();
				}
				
				list.clear();
		}
				
	}
		
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