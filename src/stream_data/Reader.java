package stream_data;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.neo4j.driver.v1.Session;

import twitter4j.Status;

public class Reader {

	/*private static JavaStreamingContext jssc;
	private static SparkConf conf;
	private static JavaSparkContext jsc;*/
	//private static Map<String, Integer> topics;
	private static Set<String> topics;
	private static String topic = "twitter-test1";
	/*private static String threads;
	private static String appName;
	private static String master;
	private static long duration;*/
	static String zookeeper_server = "";
	static String kafka_consumer_group = "";
	//static JavaPairInputDStream<String, Status> messages;
	private static Map<String, Object> props;
	private static Session session;
	
	
	


	public static void main(String[] args) throws FileNotFoundException, InterruptedException, SQLException {
		loadProperties();
		readData();
	}
	
	private static void init(){
		GraphDBManager gdbm = new GraphDBManager();
		session = gdbm.getSession();
		session.run("CREATE CONSTRAINT ON (source:Source) ASSERT source.application IS UNIQUE");
		session.run("CREATE CONSTRAINT ON (n:Tweet) ASSERT n.tweet_id IS UNIQUE");
		session.run("CREATE CONSTRAINT ON (n:User) ASSERT n.user_id IS UNIQUE");

		/*conf = new SparkConf().setAppName(appName).setMaster(master).set("spark.driver.allowMultipleContexts", "true");
		jsc = new JavaSparkContext(conf);
		jssc = new JavaStreamingContext(jsc, new Duration(duration));*/
		//topics = new HashMap<String, Integer>();
		//topics.put(topic, Integer.parseInt(threads));
		topics = new HashSet<>();
		topics.add(topic);
		/*Logger.getLogger("org").setLevel(Level.ERROR);
		Logger.getLogger("akka").setLevel(Level.ERROR);*/
	}
	
	private static void loadProperties() throws FileNotFoundException{
		/*Scanner sc = new Scanner(new File("config/spark_streaming_conf.txt"));
		int count = 0;
		while(sc.hasNextLine()){
			if(count==0)
				appName = sc.nextLine().split("=")[1];
			if(count==1)
				master = sc.nextLine().split("=")[1];
			if(count==2)
				zookeeper_server = sc.nextLine().split("=")[1];
			if(count==3)
				kafka_consumer_group = sc.nextLine().split("=")[1];
			if(count==4)
				threads = sc.nextLine().split("=")[1];
			if(count==5)
				duration = Long.parseLong(sc.nextLine().split("=")[1]);
			count++;
			}
		sc.close();*/
		
		List<String> kafkaBrokerList = readKafkaBrokers("config/kafka_broker_list.txt");
		List<String> zookeeperServerList = readZKServerList("config/zookeeper_server_list.txt");
		
		props = new HashMap<>();
		
		for(String s : kafkaBrokerList)
			props.put("metadata.broker.list", s);
		
		for(String s : zookeeperServerList)
			props.put("bootstrap.servers", s);
		
		props.put("acks", "all");
		props.put("retries", Integer.toString(0));
		props.put("batch.size", Integer.toString(16384));
		props.put("linger.ms", Integer.toString(20));
		props.put("buffer.memory", Integer.toString(33554432));
		props.put("auto.offset.reset", "latest");
		props.put("key.deserializer", StringDeserializer.class);
		props.put("value.deserializer", ByteArrayDeserializer.class);
		props.put("group.id", "test-consumer-group");
		
		init();

		
	}
	
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
	

	
	private static Status deserializeStatus(byte[] ser) throws IOException, ClassNotFoundException{
		ByteArrayInputStream bis = new ByteArrayInputStream(ser);
        ObjectInput in = new ObjectInputStream(bis);
        Status status = (Status) in.readObject();
        return status;
	}
	
	
	private static void readData() throws InterruptedException, SQLException {
		
		try (KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<String, byte[]>(props)) {
		    consumer.subscribe(Collections.singletonList(topic));
		    int contatore = 0;
		    double time_avg = 0;
		    while (true) {
		        ConsumerRecords<String, byte[]> messages = consumer.poll(100);
		        for (ConsumerRecord<String, byte[]> message : messages) {
		        	Status status = deserializeStatus(message.value());
					long start = System.currentTimeMillis();
					if(status.isRetweet()){
						TwitterManager.insertRetweet(session, topic, status);
						contatore++;
					}
					else
						TwitterManager.insertTweet(session, topic, status);
					contatore++;
					long end = System.currentTimeMillis();
					long diff = (end-start);
					if(time_avg==0)
						time_avg=diff/contatore;
					else
						time_avg = (time_avg*(contatore-1)+(diff))/contatore;
					if(contatore%10==0 && contatore>0)
						System.out.println(contatore+" tweet caricati;"+time_avg);
		        }
		        
		    }
		} catch (Exception e) {
		    e.printStackTrace();
		}
		
		
		
		
		/*messages =  KafkaUtils.createStream(jssc, String.class, byte[].class, org.apache.kafka.common.serialization.StringDeserializer.class, org.apache.kafka.common.serialization.ByteArrayDeserializer.class, props, topics, StorageLevel.DISK_ONLY());
		
		
		messages =  KafkaUtils.createDirectStream(jssc, String.class, Status.class, org.apache.kafka.common.serialization.StringDeserializer.class, stream_data.StatusDeserializer.class, props, topics);
		
		lines = messages.mapToPair((x)->(new Tuple2<String, Integer>(x._2, 1))).reduceByKey(sumFunc);
//		sortedStream = lines.mapToPair(x->x.swap()).transformToPair(sortFunc);
		sortedStream = lines.mapToPair(x->x.swap());
		sortedStream.print();
		sortedStream.foreachRDD(saveHashtagToDB);
		jssc.start();
		jssc.awaitTermination();*/
	}

}
