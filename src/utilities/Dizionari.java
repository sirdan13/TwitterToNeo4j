package utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import java.nio.file.Files;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import javax.swing.JOptionPane;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.types.Node;

import stream_data.GraphDBManager;


public class Dizionari {
	
	static List<String> positive = new ArrayList<>();
	static List<String> negative = new ArrayList<>();

	public static void main(String[] args) throws IOException {
		
		GraphDBManager g = new GraphDBManager();
		Session session = g.getSession();
		String fetch = "MATCH (n:Tweet)-->(t:Topic) WHERE not exists(n.sentimentChecked) RETURN n";
		StatementResult sr = session.run(fetch);
		
		importList("positive");
		importList("negative");

		while(sr.hasNext()){
			
			Record r = sr.next();
			Node tweet = r.get("n").asNode();
			String text = tweet.get("text").asString();
			String input = JOptionPane.showInputDialog(null, text, "0 = neutral, 1 = positive, 2 = negative (-1 to exit)");
			if( isEmpty(input)|| input.equals("-1")){
				Path file = Paths.get("C:\\Users\\daniele\\Desktop\\Tesi\\Eseguibili\\words\\positive-learning.txt");
				Collections.sort(positive);
	            Files.write(file, positive, Charset.forName("UTF-8"));
	            Path file2 = Paths.get("C:\\Users\\daniele\\Desktop\\Tesi\\Eseguibili\\words\\negative-learning.txt");
	            Collections.sort(negative);
	            Files.write(file2, negative, Charset.forName("UTF-8"));
				System.exit(0);
			}
				
			else{
				String sentiment = "";
				if(input.equals("0")) sentiment = "neutral";
				if(input.equals("1")) sentiment = "positive";
				if(input.equals("2")) sentiment = "negative";
				
				session.run("MATCH (t:Tweet) WHERE id(t)="+tweet.id()+" SET t+= {sentimentChecked:TRUE, sentiment:'"+sentiment+"'}");
				text = text.replaceAll("@\\w+", " ").replaceAll("[^a-zA-Z òàèùìçäüöÄÜÖßÁÀÈÉÒÓÚÙñÑ]", "").toLowerCase().replaceAll("http*?\\s", " ").replace("\n", "").replace("\r", "");
				String[] v = text.split("\\s+");
				if(input.equals("1"))
					for(int i = 0; i<v.length; i++){
						if(!v[i].startsWith("http") && !v[i].equals(" "))
							positive.add(v[i]);
					}
						
				if(input.equals("2"))
					for(int i = 0; i<v.length; i++){
						if(!v[i].startsWith("http") && !v[i].equals(" "))
							negative.add(v[i]);
					}
						
			}
		}
		

	}
	
	
	private static void importList(String list) {
		Scanner sc;
		try {
			sc = new Scanner(new File("C:\\Users\\daniele\\Desktop\\Tesi\\Eseguibili\\words\\"+list+"-learning.txt"));
			List<String> temp = new ArrayList<>();
			while(sc.hasNextLine()){
				temp.add(sc.nextLine());
				
			}
			if(list.equals("positive"))
				positive = temp;
			if(list.equals("negative"))
				negative = temp;
			sc.close();
		} catch (FileNotFoundException e) {
			System.out.println("File not found. Creating new file.");
		}
		
		
	}


	public static boolean isEmpty(CharSequence str) {
        if (str == null || str.length() == 0)
            return true;
        else
            return false;
    }

}
