package utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;


public class Utilities {
	
	private Set<String> emojiLike;
	private Set<String> emojiSad;
	private Set<String> emojiAngry;
	private Set<String> emojiHilarious;
	private Set<String> coreLike;
	private Set<String> coreSad;
	private Set<String> coreAngry;
	private Set<String> coreHilarious;
	private Set<String> likeWords;
	private Set<String> sadWords;
	private Set<String> angryWords;
	private Set<String> hilariousWords;
	
	
	public Utilities(){
		try {
			emojiLike = wordSetFromTxtFile("words/emoji/emojilike.txt");
			emojiSad = wordSetFromTxtFile("words/emoji/emojilike.txt");
			emojiHilarious = wordSetFromTxtFile("words/emoji/emojihilarious.txt");
			emojiAngry = wordSetFromTxtFile("words/emoji/emojiangry.txt");
			
			coreLike = wordSetFromTxtFile("words/core/Like.txt");
			coreHilarious = wordSetFromTxtFile("words/core/Hilarious.txt");
			coreSad = wordSetFromTxtFile("words/core/Sad.txt");
			coreAngry = wordSetFromTxtFile("words/core/Angry.txt");
			
			likeWords = wordSetFromTxtFile("words/dic/Like.txt");
			sadWords = wordSetFromTxtFile("words/dic/Sad.txt");
			angryWords = wordSetFromTxtFile("words/dic/Angry.txt");
			hilariousWords = wordSetFromTxtFile("words/dic/Hilarious.txt");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Create a set of strings from a text file (one word per row)
	 * @param swFile the input file
	 * @return a set of strings
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static Set<String> wordSetFromTxtFile(String swFile) throws IOException, FileNotFoundException{
		
		TreeSet<String> swSet = new TreeSet<String>();
		String str = new String();
		
		File inputfile = new File(swFile);
		
		BufferedReader in = new BufferedReader(
				   new InputStreamReader(
		                      new FileInputStream(inputfile), "UTF8")); 
		 

		while ((str = in.readLine()) != null) {
			swSet.add(str.replace(" ", "" )); //remove white spaces
		}
		
		in.close();
				
		return swSet;
				
	}
	
	
	public static String convertDate(Date date){
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		return dateFormat.format(date);
		
	}
	
	public Set<String> getCoreLike() {
		return coreLike;
	}

	public void setCoreLike(Set<String> coreLike) {
		this.coreLike = coreLike;
	}

	public Set<String> getCoreSad() {
		return coreSad;
	}

	public void setCoreSad(Set<String> coreSad) {
		this.coreSad = coreSad;
	}

	public Set<String> getCoreAngry() {
		return coreAngry;
	}

	public void setCoreAngry(Set<String> coreAngry) {
		this.coreAngry = coreAngry;
	}

	public Set<String> getCoreHilarious() {
		return coreHilarious;
	}

	public void setCoreHilarious(Set<String> coreHilarious) {
		this.coreHilarious = coreHilarious;
	}


	public Set<String> getEmojiLike() {
		return emojiLike;
	}

	public void setEmojiLike(Set<String> emojiLike) {
		this.emojiLike = emojiLike;
	}

	public Set<String> getEmojiSad() {
		return emojiSad;
	}

	public void setEmojiSad(Set<String> emojiSad) {
		this.emojiSad = emojiSad;
	}

	public Set<String> getEmojiAngry() {
		return emojiAngry;
	}

	public void setEmojiAngry(Set<String> emojiAngry) {
		this.emojiAngry = emojiAngry;
	}

	public Set<String> getEmojiHilarious() {
		return emojiHilarious;
	}

	public void setEmojiHilarious(Set<String> emojiHilarious) {
		this.emojiHilarious = emojiHilarious;
	}

	public Set<String> getLikeWords() {
		return likeWords;
	}

	public void setLikeWords(Set<String> likeWords) {
		this.likeWords = likeWords;
	}

	public Set<String> getSadWords() {
		return sadWords;
	}

	public void setSadWords(Set<String> sadWords) {
		this.sadWords = sadWords;
	}

	public Set<String> getAngryWords() {
		return angryWords;
	}

	public void setAngryWords(Set<String> angryWords) {
		this.angryWords = angryWords;
	}

	public Set<String> getHilariousWords() {
		return hilariousWords;
	}

	public void setHilariousWords(Set<String> hilariousWords) {
		this.hilariousWords = hilariousWords;
	}

	public static Date getCurrentDate() throws ParseException{
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		Date date = new Date();
		return dateFormat.parse(dateFormat.format(date));
		
	}
	
	
}
