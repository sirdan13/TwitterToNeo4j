package utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import twitter4j.Status;

public class FileReader {
	
	static List<Status> statusList;
	static int tweetsLoaded = 0;

	public static void main(String[] args) {
		File folder = new File("C:/users/daniele/desktop/tesi/eseguibili/statuses/");
		statusList = new ArrayList<Status>();
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
								Thread.sleep(200);
							} catch (InterruptedException e) {
								
								e.printStackTrace();
							}
						}
						loadTweets(files[i].getAbsolutePath(), files[i]);
						//TODO manageStatuses
						files[i].delete();
						statusList.clear();;
					}
					
				}
				
			}
		
		}
		
	}
	
	
	@SuppressWarnings("unchecked")
	private static void loadTweets(String filename, File file) {
		
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try{
			fis = new FileInputStream(filename);
			in = new ObjectInputStream(fis);
			statusList = (List<Status>) in.readObject();
			if(statusList.size()>0)
				tweetsLoaded+=statusList.size();
			if(tweetsLoaded%100==0)
				System.out.println(tweetsLoaded+" tweets loaded.");
			in.close();
			fis.close();
		}
		catch(IOException | ClassNotFoundException ex){
			statusList.clear();;
			System.out.println("File length: "+file.length());
			ex.printStackTrace();
		}
			
		
	}

}
