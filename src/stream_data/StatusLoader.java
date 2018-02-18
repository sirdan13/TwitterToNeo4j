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

public class StatusLoader {
	
	static List<Status> statusList;
	static Session session;
	static String topic;

	public static void main(String[] args) {
		statusList = new ArrayList<>();
		File statusesFolder = new File(JOptionPane.showInputDialog("Insert location of statuses folder: "));
		topic = JOptionPane.showInputDialog("Insert topic name:");
		GraphDBManager gdbm = new GraphDBManager();
		session = gdbm.getSession();
	/*	session.run("CREATE CONSTRAINT ON (source:Source) ASSERT source.application IS UNIQUE");
		session.run("CREATE CONSTRAINT ON (n:Tweet) ASSERT n.tweet_id IS UNIQUE");
		session.run("CREATE CONSTRAINT ON (n:User) ASSERT n.user_id IS UNIQUE");*/
	/*	topic = (String) JOptionPane.showInputDialog(null, "Choose a topic:", "Twitter", 0, null, statusesFolder.list(), statusesFolder.list()[0]);
		File topicFolder = new File(statusesFolder+"/"+topic);
		String [] folders = topicFolder.list();
		Arrays.sort(folders);
		String date = (String) JOptionPane.showInputDialog(null, "Choose a starting date:", "Twitter", 0, null, folders, folders);
		int separator = 0;
		for(int i = 0;i<folders.length;i++)
			if(date.equals(folders[i]))
				separator = i;
		List<File> files = new ArrayList<>();
		for(int i = 0; i<topicFolder.listFiles().length; i++)
			if(i>=separator)
				files.add(topicFolder.listFiles()[i]);
		for(File f : files){
			File [] hourFiles = f.listFiles();
			for(File f1 : hourFiles){
				for(File f2 : f1.listFiles()){
					loadTweets(f2.getAbsolutePath());
				}
			}
		}
*/
		
		String [] fileNames = statusesFolder.list();
		//System.out.println(statusesFolder);
		//System.exit(0);
		Arrays.sort(fileNames);
		List<File> files = new ArrayList<>();
		System.out.println("Loading files...");
		for(int i = 0;i<fileNames.length;i++)
			files.add(new File(fileNames[i]));
		System.out.println("Files loaded.");
		System.out.println("Loading into db...");
		double counter = 0;
		for(File f : files){
		//	System.out.println(statusesFolder+"\\"+f.getName());
			loadTweets(statusesFolder+"\\"+f.getName());
			counter++;
			System.out.println((int)counter+"/"+files.size()+" files processed ("+counter/(double) (files.size())*100+" %)");
		}
			
		
	}
	
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
		int contatore = 1;
		for(Status s : statusList){
			if(s!=null){
				if(s.isRetweet()){
					TwitterManager.insertRetweet(session, topic, s);
					contatore++;
				}
					
				else
					TwitterManager.insertTweet(session, topic, s);
				contatore++;
				
				if(contatore%500==0 && contatore>0)
					System.out.println(contatore+" tweet caricati.");
			}
		}
		statusList.clear();
			
		
	}

}
