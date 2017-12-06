package utilities;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import twitter4j.Status;

public class StatusSerializationTest{
    public static void save(List<Status> status){
        try(
            FileOutputStream fos = new FileOutputStream("tweets.ser");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
        ) {
            oos.writeObject(status);
        }catch(IOException ioe) {
            System.out.println("Problem saving Tweets");
            ioe.printStackTrace();
        }
    }
    public static List<Status> load(){
    	List<Status> status = new ArrayList<>();
        try(
            FileInputStream fis = new FileInputStream("tweets.ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
        ){
            status = (List<Status>) ois.readObject();
        } catch(IOException ioe){
            System.out.println("Error reading file!");
            ioe.printStackTrace();
        } catch(ClassNotFoundException cnfe){
            System.out.println("Error loading treets.");
            cnfe.printStackTrace();
        }
        return status;
    }
}