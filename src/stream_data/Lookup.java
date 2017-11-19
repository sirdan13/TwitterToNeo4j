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
		gdbm = tm.getGdbm();
		while(true){
			if((System.currentTimeMillis()-startTime)>5000 || counter==0){
				
				startTime=System.currentTimeMillis();
				lookupUsers(tm);
				lookupTweets(tm);
				counter++;
				System.out.println();
				System.out.println("Next update in 5 seconds");
				System.out.println();
			}
				
				
		}
			
	}
	
	private static void lookupTweets(TwitterManager tm) {
		long[] array = TwitterManager.extractTweets(gdbm);
		if(array.length>0 && array!=null){
			Set<Status> statusSet = TwitterManager.lookupTweets(array);
			if(statusSet!=null){
				//Completing the info about the extracted tweets
				for(Status s : statusSet){
					if(s!=null)
						TwitterManager.fillUpStatus(s);
				}
			}
			
				
		}
		
	}

	public static void lookupUsers(TwitterManager tm){
		//Extracting users which have no other info but the user_id
		long[] array = TwitterManager.extractUsers(gdbm);
		if(array.length>0 && array!=null){
			Set<User> userSet = TwitterManager.lookupUsers(array);
			if(userSet!=null){
				for(User u : userSet)
				TwitterManager.fillUpUser(u);
			}
			//Completing the info about the extracted users
			
		}
			
		
		
	}

}
	

