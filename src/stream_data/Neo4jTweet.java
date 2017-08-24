package stream_data;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.ColorUIResource;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

public class Neo4jTweet {
	
	static Driver driver;
	static Session session;
	static Icon icon =  new ImageIcon("config/icon.png");
	public static long lastLimitation = System.currentTimeMillis();
	
	private static void openConnection() throws FileNotFoundException{
		Scanner sc = new Scanner(new File("config/connessione.txt"));
		String host, user, pass;
		host = sc.nextLine();
		user = sc.nextLine();
		pass = sc.nextLine();
		sc.close();
		session = driver.session();
	}
	
	private static ConfigurationBuilder setTwitterConnection() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, FileNotFoundException{
		
		String [] arguments  = readTwitterAuth();

		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true).setOAuthConsumerKey(arguments[0]).setOAuthConsumerSecret(arguments[1]).setOAuthAccessToken(arguments[2]).setOAuthAccessTokenSecret(arguments[3]);
		
		List<Object> params = insertMultipleValues();
		String [] lang = (String[]) params.get(1);
		String [] keyWords = (String[]) params.get(0);
		String topic = (String) params.get(2);
		return cb;
	}
	
	public static List<Object> insertMultipleValues() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException{

		   JTextField key = new JTextField();
		   JTextField lang = new JTextField();
		   JTextField topic = new JTextField();
		   JLabel label = new JLabel();
		   label.setText("Inserire i parametri: ");
		   label.setFont(new Font("Calibri", Font.BOLD, 20));
		   Object[] message = {label, "Keywords:", key, "Languages:", lang, "Topic:", topic};
		   Dimension size = UIManager.getDimension("OptionPane.minimumSize");
		   size.width = 450;
		   size.height= 300;
		   UIManager.put("OptionPane.background", new ColorUIResource(214,227,249));
		   UIManager.put("Panel.background",new ColorUIResource(214,227,249));
		   int option = JOptionPane.showConfirmDialog(null, message, "Producer", JOptionPane.OK_CANCEL_OPTION, 0, icon);
		   
		   while(option != JOptionPane.OK_OPTION || key.getText().length()<1 || lang.getText().length()<2){
					if(noParamsChosen()==0){
						option = JOptionPane.showConfirmDialog(null, message, "Producer", JOptionPane.OK_CANCEL_OPTION, 0, icon);
						if(option == JOptionPane.OK_OPTION && key.getText().length()>1 && lang.getText().length()>=2)
							break;
					}
					else{
						System.exit(-1);
					}
						
				}
		   
		   String [] keywords = key.getText().split(",");
		   String [] languages = lang.getText().split(",");
		   String topicScelto = topic.getText();
		   List<Object> output = new ArrayList<Object>();
		   output.add(keywords);
		   output.add(languages);
		   output.add(topicScelto);
		   return output;
	}
	
	
	private static int noParamsChosen() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException{
		JLabel label2 = new JLabel("<html>Parametri non inseriti correttamente.<br>Riprovare?</html>");
		label2.setFont(new Font("Calibri", Font.BOLD, 20));
		String [] errorOptions = {"Riprova", "Esci"};
		return JOptionPane.showOptionDialog(null, label2, "Topic", 0, 0, icon, errorOptions, "Riprova");
		}
	
	public static String[] readTwitterAuth() throws FileNotFoundException {
		UIManager.put("OptionPane.background", new ColorUIResource(214,227,249));
		 UIManager.put("Panel.background",new ColorUIResource(214,227,249));
		 Dimension size = UIManager.getDimension("OptionPane.minimumSize");
		 size.width = 450;
		 size.height= 150;
		 UIManager.put("OptionPane.minimumSize", size);
		 JLabel message = new JLabel();
		 message.setText("Scegliere credenziali dev.twitter.com");
		 message.setFont(new Font("Calibri", Font.BOLD, 20));
		 String [] options = {"(default)", "2", "3"};
		 int scelta = JOptionPane.showOptionDialog(null, message, "Credenziali", 2, 0, icon, options, options[0]);
		 if(scelta==0)
			 return readTwitterAuth("config/credenziali_twitter.txt");
		 if(scelta==1)
			 return readTwitterAuth("config/credenziali_twitter2.txt");
		 if(scelta==2)
			 return readTwitterAuth("config/credenziali_twitter3.txt");
		 return null;
	}
	
	
	public static String [] readTwitterAuth(String file) throws FileNotFoundException{
		String [] output = new String[4];
		Scanner sc = new Scanner(new File(file));
		int count = 0;
		while(sc.hasNextLine()){
			output[count++] = sc.nextLine();
		}
		sc.close();
		return output;
	}

	public static void main(String[] args) throws ParseException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, FileNotFoundException, InterruptedException {

		final LinkedBlockingQueue<Status> queue = new LinkedBlockingQueue<Status>(1000);
		
		String startTime = DateManager.getCurrentDate();
		System.out.println(startTime);
		
		ConfigurationBuilder cb = setTwitterConnection();
		
		TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
		StatusListener listener = new StatusListener() {
		
			@Override
			public void onStatus(Status status) {
				queue.offer(status);
			}

			@Override
			public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
				System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
			}

			@Override
			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
				if(System.currentTimeMillis()-lastLimitation>=30000){
					System.out.println();
					System.out.println("Got track limitation notice: " + numberOfLimitedStatuses);
					System.out.println();
					lastLimitation=System.currentTimeMillis();
				}
					
			}

			@Override
			public void onScrubGeo(long userId, long upToStatusId) {
				System.out.println("Got scrub_geo event userId:" + userId + "upToStatusId:" + upToStatusId);
			}

			@Override
			public void onStallWarning(StallWarning warning) {
				System.out.println("Got stall warning:" + warning);
			}

			@Override
			public void onException(Exception ex) {
				ex.printStackTrace();
			}
		};

		FilterQuery query = new FilterQuery();
		
		List<Object> params = insertMultipleValues();
		String [] lang = (String[]) params.get(1);
		String [] keyWords = (String[]) params.get(0);
		String topic = (String) params.get(2);
		query.track(keyWords);
		query = importLanguagesInQuery(query, lang);
		
		twitterStream.addListener(listener);
		
		twitterStream.filter(query);

		int nTweets = 0;
		
		
		long lastUpdate = System.currentTimeMillis();
		
		while (true) {
			Status status = queue.poll();
			if (status == null) {
				Thread.sleep(100);
	
			} else {
				
				
			}
			
		}

	}
	
	
	private static FilterQuery importLanguagesInQuery(FilterQuery query, String[] lang) {
		if(lang.length==0)
			return query.language();
		if(lang.length==1)
			return query.language(lang[0]);
		if(lang.length==2)
			return query.language(lang[0], lang[1]);
		if(lang.length==3)
			return query.language(lang[0], lang[1], lang[2]);
		if(lang.length==4)
			return query.language(lang[0], lang[1], lang[2], lang[3]);
		if(lang.length==5)
			return query.language(lang[0], lang[1], lang[2], lang[3], lang[4]);
		if(lang.length==6)
			return query.language(lang[0], lang[1], lang[2], lang[3], lang[4], lang[5]);
		if(lang.length==7)
			return query.language(lang[0], lang[1], lang[2], lang[3], lang[4], lang[5], lang[6]);
		if(lang.length==8)
			return query.language(lang[0], lang[1], lang[2], lang[3], lang[4], lang[5], lang[6], lang[7]);
		return null;
		
	}

}
