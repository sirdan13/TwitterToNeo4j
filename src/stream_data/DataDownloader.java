package stream_data;

import java.io.FileNotFoundException;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.UnsupportedLookAndFeelException;
import org.neo4j.driver.v1.Session;
import twitter4j.FilterQuery;
import twitter4j.Status;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.Configuration;


public class DataDownloader {
	
	static Session session;
	private static String about;
	
	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, FileNotFoundException, InterruptedException{
		

			final LinkedBlockingQueue<Status> queue = new LinkedBlockingQueue<Status>(1000);
			
			GraphDBManager gdbm = new GraphDBManager();
			session = gdbm.getSession();
			TwitterManager tm = new TwitterManager(queue);
			Configuration config = tm.getConfig();
			TwitterStream twitterStream = new TwitterStreamFactory(config).getInstance();
			StatusListener listener = tm.getListener();
			FilterQuery query = tm.setQueryParameters();
			about = TwitterManager.getTopic();

			twitterStream.addListener(listener);
			
			twitterStream.filter(query);
			
			int contatore=0;
			long lastUpdate = System.currentTimeMillis();
			int lastBunch = 0;
			while(true){
				
				Status status = queue.poll();
				
				if (status == null) {
					Thread.sleep(100);
				} 
				
				
				
				if(status!=null){
					if(status.isRetweet()){
						TwitterManager.insertRetweet(session, about, status);
						contatore++;
					}
						
					else
						TwitterManager.insertTweet(session, about, status);
					contatore++;
				}
				
				
				
				if(contatore%500==0 && contatore>0)
					System.out.println(contatore+" tweet ricevuti.");
				
				if(System.currentTimeMillis()-lastUpdate>=10000){
					long diff = System.currentTimeMillis()-lastUpdate;
					int diffTweets = contatore-lastBunch;
					double pace = diffTweets/(diff/1000);
					System.out.println();
					System.out.println("Ritmo: "+pace+" T/s");
					System.out.println();
					lastUpdate=System.currentTimeMillis();
					lastBunch=contatore;
					
				}
					
			}
			
	}

	

	
	

}
