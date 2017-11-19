package stream_data;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;

public class GraphDBManager {

	private static Driver driver;
	private static Session session;
	
	public GraphDBManager(){
		try {
			openConnection();
		} 
		catch (FileNotFoundException e) {
			System.out.println("Can't find the connection configuration file");
			e.printStackTrace();
		}
		
	}
	
	private static void openConnection() throws FileNotFoundException{
		Scanner sc = new Scanner(new File("config/connessione.txt"));
		String host, user, pass;
		host = sc.nextLine();
		user = sc.nextLine();
		pass = sc.nextLine();
		sc.close();
		driver = GraphDatabase.driver(host, AuthTokens.basic(user, pass));
		setSession(driver.session());
		System.out.println("Succesfully connected to graph database at "+host);
	}

	public Session getSession() {
		return session;
	}

	public static void setSession(Session session) {
		GraphDBManager.session = session;
	}
	

}
	
	
	
	
	
	

