package stream_data;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import javax.swing.JOptionPane;


import twitter4j.Status;

public class SparkConsumer {
	
	/*
	static SparkConf conf;
	static JavaStreamingContext jssc;
	static JavaSparkContext jsc;
	static String master = "";
	static String appName = "";
	static String threads = "";
	static long duration;

	public static void main(String[] args) throws FileNotFoundException, InterruptedException {
	
		loadProperties("config/sparkstreaming_conf.txt");
		
		final String consumerKey = "2HYNTq4YRLJGTvZhWcRe6w8Ru";
        final String consumerSecret = "X6OVLg71qvJsDW1HVZ1CGqWVTRXR1k6moU89kqnkeLvsKUvvu3";
        final String accessToken = "706780980770693120-tUipI64r8XL41fYOydB3nI8C9R0mLRY";
        final String accessTokenSecret = "AdrzKxO1iWmJq0bFocfLwpoUh26sZh5mStbmcRqPHuHhT";
        System.setProperty("twitter4j.oauth.consumerKey", consumerKey);
        System.setProperty("twitter4j.oauth.consumerSecret", consumerSecret);
        System.setProperty("twitter4j.oauth.accessToken", accessToken);
        System.setProperty("twitter4j.oauth.accessTokenSecret", accessTokenSecret);
        init();
        JavaStreamingContext jssc = new JavaStreamingContext(conf, new Duration(3000));
        String[] filters={"calcio", "seriea", "immobile", "lazio", "juventus", "sscnapoli", "inzaghi"};
        JavaReceiverInputDStream<Status> twitterStream = TwitterUtils.createStream(jssc, filters);
        JavaDStream<Status> statuses = twitterStream.map(
                new Function<Status, Status>() {
                    public Status call(Status status) { return status; }
                }
        );
        statuses.print();
        jssc.start();
        jssc.awaitTermination();

	}
	
	private static void init(){

		conf = new SparkConf().setAppName(appName).setMaster(master).set("spark.driver.allowMultipleContexts", "true");
		jsc = new JavaSparkContext(conf);
		jssc = new JavaStreamingContext(jsc, new Duration(duration));
		Logger.getLogger("org").setLevel(Level.ERROR);
		Logger.getLogger("akka").setLevel(Level.ERROR);
	}
	
	
	private static void loadProperties(String propertiesFile) throws FileNotFoundException {
		
		File prop = new File(propertiesFile);
		while(!prop.exists())
			prop = new File(JOptionPane.showInputDialog(null, "Properties file not found. Please type the correct location:", "Spark configuration", JOptionPane.ERROR_MESSAGE));
		Scanner sc = new Scanner(prop);
		int count = 0;
		while(sc.hasNextLine()){
			if(count==0)
				appName = sc.nextLine().split("=")[1];
			if(count==1)
				master = sc.nextLine().split("=")[1];
			if(count==2)
				threads = sc.nextLine().split("=")[1];
			if(count==3)
				duration = Long.parseLong(sc.nextLine().split("=")[1]);
			count++;
			}
		sc.close();

	}
*/
}
