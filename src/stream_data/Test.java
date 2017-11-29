package stream_data;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Relationship;

public class Test {

	/*
	public static void main(String [] args){
		GraphDBManager g = new GraphDBManager();
		String query = "";
		query = "match p=shortestPath((n:Tweet)-[*..2]-(m:Topic)) where  n.tweet_id=930424855933214726 return m.name";
		StatementResult sr = g.getSession().run(query);
		while(sr.hasNext()){
			Record r = sr.next();
			System.out.println(r.get("m.name").asString());
		}
	}
	*/
	
	public static void main(String [] args){
		
		
		
		
		GraphDBManager g = new GraphDBManager();
		String query;
		String topic = "gfvip";
		System.out.println("Topic: "+topic);
		
		query = "match (u:User)-[:POSTS]->()-->(t:Topic) d where t.name='"+topic+"' return distinct u.screen_name as user"; 
		
		query = "match (t:Tweet)-->(topic:Topic) where topic.name='"+topic+"' return t.tweet_id as tweet_id";
		
		StatementResult sr = g.getSession().run(query);
		Record r;
		Map<String, Integer> result = new HashMap<>();
		while(sr.hasNext()){
			r=sr.next();
		//	String user = r.get("user").asString();
			String tID = r.get("tweet_id").asString();
			String secondQuery = "match p=shortestPath((t:Tweet)-[*..2]-(u:User))"
								+"where t.tweet_id="+tID+" "
								+"return count(u)";

			 
		}
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		/*
		
		
		
		GraphDBManager g = new GraphDBManager();
		String query;
		String topic = "gfvip";
		System.out.println("Topic: "+topic);
		query = "match (n:Topic)<--(b:Tweet)-[r:RETWEETS]->() where n.name='"+topic+"' return count(r)";
		
		
		query = "match (t:Tweet)-->(topic:Topic) where topic.name='"+topic+"' and size( (t)-[:RETWEETS|:REPLIES_TO]->() ) > 0  return count(t)";
		
		StatementResult sr = g.getSession().run(query);
		Record r;
		double influenced = 0;
		while(sr.hasNext()){
			r=sr.next();
			influenced = r.get("count(t)").asDouble();
		}
		query = "match (t:Tweet)-->(topic:Topic) where topic.name='"+topic+"' return count(t)";
		
		sr = g.getSession().run(query);
		double totali = 0;
		while(sr.hasNext()){
			r=sr.next();
			totali = r.get("count(t)").asDouble();
		}
		double div = influenced/totali;
		double ratio = (influenced/totali)*100;
		String outputLabel = Double.toString(ratio).substring(0, 5);
		DecimalFormat df = new DecimalFormat("#.##");
		df.setRoundingMode(RoundingMode.HALF_UP);
		System.out.println(df.format(ratio));
		
		*/
		
	/*
		int contatore = 0;
		while(sr.hasNext()){
			Record r = sr.next();
	//		Iterable<Relationship> rel = r.get("p").asPath().relationships();
	//		for(Relationship r1 : rel){
			//	System.out.print("("+r1.startNodeId()+")-["+r1.type()+"]->("+r1.endNodeId()+")");
			//	contatore++;
			//	System.out.print("\t");
		//	}
		//	System.out.println();
			//System.out.println(r.get("p"));
		//	contatore++;
			contatore = r.get(0).asInt();
				
		//	System.out.println(Iterables.size(rel));
		}
		System.out.println("Coinvolti tramite retweet: \t\t"+contatore);
		
		query = "match (n:Topic)<--(a:Tweet)<-[r:REPLIES_TO]-(b:Tweet) where n.name='"+topic+"' return count(r)";
		sr = g.getSession().run(query);
	
		contatore = 0;
		while(sr.hasNext()){
			Record r = sr.next();
			contatore = r.get(0).asInt();
		}
		System.out.println("Coinvolti tramite risposta: \t\t"+contatore);
		
		
		query = "match (n:Topic)<--(:Tweet)-[r:MENTIONS]->(:User) where n.name='"+topic+"' return count(r)";
		sr = g.getSession().run(query);
	
		contatore = 0;
		while(sr.hasNext()){
			Record r = sr.next();
			contatore = r.get(0).asInt();
		}
		System.out.println("Coinvolti tramite menzione: \t\t"+contatore);
		
		
		query = "match (n:Topic)<--(t:Tweet) where n.name='"+topic+"' return count(t)";
		sr = g.getSession().run(query);
	
		contatore = 0;
		while(sr.hasNext()){
			Record r = sr.next();
			contatore = r.get(0).asInt();
				
		}
		System.out.println("Coinvolti totali: \t\t\t"+contatore);
		
		query = "MATCH (t:Tweet)-[r:MENTIONS]->(u:User), (t)-->(tv:Topic{name:'"+topic+"'}) WHERE u.name<>'null' RETURN u, count(r) as mentions ORDER BY COUNT(r) DESC LIMIT 1";
        sr = g.getSession().run(query);
        while(sr.hasNext()){
        	Record r = sr.next();
        	Node n = r.get("u").asNode();
        	System.out.println(n.get("description").asString());
        	System.out.println(n.get("name").asString());
        	int count = r.get("mentions").asInt();
        	System.out.println("Mentions: "+count);
        }
        
        */
        
        
	}
	

}
