package stream_data;
import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.neo4j.driver.v1.Session;

import twitter4j.Status;

public class StatusToDB {
	
	static List<Status> statusList;
	static Session session;
	static String topic;
	static int contatore = 0;
	static double time_avg = 0;

	public static void main(String[] args) {
		statusList = new ArrayList<>();
		File statusesFolder = new File(JOptionPane.showInputDialog("Insert location of statuses folder: ")+"/statuses/");
		GraphDBManager gdbm = new GraphDBManager();
		session = gdbm.getSession();
		session.run("CREATE CONSTRAINT ON (source:Source) ASSERT source.application IS UNIQUE");
		session.run("CREATE CONSTRAINT ON (n:Tweet) ASSERT n.tweet_id IS UNIQUE");
		session.run("CREATE CONSTRAINT ON (n:User) ASSERT n.user_id IS UNIQUE");
		topic = (String) JOptionPane.showInputDialog(null, "Choose a topic:", "Twitter", 0, null, statusesFolder.list(), statusesFolder.list()[0]);
		File topicFolder = new File(statusesFolder+"/"+topic);
		File [] files = topicFolder.listFiles();
		int separator = 0;
		for(int i = 0;i<files.length;i++)
			loadTweets(files[i].getAbsolutePath());
				
			
		}

	
	
	@SuppressWarnings("unused")
	private static void loadTweets(String filename) {
		
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try{
			//System.out.println("Start loading");
			fis = new FileInputStream(filename);
			in = new ObjectInputStream(fis);
			statusList = (ArrayList) in.readObject();
			in.close();
			//System.out.println("Loading completed");
			
		}
		catch(IOException | ClassNotFoundException ex){
			ex.printStackTrace();
		}
		
		for(Status s : statusList){
			if(s!=null){
				long start = System.currentTimeMillis();
				if(s.isRetweet()){
					TwitterManager.insertRetweet(session, topic, s);
					contatore++;
				}
					
				else
					TwitterManager.insertTweet(session, topic, s);
				contatore++;
				long end = System.currentTimeMillis();
				long diff = (end-start);
				if(time_avg==0)
					time_avg=diff/contatore;
				else
					time_avg = (time_avg*(contatore-1)+(diff))/contatore;
				if(contatore%10==0 && contatore>0)
					System.out.println(contatore+" tweet caricati;"+time_avg);
			}
		}
		statusList.clear();
			
		
	}

}
