package utilities;

public class User {
	
	private long nodeID;
	private long userID;
	private String name;
	private String screen_name;
	private String location;
	private String description;
	private String profileImage;
	private boolean verified;
	private int followers;
	private int following;
	
	public User(long nodeID, long userID, String name,String screen_name,String location,String description,String profileImage,boolean verified,int followers, int following){
		this.nodeID=nodeID;
		this.userID=userID;
		this.name=name;
		this.screen_name=screen_name;
		this.location=location;
		this.description=description;
		this.profileImage=profileImage;
		this.verified=verified;
		this.followers=followers;
		this.following=following;
	}

	public long getNodeID() {
		return nodeID;
	}

	public void setNodeID(long nodeID) {
		this.nodeID = nodeID;
	}

	public long getUserID() {
		return userID;
	}

	public void setUserID(long userID) {
		this.userID = userID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getScreen_name() {
		return screen_name;
	}

	public void setScreen_name(String screen_name) {
		this.screen_name = screen_name;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getProfileImage() {
		return profileImage;
	}

	public void setProfileImage(String profileImage) {
		this.profileImage = profileImage;
	}

	public boolean isVerified() {
		return verified;
	}

	public void setVerified(boolean verified) {
		this.verified = verified;
	}

	public int getFollowers() {
		return followers;
	}

	public void setFollowers(int followers) {
		this.followers = followers;
	}

	public int getFollowing() {
		return following;
	}

	public void setFollowing(int following) {
		this.following = following;
	}
	
	
	

}
