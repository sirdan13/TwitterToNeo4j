package stream_data;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateManager {
	
	public static String getCurrentDate() throws ParseException{
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		Date date = new Date();
		return dateFormat.format(date);
		
	}
	
/*	public static Date convertDateFormat(Date oldDate) throws ParseException{
		SimpleDateFormat fromFmt = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");
        Date date = fromFmt.parse(oldDate);;
    
        SimpleDateFormat toFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(toFmt.format(date)); 
        return date;
	}*/
	
	public static String dateToString(Date date){
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return dateFormat.format(date);
	}
	
	
	

}
