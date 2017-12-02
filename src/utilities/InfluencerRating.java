package utilities;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.types.Node;

import stream_data.GraphDBManager;

public class InfluencerRating {
	
	private static Session session;
	private static GraphDBManager gdbm;

	public static void main(String[] args) {
		
		/*
		 * Extract users with more than 100k followers
		 */
		
		gdbm = new GraphDBManager();
		session = gdbm.getSession();
		
		String query = "MATCH (u:User)-->(t:Tweet) WHERE exists(u.name) and not exists(u.influencerRating) RETURN distinct u as user order by u.followers desc";
		StatementResult sr = session.run(query);
		Record r = null;
		while(sr.hasNext()){
			r = sr.next();
			Node n = r.get("user").asNode();
			long idNode = n.id();
		//	int followers = n.get("followers").asInt();
			String name = n.get("name").asString();
			
			int sumRTCount = 0, sumLikeCount = 0;
			
			query = "MATCH (u:User)-->(t:Tweet) where id(u)="+idNode+" RETURN sum(t.retweetcount) as rtsum, sum(t.likecount) as likesum";
			StatementResult sr2 = session.run(query);
			Record r1 = null;
			while(sr2.hasNext()){
				r1 = sr2.next();
				sumRTCount = r1.get("rtsum").asInt();
				sumLikeCount = r1.get("likesum").asInt();
			}
			
			double rank = sumRTCount*0.5 + sumLikeCount*0.15;
			if(n.get("verified").asBoolean())
				rank = rank*1.05;
			//TODO sommare numero utenti influenzati * 1.5
			
			query = "MATCH (u:User) where id(u)="+idNode+" SET u += {influencerRating : "+(rank/1000)+"}";
			session.run(query);
			
			System.out.println(name+"\t rank:"+(rank/1000));
			
		}
		

	}

}
