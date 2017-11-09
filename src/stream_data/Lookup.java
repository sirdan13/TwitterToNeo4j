package stream_data;

import java.util.Set;

import twitter4j.User;

public class Lookup {

	public static void main(String[] args) {
		
		//Extracting users which have no other info but the user_id
		long[] array = TwitterManager.extractUsers();
		Set<User> userSet = TwitterManager.lookupUsers(array);
		TwitterManager tm = new TwitterManager();
		//Completing the info about those users
		for(User u : userSet)
			tm.fillUpUser(u);
		
	}

}
