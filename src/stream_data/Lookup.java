package stream_data;

import java.util.Set;

import twitter4j.Status;
import twitter4j.User;

public class Lookup {

	static GraphDBManager gdbm;
	
	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		TwitterManager tm = new TwitterManager();
		int counter = 0;
		gdbm = new GraphDBManager();
		while(true){
			if((System.currentTimeMillis()-startTime)>30000 || counter==0){
				
				startTime=System.currentTimeMillis();
				lookupUsers(tm);
				lookupTweets(tm);
				counter++;
				System.out.println("Task completed.");
				System.out.println("Next update in 30 seconds");
			}
				
				
		}
			
	}
	
	private static void lookupTweets(TwitterManager tm) {
		long[] array = TwitterManager.extractTweets(gdbm);
		if(array.length>0){
			Set<Status> statusSet = TwitterManager.lookupTweets(array);
			//Completing the info about the extracted tweets
			for(Status s : statusSet)
				TwitterManager.fillUpStatus(s);
		}
		
	}

	public static void lookupUsers(TwitterManager tm){
		//Extracting users which have no other info but the user_id
		long[] array = TwitterManager.extractUsers(gdbm);
		if(array.length>0){
			Set<User> userSet = TwitterManager.lookupUsers(array);
			//Completing the info about the extracted users
			for(User u : userSet)
				TwitterManager.fillUpUser(u);
		}
			
		
		
	}

}
	

