package stream_data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;

import org.apache.kafka.common.serialization.Serializer;


import twitter4j.Status;

public class StatusSerializer implements Serializer<Status> {


	  @Override public byte[] serialize(String s, Status o) {

	       try {
	            ByteArrayOutputStream baos = new ByteArrayOutputStream();
	            ObjectOutputStream oos = new ObjectOutputStream(baos);
	            oos.writeObject(o);
	            oos.close();
	            byte[] b = baos.toByteArray();
	            return b;
	        } catch (IOException e) {
	            return new byte[0];
	        }
	    }

	  @Override public void close() {

	  }

	@Override
	public void configure(Map<String, ?> configs, boolean isKey) {
		
		
	}

	

	  


	}