package utilities;

import java.util.Set;

import com.vdurmont.emoji.EmojiParser;


public class Tweet {
	
	private long nodeID;
	private long tweetID;
	private String created_at;
	private String location;
	private String text;
	private String language;
	private int likecount;
	private int retweetcount;
	private String sentiment;
	
	//Array of words taken by the text, excluding non-alphanumeric symbols
	private String[] words;
	
	public Tweet(long nodeID, long tweetID, String created_at, String location, String text, String language, int likecount, int retweetcount){
		this.nodeID=nodeID;
		this.tweetID=tweetID;
		this.created_at=created_at;
		this.location=location;
		this.text=text;
		this.likecount=likecount;
		this.retweetcount=retweetcount;
	}
	
	public Tweet(long nodeIDTweet, String text) {
		this.nodeID=nodeIDTweet;
		this.text=text;
	}

	public void computeSentiment(){
		text = EmojiParser.parseToAliases(text);
		words = text.toLowerCase().split("\\s+");
		Utilities ut = new Utilities();
		Set<String> emojiLike = ut.getEmojiLike(), emojiSad = ut.getEmojiSad(), emojiHilarious = ut.getEmojiHilarious(), emojiAngry = ut.getEmojiAngry();
		Set<String> coreLike = ut.getCoreLike(), coreSad = ut.getCoreSad(), coreHilarious = ut.getCoreHilarious(), coreAngry = ut.getCoreAngry();
		Set<String> likeWords = ut.getLikeWords(), sadWords = ut.getSadWords(), hilariousWords = ut.getHilariousWords(), angryWords = ut.getAngryWords();
		
		if(checkSentiment(emojiLike, emojiSad, emojiHilarious, emojiAngry))
			return;
		if(checkSentiment(coreLike, coreSad, coreHilarious, coreAngry))
			return;
		if(checkSentiment(likeWords, sadWords, hilariousWords, angryWords))
			return;
		sentiment = "neutral";
		return;
		
		/*
		 * 1. Check emojis to set the sentiment
		 * 2. If 1. didn't succeed, check unequivocal terms
		 * 3. If 2. didn't succeed, check the entire corpus of terms 
		 * 4. If 3. didn't succeed, the text is neutral
		 */
		
		/*
		 * Checking process:
		 * 1. Method is called by passing an array containing the dictionaries
		 * 2. Whenever a term is found, a counter is increased (max. once for each sentiment type)
		 * 3. If counter equals 0, 3 or 4, the check fails to decide which sentiment has to be set
		 * 4. Repeat steps 1-2-3 for each kind of dictionary
		 * 5. If still no sentiment has been set, the text is neutral
		 */
		/*
		 * PS: to solve neutral conflict, we can count how many istances of each sentiment are present: 
		 * if some is greater than the rest, that will be the chosen sentiment
		 */
		
	}
	
	/*
	 * Checks the sentiment for the given dictionaries
	 */
	private boolean checkSentiment(Set<String> like, Set<String> sad, Set<String> hilarious, Set<String> angry){
		
		int positiveCounter = 0, negativeCounter = 0;
		for(int i = 0; i<words.length; i++){
			String w = words[i];
			String w0 = "";
			if(i-1>=0)
				w0=words[i-1];
			String w02 = "";
			if(i-2>=0)
				w02=words[i-2];
			if(like.contains(w)){
				if(w0.equals("not") || w0.equals("non") || w02.equals("not") || w02.equals("non") || w0.equals("can't") || w02.equals("can't") || w0.equals("cannot") || w02.equals("cannot") )
					negativeCounter++;
				else
					positiveCounter++;
			}
				
			if(hilarious.contains(w)){
				if(w0.equals("not") || w0.equals("non") || w02.equals("not") || w02.equals("non"))
					negativeCounter++;
				else
					positiveCounter++;
			}
				
			if(sad.contains(w)){
				if(w0.equals("not") || w0.equals("non") || w02.equals("not") || w02.equals("non"))
					positiveCounter++;
				else
					negativeCounter++;
			}
			
			if(angry.contains(w)){
				if(w0.equals("not") || w0.equals("non") || w02.equals("not") || w02.equals("non"))
					positiveCounter++;
				else
					negativeCounter++;
			}
		}
		
		if(positiveCounter>negativeCounter){
			sentiment="positive";
			return true;
		}
		else{
			if(negativeCounter>positiveCounter){
				sentiment="negative";
				return true;
			}
		}
		
		/*
		//If a counter is equal or greater than 3 and the rest of them together reach max. 1, the sentiment is set
		if(likeCounter>=3 && (sadCounter+hilariousCounter+angryCounter)<=1){
			sentiment="positive";
			return true;
		}
			
		if(sadCounter>=3 && (likeCounter+hilariousCounter+angryCounter)<=1){
			sentiment="negative";
			return true;
		}
			
		if(hilariousCounter>=3 && (sadCounter+likeCounter+angryCounter)<=1){
			sentiment="positive";
			return true;
		}
			
		if(angryCounter>=3 && (sadCounter+hilariousCounter+likeCounter)<=1){
			sentiment="negative";
			return true;
		}
		
		
		//If a counter is positive and the rest are null, the sentiment is set
		if(likeCounter>0 && (sadCounter+hilariousCounter+angryCounter)==0){
			sentiment="positive";
			return true;
		}
			
		if(sadCounter>0 && (likeCounter+hilariousCounter+angryCounter)==0){
			sentiment="negative";
			return true;
		}
			
		if(hilariousCounter>0 && (sadCounter+likeCounter+angryCounter)==0){
			sentiment="positive";
			return true;
		}
			
		if(angryCounter>0 && (sadCounter+hilariousCounter+likeCounter)==0){
			sentiment="negative";
			return true;
		}
		
		//If sum(like,hilarious) is greater than sum(sad,angry) sentiment is positive (and viceversa)
		if(likeCounter+hilariousCounter>sadCounter+angryCounter){
			sentiment="positive";
			return true;
		}
		else{
			sentiment="negative";
			return true;
		}
			
		*/
			
		
		/*
		 * 1. Loop over the "words" array
		 * 2. Check the presence of the i-th element in the dictionary 
		 * 3. Increment the counter, or let it go
		 * 4.
		 */
		return false;
		
	}
	
	
	public long getNodeID() {
		return nodeID;
	}

	public void setNodeID(long nodeID) {
		this.nodeID = nodeID;
	}

	public long getTweetID() {
		return tweetID;
	}

	public void setTweetID(long tweetID) {
		this.tweetID = tweetID;
	}

	public String getCreated_at() {
		return created_at;
	}

	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public int getLikecount() {
		return likecount;
	}

	public void setLikecount(int likecount) {
		this.likecount = likecount;
	}

	public int getRetweetcount() {
		return retweetcount;
	}

	public void setRetweetcount(int retweetcount) {
		this.retweetcount = retweetcount;
	}

	public String getSentiment() {
		return sentiment;
	}

	public void setSentiment(String sentiment) {
		this.sentiment = sentiment;
	}

	public String[] getWords() {
		return words;
	}

	public void setWords(String[] words) {
		this.words = words;
	}


	

}
