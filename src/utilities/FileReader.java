package utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.swing.JOptionPane;

import org.neo4j.driver.v1.Session;

import stream_data.GraphDBManager;
import stream_data.TwitterManager;
import twitter4j.Status;

public class FileReader {
	
	static List<Status> statusList;
	static int tweetsLoaded = 0;
	static GraphDBManager gdbm;
	static Session session;
	static String topic;
	static long lastUpdate;

	public static void main(String[] args) {
		int folderChoice = Integer.parseInt(args[0]);
		File folder = new File("C:/users/daniele/desktop/tesi/eseguibili/statuses/"+folderChoice+"/");
		gdbm = new GraphDBManager();
		session = gdbm.getSession();
		statusList = new ArrayList<Status>();
		lastUpdate = System.currentTimeMillis();
		while(true){
			
			File[] files = folder.listFiles();
			if(files != null){
				Arrays.sort(files, new Comparator<File>() {
							public int compare(File f1, File f2) {
									return Long.compare(f1.lastModified(), f2.lastModified());
							}
				});
				//Here we manage each single file, from data-load until the deletion
				for(int i = 0; i<files.length; i++){
					if(files[i].exists()){
						if(System.currentTimeMillis()-files[i].lastModified()<1000){
							try {
								Thread.sleep(50);
							} catch (InterruptedException e) {
								
								e.printStackTrace();
							}
						}
						loadTweets(files[i]);
						//TODO manageStatuses
						files[i].delete();
						statusList.clear();;
					}
					
				}
				
			}
		
		}
		
	}
	
	
	@SuppressWarnings("unchecked")
	private static void loadTweets(File file) {
		
		topic = file.getName().split("-")[0];
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try{
			fis = new FileInputStream(file.getAbsolutePath());
			in = new ObjectInputStream(fis);
			statusList = (List<Status>) in.readObject();
			if(statusList.size()>0)
				tweetsLoaded+=statusList.size();
			for(Status s : statusList)
				storeTweet(s);
			if(tweetsLoaded%100==0){
				long diff = (System.currentTimeMillis()-lastUpdate)/1000;
				double pace = 100.0/diff;
				System.out.println(tweetsLoaded+" tweets loaded. Pace: "+pace+" T/s");
				lastUpdate = System.currentTimeMillis();
			}
				
			
			in.close();
			fis.close();
		}
		catch(IOException | ClassNotFoundException ex){
			statusList.clear();
			ex.printStackTrace();
		}
			
		
	}
	
	private static void storeTweet(Status status){
		if(status.isRetweet())
			TwitterManager.insertRetweet(session, topic, status);
		else
			TwitterManager.insertTweet(session, topic, status);
	}

}
