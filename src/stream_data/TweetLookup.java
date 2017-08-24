package stream_data;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import static org.neo4j.driver.v1.Values.parameters;

public class TweetLookup{
	
	public static void main(String[] args) throws Exception {
		
		
		Driver driver = GraphDatabase.driver( "bolt://localhost:7687", AuthTokens.basic( "neo4j", "tesi" ) );
		Session session = driver.session();

	//	session.run( "CREATE (a:Person {name: {name}, title: {title}})", parameters( "name", "Arthur", "title", "King" ) );

		StatementResult result = session.run( "MATCH (t:Tweet) "+
		                                      "WHERE t.tweet_id={id} "+
											"RETURN t.tweet_id as tweet_id, t.text as text, t.location as location, t.retweetcount as retweetcount",
		      parameters( "id", "899200229202898944" ) );
		while ( result.hasNext() )
		{
		    Record record = result.next();
		   
		    System.out.println( record.get( "text" ).asString() + " " + record.get( "location" ).asString() );
		}
		
	/*	session.run( "CREATE (a:Person {name: {name}, title: {title}})",
		        parameters( "name", "Arthur", "title", "King" ) );

		StatementResult result = session.run( "MATCH (a:Person) WHERE a.name = {name} " +
		                                      "RETURN a.name AS name, a.title AS title",
		        parameters( "name", "Arthur" ) );
		while ( result.hasNext() )
		{
		    Record record = result.next();
		    System.out.println( record.get( "title" ).asString() + " " + record.get( "name" ).asString() );
		}

		session.close();
		driver.close();
		*/
}
}

