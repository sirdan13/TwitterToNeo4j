package stream_data;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.UnsupportedLookAndFeelException;
import org.neo4j.driver.v1.Session;

import twitter4j.FilterQuery;
import twitter4j.Status;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.Configuration;
import utilities.Launcher;
import utilities.Utilities;


public class DataDownloader {
	
	static Session session;
	private static String about;
	static int contatore;
	static LinkedBlockingQueue<Status> queue;
	static Date startDate;
	static boolean checkTime;
	static long lastUpdate;
	static int lastBunch = 0;
	
	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, FileNotFoundException, InterruptedException, ParseException{
		

		try {
            Runtime.getRuntime().exec("cmd /c start cmd.exe /K \" cd C:\\Users\\daniele\\Desktop\\Tesi\\Eseguibili && java -jar Lookup.jar");
        } catch (IOException ex) {
            Logger.getLogger(Launcher.class.getName()).log(Level.SEVERE, null, ex);
        }
		
		try {
            Runtime.getRuntime().exec("cmd /c start cmd.exe /K \" cd C:\\Users\\daniele\\Desktop\\Tesi\\Eseguibili && java -jar SentimentSetup.jar");
        } catch (IOException ex) {
            Logger.getLogger(Launcher.class.getName()).log(Level.SEVERE, null, ex);
        }
		
			startDate = Utilities.getCurrentDate();
			queue = new LinkedBlockingQueue<Status>(100000);
			lastUpdate = System.currentTimeMillis();
			GraphDBManager gdbm = new GraphDBManager();
			session = gdbm.getSession();
			TwitterManager tm = new TwitterManager(queue);
			Configuration config = tm.getConfig();
			TwitterStream twitterStream = new TwitterStreamFactory(config).getInstance();
			StatusListener listener = tm.getListener();
			FilterQuery query = tm.setQueryParameters();
			about = TwitterManager.getTopic();
			if(tm.getTimeFilter().equals("true"))
				checkTime = true;
			else
				checkTime = false;

			twitterStream.addListener(listener);
			
			twitterStream.filter(query);
			contatore=0;
			long lastUpdate = System.currentTimeMillis();
			session.run("CREATE CONSTRAINT ON (source:Source) ASSERT source.application IS UNIQUE");
			session.run("CREATE CONSTRAINT ON (n:Tweet) ASSERT n.tweet_id IS UNIQUE");
			session.run("CREATE CONSTRAINT ON (n:User) ASSERT n.user_id IS UNIQUE");
			
			double time_avg = 0;
			
			if(checkTime){
				while(true){
					/*
					Status status = queue.poll();
					if (status == null) {
						Thread.sleep(100);
					} */
					
					long start = System.currentTimeMillis();
					
					Status status = queue.take();
					managePresentStatus(status);
					
					long end = System.currentTimeMillis();
					long diff = (end-start);
					if(time_avg==0)
						time_avg=diff/contatore;
					else
						time_avg = (time_avg*(contatore-1)+(diff))/contatore;
					if(contatore%10==0 && contatore>0)
						System.out.println(contatore+" tweet caricati;"+time_avg);
					/*
					if(System.currentTimeMillis()-lastUpdate>=10000)
						printPace();*/
				}
			}
			else{
				while(true){
					
					/*
					Status status = queue.poll();
					if (status == null) {
						Thread.sleep(100);
					} */
					long start = System.currentTimeMillis();
					
					Status status = queue.take();
					manageAllStatus(status);
					
					long end = System.currentTimeMillis();
					long diff = (end-start);
					if(time_avg==0)
						time_avg=diff/contatore;
					else
						time_avg = (time_avg*(contatore-1)+(diff))/contatore;
					if(contatore%10==0 && contatore>0)
						System.out.println(contatore+" tweet caricati;"+time_avg);
					
					/*
					if(System.currentTimeMillis()-lastUpdate>=10000)
						printPace();*/
			}
		}
	}
	
	private static void printPace(){
		long diff = System.currentTimeMillis()-lastUpdate;
		double diffTweets = contatore-lastBunch;
		double pace = diffTweets/(diff/1000);
		System.out.println();
		System.out.println("Ritmo: "+pace+" T/s");
		System.out.println();
		lastUpdate=System.currentTimeMillis();
		lastBunch=contatore;
	}
			
	private static void managePresentStatus(Status status) throws InterruptedException{
		
		if (status == null) {
			return;
		} 
		
		if(status!=null && TwitterManager.checkTime(startDate, status, checkTime)){
			if(status.isRetweet()){
				TwitterManager.insertRetweet(session, about, status);
				contatore++;
			}
				
			else
				TwitterManager.insertTweet(session, about, status);
			contatore++;
			
			if(contatore%500==0 && contatore>0)
			System.out.println(contatore+" tweet ricevuti.");
		}
	}
	
	private static void manageAllStatus(Status status) throws InterruptedException{
		
		if (status == null) {
			return;
		} 
		
		if(status!=null){
			if(status.isRetweet()){
				TwitterManager.insertRetweet(session, about, status);
				contatore++;
			}
				
			else
				TwitterManager.insertTweet(session, about, status);
			contatore++;
			
			if(contatore%500==0 && contatore>0)
			System.out.println(contatore+" tweet ricevuti.");
		}
	}
	
	

}