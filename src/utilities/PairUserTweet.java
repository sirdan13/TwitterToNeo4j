package utilities;

public class PairUserTweet {

	private User u;
    private Tweet t;
    public PairUserTweet(User u, Tweet t){
        this.u = u;
        this.t = t;
    }
    public User getUser(){ return u; }
    public Tweet getTweet(){ return t; }
    public void setUser(User u){ this.u = u; }
    public void setTweet(Tweet t){ this.t = t; }
}
