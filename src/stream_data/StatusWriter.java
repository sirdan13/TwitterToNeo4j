package stream_data;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import twitter4j.DirectMessage;
import twitter4j.FilterQuery;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class StatusWriter {
	
	static StatusListener listener;
	static Twitter twitter;
	static ConfigurationBuilder cb;
	static List<String> urls;
	static String topic;
	static int counter = 1;
	/*static JavaSparkContext jsc;
	static JavaRDD<Status> statusRDD;
	static JavaPairRDD<Integer, String> hashtagsRDD;*/
	static int contatore = 0;
	static Properties props;
	public static void main(String[] args) throws InterruptedException, TwitterException, IOException, ParseException{
		
		/*Logger.getLogger("org").setLevel(Level.ERROR);
		Logger.getLogger("akka").setLevel(Level.ERROR);
		SparkConf conf = new SparkConf();
		conf.setAppName("Esempio d'uso di Spark");
		conf.setMaster("local[*]");
		jsc = new JavaSparkContext(conf);*/
		loadKafkaProperties();
		//readSpark();
		//writeData();
		writeDataKafka();
		
		
	}
	/*
	private static void writeData() throws InterruptedException, IOException, ParseException {
		LinkedBlockingQueue<Status> queue = new LinkedBlockingQueue<Status>(100000);
		File statusesFolder = new File("statuses/");
		if(!statusesFolder.exists())
			statusesFolder.mkdir();
		topic = JOptionPane.showInputDialog("Input the topic:");
		int batchSize = Integer.parseInt(JOptionPane.showInputDialog("Input the batch size: "));
		setCredentials(readTwitterAuth("config/oauth.txt"));
		Configuration config = cb.build();
		TwitterFactory tf = new TwitterFactory(config);
		twitter = tf.getInstance();
		TwitterStream twitterStream = new TwitterStreamFactory(config).getInstance();
		FilterQuery query = new FilterQuery();
		urls = new ArrayList<>();
		String [] keywords = importKeywords("config/keywords.txt");
		String [] languages = importKeywords("config/lang.txt");
		//long[] users = importUsers("config/users.txt");
		query.track(keywords);
		//query.follow(users);
		if(!languages[0].equals("none"))
			query.language(languages);
		listener = getListener(queue);
		twitterStream.addListener(listener);
		twitterStream.filter(query);
		List<Status> statusList = new ArrayList<>();
		while(true){
			Status status = queue.poll();
			if(status==null)
				Thread.sleep(100);
			else{
				statusList.add(status);
				if(statusList.size()>=batchSize){
					writeFile(statusList);
					System.out.println("Current status created_at:\t"+status.getCreatedAt());
					System.out.println();
					statusList.clear();
				}
					
			}
		}
		
	}
	*/
	private static void writeDataKafka() throws InterruptedException, IOException, ParseException {
		
		LinkedBlockingQueue<Status> queue = new LinkedBlockingQueue<Status>(100000);
	/*	File statusesFolder = new File("statuses/");
		if(!statusesFolder.exists())
			statusesFolder.mkdir();*/
	//	topic = JOptionPane.showInputDialog("Input the topic:");
	//	int batchSize = Integer.parseInt(JOptionPane.showInputDialog("Input the batch size: "));
		setCredentials(readTwitterAuth("config/oauth.txt"));
		Configuration config = cb.build();
		TwitterFactory tf = new TwitterFactory(config);
		twitter = tf.getInstance();
		TwitterStream twitterStream = new TwitterStreamFactory(config).getInstance();
		FilterQuery query = new FilterQuery();
		urls = new ArrayList<>();
		String [] keywords = importKeywords("config/keywords.txt");
		String [] languages = importKeywords("config/lang.txt");
		//long[] users = importUsers("config/users.txt");
		query.track(keywords);
		//query.follow(users);
		if(!languages[0].equals("none"))
			query.language(languages);
		listener = getListener(queue);
		twitterStream.addListener(listener);
		twitterStream.filter(query);
		//List<Status> statusList = new ArrayList<>();
		
		//Producer<String, Status> producer = new KafkaProducer<>(props);
		Producer<String, byte[]> producer = new KafkaProducer<>(props);
		
		int j = 0;
		while(true){
			Status status = queue.poll();
			if(status==null)
				Thread.sleep(100);
			else{
				//producer.send(new ProducerRecord<String, Status>("twitter-test", Integer.toString(j++), status));
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
			    ObjectOutput out = new ObjectOutputStream(bos);
			    out.writeObject(status);
			    byte b[] = bos.toByteArray();
			    out.close();
			    bos.close();
			 //   double random = Math.random();
			 /*   int topicID;
			    if(random>0.5)
			    	topicID=1;
			    else
			    	topicID=2;
			    producer.send(new ProducerRecord<String, byte[]>("twitter-test"+topicID, Integer.toString(j++), b));
			*/	
			    producer.send(new ProducerRecord<String, byte[]>("twitter-test1", Integer.toString(j++), b));
				
			    if(j%100==0)
					System.out.println("Sent "+j+" messages");
				//statusList.add(status);
				/*if(statusList.size()>=batchSize){
					//writeFile(statusList);
					System.out.println("Current status created_at:\t"+status.getCreatedAt());
					System.out.println();
					statusList.clear();*/
				}
					
			}
		//producer.close();
		}
		
	
	
	
	private static void loadKafkaProperties() throws FileNotFoundException{
		List<String> kafkaBrokerList = readKafkaBrokers("config/kafka_broker_list.txt");
		List<String> zookeeperServerList = readZKServerList("config/zookeeper_server_list.txt");
		
		props = new Properties();
		
		for(String s : kafkaBrokerList)
			props.put("metadata.broker.list", s);
		
		for(String s : zookeeperServerList)
			props.put("bootstrap.servers", s);
		
		props.put("acks", "all");
		props.put("retries", 0);
		props.put("batch.size", 16384);
		props.put("linger.ms", 20);
		props.put("buffer.memory", 33554432);
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		//props.put("value.serializer", "stream_data.StatusSerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
	
	}

	/*private static void readSpark() {

		File folder = new File("analytics/test spark");
		for(File f : folder.listFiles()){
			statusRDD = jsc.objectFile(f.getAbsolutePath());
			/*for(Status s : rdd.collect())
				System.out.println(s.getText());
			JavaPairRDD<Integer, String> htFreq = countHashtags(statusRDD);
			JavaPairRDD<String, Integer> temp;
			if(contatore>0){
				hashtagsRDD.union(htFreq);
				temp = hashtagsRDD.mapToPair(x->x.swap()).reduceByKey(sumFunc);
			}
			else{
				temp = htFreq.mapToPair(x->x.swap());
			}
			contatore++;
			//TODO ripete sempre gli stessi elementi, non aggiorna nell'output----> risolvere
			hashtagsRDD = temp.mapToPair(x->x.swap()).sortByKey(false);
			System.out.println();
			System.out.println("----------------#"+(contatore)+" UPDATE--------------------");
			System.out.println();
			for(Tuple2<Integer, String> s : hashtagsRDD.collect())
					System.out.println(s._2+"\t"+s._1);
		}
	}
	
	public static JavaPairRDD<Integer, String> countHashtags(JavaRDD<Status> statuses) {

		JavaPairRDD<String, Integer> htCount = statuses.flatMapToPair(getHashtags).reduceByKey(sumFunc).filter(frequenceThreshold);
		return  htCount.mapToPair(x->x.swap()).sortByKey(false);
		
	}
	
	static Function<Tuple2<String, Integer>, Boolean> frequenceThreshold = new Function<Tuple2<String, Integer>, Boolean>(){

		@Override
		public Boolean call(Tuple2<String, Integer> arg0) throws Exception {
			if(arg0._2>1)
				return true;
			return false;
		}
		
	};
	
	private static PairFlatMapFunction<Status, String, Integer> getHashtags = new PairFlatMapFunction<Status, String, Integer>(){

		private static final long serialVersionUID = 1L;

		@Override
		public Iterator<Tuple2<String, Integer>> call(Status arg0) throws Exception {
			List<Tuple2<String, Integer>> hashtags = new ArrayList<>();
			for(HashtagEntity h : arg0.getHashtagEntities())
				hashtags.add(new Tuple2<String, Integer>(h.getText(), 1));
			return hashtags.iterator();
		}
		
	};
	
	static Function2<Integer, Integer, Integer> sumFunc = new Function2<Integer, Integer, Integer>() {
		
		private static final long serialVersionUID = 1L;
	
		@Override public Integer call(Integer i1, Integer i2) throws Exception {
			return i1 + i2;
		}
	};*/

	private static String [] readTwitterAuth(String file){
		String [] output = new String[4];
		boolean success = false;
		while(!success){
			Scanner sc = null;
			try {
				sc = new Scanner(new File(file));
				success = true;
				int count = 0;
				while(sc.hasNextLine()){
					output[count++] = sc.nextLine();
				}
				sc.close();
			} catch (FileNotFoundException e1) {
				System.out.println("File not found. Please type its location below (press 0 to exit):");
				Scanner sc2 = new Scanner(System.in);
				file = sc2.nextLine();
				if(file.equals("0"))
					System.exit(-1);
				sc2.close();
			}
			
		}
		return output;
	}
	/*
	private static void writeFile(List<Status> statusList) throws IOException, ParseException{
		
		JavaRDD<Status> rdd = jsc.parallelize(statusList);
		rdd.saveAsObjectFile("analytics/"+topic+"/"+System.currentTimeMillis());
		
		
	}*/
	
	

	private static List<String> readKafkaBrokers(String file) throws FileNotFoundException {
		Scanner sc = new Scanner(new File(file));
		List<String> output = new ArrayList<String>();
		while(sc.hasNextLine())
			output.add(sc.nextLine());
		sc.close();
		return output;
	}
	
	
	private static List<String> readZKServerList(String file) throws FileNotFoundException {
		Scanner sc = new Scanner(new File(file));
		List<String> output = new ArrayList<String>();
		while(sc.hasNextLine())
			output.add(sc.nextLine());
		sc.close();
		return output;
	}
	
	/*
	private static void writeFile(List<Status> statusList) throws IOException, ParseException{
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		File folder = new File("statuses/"+topic);
		if(!folder.exists())
			folder.mkdirs();
		File filename = new File(folder+"/"+System.currentTimeMillis()+".ser");
		fos = new FileOutputStream(filename);
		out = new ObjectOutputStream(fos);
		out.writeObject(statusList);
		out.close();
		fos.close();
		System.out.println(statusList.size()*(counter++)+" tweets stored.");
	}
	*/
	
	//old method
	/*
	private static void writeFile(List<Status> statusList) throws IOException, ParseException{
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		String date = getCurrentDate().substring(0, 10);
		File folder = new File("statuses/"+topic+"/"+date+"/"+getCurrentDate().substring(11, 16).replaceAll(":", ".")+".00");
		if(!folder.exists())
			folder.mkdirs();
		String filename = folder+"/"+UUID.randomUUID().toString()+".ser";
		fos = new FileOutputStream(filename);
		out = new ObjectOutputStream(fos);
		out.writeObject(statusList);
		out.close();
		fos.close();
		System.out.println(statusList.size()*(counter++)+" tweets stored.");
	}*/
			
	public static String getCurrentDate() throws ParseException{
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		Date date = new Date();
		return dateFormat.format(date);
	}
		
	
	private static String[] importKeywords(String filePath) throws FileNotFoundException {
		
		List<String> list = new ArrayList<>();
		Scanner sc = new Scanner(new File(filePath));
		while(sc.hasNextLine())
			list.add(sc.nextLine());
		String [] keywords = new String[list.size()];
		for(int i = 0;i<list.size();i++)
			keywords[i]=list.get(i);
		sc.close();
		return keywords;
	}
	
	@SuppressWarnings("unused")
	private static long[] importUsers(String filePath) throws FileNotFoundException {
		
		List<Long> list = new ArrayList<>();
		Scanner sc = new Scanner(new File(filePath));
		while(sc.hasNextLine())
			list.add(Long.valueOf(sc.nextLine()));
		long [] users = new long[list.size()];
		for(int i = 0;i<list.size();i++)
			users[i]=list.get(i);
		sc.close();
		return users;
	}

	
	@SuppressWarnings({ "unused", "unchecked", "rawtypes" })
	private static void loadTweets() {
		String filename="tweets.ser";
		FileInputStream fis = null;
		ObjectInputStream in = null;
		List<Status> list = new ArrayList<>();
		try{
			System.out.println("Start loading");
			fis = new FileInputStream(filename);
			in = new ObjectInputStream(fis);
			list = (ArrayList) in.readObject();
			in.close();
			System.out.println("Loading completed");
			
		}
		catch(IOException | ClassNotFoundException ex){
			ex.printStackTrace();
		}
		int contatore = 1;
		for(Status s : list){
			System.out.println("#"+contatore+"\t"+s.getUser().getName());
			contatore++;
		}
			
		
	}

	private static void setCredentials(String [] credentials){

		cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true).setOAuthConsumerKey(credentials[0]).setOAuthConsumerSecret(credentials[1]).setOAuthAccessToken(credentials[2]).setOAuthAccessTokenSecret(credentials[3]).setJSONStoreEnabled(true);;

	}
	
	public static StatusListener getListener(LinkedBlockingQueue<Status> queue) {
		return listener = new StatusListener() {
			
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
		//		System.out.println("Got track limitation notice: " + numberOfLimitedStatuses);
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
	}


	
	public static String createTweet(String tweet) throws TwitterException {
		//Twitter twitter = getTwitterinstance();
		Status status = twitter.updateStatus(tweet);
	    return status.getText();
	}
	
	public static List<String> getTimeLine() throws TwitterException {
		List<Status> statuses = twitter.getHomeTimeline();
		return statuses.stream().map(
				item -> item.getText()).collect(
						Collectors.toList());
	}
	
	public static String sendDirectMessage(String recipientName, String msg) throws TwitterException {
	        DirectMessage message = twitter.sendDirectMessage(recipientName, msg);
	        return message.getText();
	}
	
	public static List<String> searchtweets() throws TwitterException {
	        Query query = new Query("source:twitter4j baeldung");
	        QueryResult result = twitter.search(query);
	        List<Status> statuses = result.getTweets();
	        return statuses.stream().map(
				item -> item.getText()).collect(
						Collectors.toList());
	}
	
	public static void streamFeed() {
		
		StatusListener listener = new StatusListener(){

			@Override
			public void onException(Exception e) {
				e.printStackTrace();
			}

			@Override
			public void onDeletionNotice(StatusDeletionNotice arg) {
                                System.out.println("Got a status deletion notice id:" + arg.getStatusId());
			}

			@Override
			public void onScrubGeo(long userId, long upToStatusId) {
                                System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
			}

			@Override
			public void onStallWarning(StallWarning warning) {
                                System.out.println("Got stall warning:" + warning);
			}

			@Override
			public void onStatus(Status status) {
                                System.out.println(status.getUser().getName() + " : " + status.getText());
			}

			@Override
			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                                System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
			}
		};
	
		TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
		
	        twitterStream.addListener(listener);
	    
	        twitterStream.sample();
		
	}
	
}