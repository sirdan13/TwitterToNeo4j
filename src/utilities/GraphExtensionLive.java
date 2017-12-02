package utilities;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.Timer;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UnsupportedLookAndFeelException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import stream_data.GraphDBManager;

public class GraphExtensionLive {
	
	@SuppressWarnings("deprecation")
	final TimeSeries series = new TimeSeries("Involved users", Minute.class);
	final TimeSeriesCollection data = new TimeSeriesCollection(series);
	final JFreeChart graphics = ChartFactory.createTimeSeriesChart("Involved users", "Time", "Users", data, true, true, true);
	final JPanel panel = new ChartPanel(graphics);
	static JPanel bottomPanel = new JPanel();
	static JPanel topPanel = new JPanel();
	final JButton insertData = new JButton("Get data");
	final JButton clearButton = new JButton("Clear chart");
	final JButton saveParameters = new JButton("Save parameters");
	JLabel dateLabel = new JLabel("Insert start date:");
	JLabel topicLabel = new JLabel("Insert topic:");
	JLabel endDateLabel = new JLabel("Insert end date:");
	static JTextField insertTopic = new JTextField("Insert topic");
	static List<Integer> arrayTime;
	static String [] times;
	GraphDBManager g = new GraphDBManager();
	Session session = g.getSession();
	static String initialTime;
	static String finalTime;
	static String startDate;
	static String endDate; 
	static String topic = "";
	int buttonPressed=0;
	static int contatore = 0;
	private com.github.lgooddatepicker.components.DateTimePicker dateTimePicker1;
	private com.github.lgooddatepicker.components.DateTimePicker dateTimePicker2;
	long minutesDiff = 0;
	
