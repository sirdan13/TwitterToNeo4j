package utilities;

import java.util.List;
import java.util.Map;

public class Subject {
	
	private String screen_name;
	private String name;
	private List<HashtagAndFreq> hashtagsAndFreq;
	private String profilePicURL;
	
	public Subject(String name, String screen_name, String profilePicURL){
		this.name=name;
		this.screen_name=screen_name;
		this.profilePicURL=profilePicURL;
	}
	
	public String getScreen_name() {
		return screen_name;
	}
	public void setScreen_name(String screen_name) {
		this.screen_name = screen_name;
	}
	
	
	public List<HashtagAndFreq> getHashtagsAndFreq() {
		return hashtagsAndFreq;
	}

	public void setHashtagsAndFreq(List<HashtagAndFreq> hashtagsAndFreq) {
		this.hashtagsAndFreq = hashtagsAndFreq;
	}

	public String getProfilePicURL() {
		return profilePicURL;
	}
	public void setProfilePicURL(String profilePicURL) {
		this.profilePicURL = profilePicURL;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