	public GraphExtensionLive(){
		JFrame frame = new JFrame();
		dateTimePicker1 = new com.github.lgooddatepicker.components.DateTimePicker();
		dateTimePicker2 = new com.github.lgooddatepicker.components.DateTimePicker();
		clearButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				series.clear();
				contatore=0;
				buttonPressed=0;
				
			}
			
		});
		

		insertTopic.addFocusListener(new FocusListener() {
		    public void focusGained(FocusEvent e) {
		    	insertTopic.setText("");
		    }

		    public void focusLost(FocusEvent e) {
		    	
		    }
		    
		});
		
		
		saveParameters.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				String datePicker = convertDatePickerFormat(dateTimePicker1.getDatePicker().getText());
				String timePicker = convertTimePickerFormat(dateTimePicker1.getTimePicker().getText());
				initialTime = datePicker+" "+timePicker;
				String datePicker2 = convertDatePickerFormat(dateTimePicker2.getDatePicker().getText());
				String timePicker2 = convertTimePickerFormat(dateTimePicker2.getTimePicker().getText());
				finalTime = datePicker2+" "+timePicker2;
				topic = insertTopic.getText();
				try {
					Date d1 = stringToDate(initialTime);
					Date d2 = stringToDate(finalTime);
					minutesDiff = getDateDiff(d1, d2, TimeUnit.MINUTES);
					System.out.println(minutesDiff);
				} catch (ParseException e1) {
					e1.printStackTrace();
				}
			}
			
		});
		
		insertData.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {

				while(contatore<minutesDiff){
					contatore++;
					buttonPressed++;
					Date a,b;

					try {
						
						if(buttonPressed>1){
							a = stringToDate(startDate);
							b = stringToDate(endDate);
							startDate = dateToString(addSecs(a, 60));
							endDate = dateToString(addSecs(b, 60));
						}
						else{
							startDate=initialTime;
							a=stringToDate(startDate);
							endDate=dateToString(addSecs(a, 60));
							b = stringToDate(endDate);
						}
							
						
						String query = "MATCH  (t:Tweet)-->(topic:Topic), (t)--(u:User) WHERE topic.name='"+topic+"' ";
						query += "and t.created_at>'"+startDate+"' ";
						query += "and t.created_at<='"+endDate+"' ";
						query += " return count(distinct u) as users";
						StatementResult sr = session.run(query);
						int involvedUsers = sr.next().get("users").asInt();
						String date = startDate;
						final Hour ora = new Hour(Integer.parseInt(date.substring(11, 13)), Integer.parseInt(date.substring(8, 10)), Integer.parseInt(date.substring(5, 7)), Integer.parseInt(date.substring(0, 4)));
						final int min = Integer.parseInt(date.substring(14, 16));
						series.add(new Minute(min, ora), (double) involvedUsers);
						
						
					} catch (ParseException e2) {
						
						e2.printStackTrace();
					}
				
				}
				}
					
				
			

		});
			
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.add(panel, BorderLayout.CENTER);
		frame.setSize(1000, 500);
		frame.setLocation(550, 300);
		topPanel.add(topicLabel);
		topPanel.add(insertTopic);
		topPanel.add(dateLabel);
		topPanel.add(dateTimePicker1);
		topPanel.add(endDateLabel);
		topPanel.add(dateTimePicker2);
		topPanel.add(saveParameters);
		frame.add(topPanel, BorderLayout.NORTH);
		bottomPanel.add(insertData, BorderLayout.EAST);
		bottomPanel.add(clearButton, BorderLayout.WEST);
		frame.add(bottomPanel, BorderLayout.SOUTH);
		frame.setVisible(true);
	}
	
	public void insertDataActionPerformed(ActionEvent arg0) {
	
		
	}
	

	public static void main(String[] args) throws ParseException {

		for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
                try {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
						| UnsupportedLookAndFeelException e) {
					e.printStackTrace();
				}
                break;
            }
        }
		new GraphExtensionLive();
		

	}
	
	 public static Date addSecs(Date oldDate, int seconds) {

			long ONE_SECOND_IN_MILLIS = 1000;
			long t = oldDate.getTime();
			Date newDate = new Date(t + (seconds * ONE_SECOND_IN_MILLIS));
			return newDate;

		}
	 
	 public static String dateToString(Date date){
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			return dateFormat.format(date);
			
		}
	 
	 public static Date stringToDate(String string) throws ParseException{
		 
		 DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		return dateFormat.parse(string);
	 }
	 
     private String convertTimePickerFormat(String input){
    	int hour = Integer.parseInt(input.substring(0, input.length()-3));
    	String sHour = "";
    	if(hour<10)
    		sHour+="0"+hour;
    	else
    		sHour=String.valueOf(hour);
    	String sMin = "";
    	int min = Integer.parseInt(input.substring(input.length()-2, input.length()));
    	if(min<10)
    		sMin+="0"+min;
    	else
    		sMin=String.valueOf(min);
    	String output = sHour+":"+sMin+":00.000";
    	return output;
    }
    
    private String convertDatePickerFormat(String input){
    	input = input.replaceAll(" ", "-");
    	String [] split = input.split("-");
    	int day = Integer.parseInt(split[0]);
    	String sDay = "";
    	if(day<10)
    		sDay+="0"+day;
    	else
    		sDay=String.valueOf(day);
    	String sMon = checkMonth(split[1]);
    	String sYear = split[2];
    	return sYear+"-"+sMon+"-"+sDay;
    }
    
    private String checkMonth(String input){
    	if(input.startsWith("gen"))
    		return "01";
    	if(input.startsWith("feb"))
    		return "02";
    	if(input.startsWith("mar"))
    		return "03";
    	if(input.startsWith("apr"))
    		return "04";
    	if(input.startsWith("mag"))
    		return "05";
    	if(input.startsWith("giu"))
    		return "06";
    	if(input.startsWith("lug"))
    		return "07";
    	if(input.startsWith("ago"))
    		return "08";
    	if(input.startsWith("set"))
    		return "09";
    	if(input.startsWith("ott"))
    		return "10";
    	if(input.startsWith("nov"))
    		return "11";
    	if(input.startsWith("dic"))
    		return "12";
    	return null;
    	
    }
	 
	 /**
	  * Get a diff between two dates
	  * @param date1 the oldest date
	  * @param date2 the newest date
	  * @param timeUnit the unit in which you want the diff
	  * @return the diff value, in the provided unit
	  */
	 public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
	     long diffInMillies = date2.getTime() - date1.getTime();
	     return (timeUnit.convert(diffInMillies,timeUnit))/60000;
	 }

}
