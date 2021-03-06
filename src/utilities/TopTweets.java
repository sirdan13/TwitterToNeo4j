/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;

import java.awt.Color;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;


import java.math.BigDecimal;
import java.math.RoundingMode;

import stream_data.GraphDBManager;
/**
 *
 * @author daniele
 */
public class TopTweets extends javax.swing.JFrame {

    static GraphDBManager gdbm;
    
    /**
     * Creates new form DetailedSentiment
     */
    public TopTweets() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        titleLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        getDataButton = new javax.swing.JButton();
        sentimentToggleButton = new javax.swing.JToggleButton();
        authorToggleButton = new javax.swing.JToggleButton();
        locationToggleButton = new javax.swing.JToggleButton();
        hashtagToggleButton = new javax.swing.JToggleButton();
        mentionedToggleButton = new javax.swing.JToggleButton();
        authorTextField = new javax.swing.JTextField();
        mentionedTextField = new javax.swing.JTextField();
        sentimentTextField = new javax.swing.JTextField();
        hashtagTextField = new javax.swing.JTextField();
        locationTextField = new javax.swing.JTextField();
        autoUpdateToggleButton = new javax.swing.JToggleButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setLocation(new java.awt.Point(50, 20));

        titleLabel.setFont(new java.awt.Font("Calibri", 1, 36)); // NOI18N
        titleLabel.setForeground(new java.awt.Color(153, 0, 0));
        titleLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        titleLabel.setText("Top Tweets");

        jTable1.setFont(new java.awt.Font("Calibri", 0, 16)); // NOI18N
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "User", "Tweet", "Sentiment", "Location", "Pot. Reach"
            }
        ));
        jTable1.setRowHeight(50);
        jScrollPane1.setViewportView(jTable1);

        getDataButton.setFont(new java.awt.Font("Calibri", 1, 24)); // NOI18N
        getDataButton.setText("Carica risultati");
        getDataButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                getDataButtonActionPerformed(evt);
            }
        });

        sentimentToggleButton.setText("Sentiment");
        sentimentToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sentimentToggleButtonActionPerformed(evt);
            }
        });

        authorToggleButton.setText("Pubblicato da");
        authorToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                authorToggleButtonActionPerformed(evt);
            }
        });

        locationToggleButton.setText("Localizzazione");
        locationToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                locationToggleButtonActionPerformed(evt);
            }
        });

        hashtagToggleButton.setText("Hashtag");
        hashtagToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hashtagToggleButtonActionPerformed(evt);
            }
        });

        mentionedToggleButton.setText("Menzione a");
        mentionedToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mentionedToggleButtonActionPerformed(evt);
            }
        });

        authorTextField.setText("Nomi utente...");
        authorTextField.setEnabled(false);

        mentionedTextField.setText("Nomi utente...");
        mentionedTextField.setEnabled(false);

        sentimentTextField.setText("'positive', 'negative', 'neutral'");
        sentimentTextField.setEnabled(false);

        hashtagTextField.setText("#hashtag");
        hashtagTextField.setEnabled(false);

        locationTextField.setText("Roma,Milano,...");
        locationTextField.setEnabled(false);

        autoUpdateToggleButton.setText("Aggiornamento automatico");
        autoUpdateToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoUpdateToggleButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(autoUpdateToggleButton)
                        .addGap(206, 206, 206)
                        .addComponent(titleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 650, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(523, 523, 523)
                        .addComponent(getDataButton, javax.swing.GroupLayout.PREFERRED_SIZE, 299, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(authorToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(authorTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(109, 109, 109)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(mentionedToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(mentionedTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(117, 117, 117)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(sentimentTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(sentimentToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(123, 123, 123)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(hashtagTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(hashtagToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(149, 149, 149)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(locationTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(locationToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(363, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(titleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addComponent(autoUpdateToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(26, 26, 26)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 532, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(locationToggleButton)
                    .addComponent(hashtagToggleButton)
                    .addComponent(sentimentToggleButton)
                    .addComponent(authorToggleButton)
                    .addComponent(mentionedToggleButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(authorTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(mentionedTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sentimentTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(hashtagTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(locationTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 19, Short.MAX_VALUE)
                .addComponent(getDataButton, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(51, 51, 51))
        );

        pack();
    }// </editor-fold>                        

    
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
    
    
    private void getQueryResult(StatementResult sr){
    	double positive = 0, negative = 0; int neutral = 0;
    	
    	 while(sr.hasNext()){
             Record r = sr.next();
             if(r.get(0).asString().equals("positive"))
                 positive = r.get(1).asInt();
             if(r.get(0).asString().equals("negative"))
             	negative = r.get(1).asInt();
             if(r.get(0).asString().equals("neutral"))
             	neutral = r.get(1).asInt();
          }
    	
    	
    	double somma = positive+negative;
        positive = 100-(negative/somma*100);
        negative = 100-positive;
        positive = round(positive, 2);
        negative = round(negative, 2);
        fillTable(positive, negative, neutral);
    	
    }
    
    private void getDataButtonActionPerformed(java.awt.event.ActionEvent evt) {                                              
       
        
        Session session = gdbm.getSession();
        
        String query = "MATCH (t:Tweet)<--(u:User) ";
        String authorQuery = "", locationQuery="", mentionedQuery="", hashtagQuery="", sentimentQuery="";
        
        if(authorToggleButton.isSelected()){
            authorQuery = " u.screen_name='"+authorTextField.getText()+"' ";
        }
        
        if(locationToggleButton.isSelected()){
           locationQuery=" t.location='"+locationTextField.getText()+"' ";
        }
        
        if(mentionedToggleButton.isSelected()){
            query += ", (t)-->(u2:User)";
            mentionedQuery = " u2.screen_name='"+mentionedTextField.getText()+"' ";
        }
        
        if(hashtagToggleButton.isSelected()){
            query += ", (t)-->(h:Hashtag)";
            hashtagQuery = " h.tag='"+hashtagTextField.getText()+"'";
        }
        
        if(sentimentToggleButton.isSelected()){
            sentimentQuery=" t.sentiment='"+sentimentTextField.getText()+"'";
        }
        
        query += "\n WHERE ";
        
        int contaCondizioni = 0;
        
            
        if(authorToggleButton.isSelected()){
            query+=" "+authorQuery;
            contaCondizioni++;
        }
        if(locationToggleButton.isSelected()){
            if(contaCondizioni>0)
                query+=" and "+locationQuery;
            else
                query+=" "+locationQuery;
            contaCondizioni++;
        }
        if(mentionedToggleButton.isSelected()){
            if(contaCondizioni>0)
                query+=" and "+mentionedQuery;
            else
                query+=" "+mentionedQuery;
            contaCondizioni++;
        }
        if(sentimentToggleButton.isSelected()){
            if(contaCondizioni>0)
                query+=" and "+sentimentQuery;
            else
                query+=" "+sentimentQuery;
            contaCondizioni++;
        }
               if(hashtagToggleButton.isSelected()){
            if(contaCondizioni>0)
                query+=" and "+hashtagQuery;
            else
                query+=" "+hashtagQuery;
            contaCondizioni++;
        }
        
        query+=" WITH t, u, (t.potentialReach) as calcolo"
                + "\n RETURN u.name as author, t.text as tweet, t.sentiment as sentiment, t.location as location, calcolo"
                + "\n ORDER BY calcolo DESC"
                + "\n LIMIT 10";
        
        StatementResult sr = session.run(query);
        int riga = 0;
        clearTable();
        while(sr.hasNext()){
            Record r = sr.next();
            String author = r.get("author").asString(), text = r.get("tweet").asString(), sentiment = r.get("sentiment").asString(), location = r.get("location").asString();
            int potentialReach = r.get("calcolo").asInt();
            jTable1.setValueAt(author, riga, 0);
            jTable1.setValueAt(text, riga, 1);
            jTable1.setValueAt(sentiment, riga, 2);
            jTable1.setValueAt(location, riga, 3);
            jTable1.setValueAt(potentialReach, riga, 4);
            riga++;
            
        }
        
        
        
        
         
    }                                             

    private void locationToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {                                                     
        if(locationToggleButton.isSelected()){
            locationTextField.setEnabled(true);
            locationTextField.grabFocus();
            locationTextField.setText("");
        }
            
        else{
            locationTextField.setEnabled(false);
            locationTextField.setText("Roma,Milano,...");
        }
            
    }                                                    

    private void mentionedToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {                                                      
        if(mentionedToggleButton.isSelected()){
            mentionedTextField.setEnabled(true);
            mentionedTextField.grabFocus();
            mentionedTextField.setText("");
        }
            
        else{
            mentionedTextField.setEnabled(false);
            mentionedTextField.setText("Nomi utente...");
        }
            
    }                                                     

    private void authorToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {                                                   
        if(authorToggleButton.isSelected()){
            authorTextField.setEnabled(true);
            authorTextField.grabFocus();
            authorTextField.setText("");
        }
        else{
            authorTextField.setEnabled(false);
            authorTextField.setText("Nomi utente...");
        }
            
    }                                                  

    private void sentimentToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {                                                      
        if(sentimentToggleButton.isSelected()){
            sentimentTextField.setEnabled(true);
            sentimentTextField.grabFocus();
            sentimentTextField.setText("");
        }
        else{
            sentimentTextField.setEnabled(false);
            sentimentTextField.setText("'positive', 'negative', 'neutral'");
        }
            
    }                                                     

    private void hashtagToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {                                                    
        if(hashtagToggleButton.isSelected()){
            hashtagTextField.setEnabled(true);
            hashtagTextField.grabFocus();
            hashtagTextField.setText("");
        }
        else{
            hashtagTextField.setEnabled(false);
            hashtagTextField.setText("#hashtag");
        }
            
    }                                                   

    private void autoUpdateToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {                                                       
                if(autoUpdateToggleButton.isSelected()){
        autoUpdateToggleButton.setBackground(Color.red);
           new Thread(new Runnable(){

           public void run(){
                   long start = 0L;
                   do{
                	   	while(System.currentTimeMillis()-start>1500){
	                	   	start = System.currentTimeMillis();
	                        getDataButtonActionPerformed(evt);
                	   	}
                         
                    }while(autoUpdateToggleButton.isSelected());
           }
           }).start();
        }
        else
        	autoUpdateToggleButton.setBackground(Color.LIGHT_GRAY);
    }                                                      

  private void unToggleHashtag(){
        hashtagTextField.setEditable(false);
        hashtagTextField.setEnabled(false);
        hashtagTextField.setText("Inserisci un hashtag...");
        hashtagToggleButton.setSelected(false);
    }
    
    private void unToggleLocation(){
	    locationTextField.setEditable(false);
        locationTextField.setEnabled(false);
        locationTextField.setText("Inserisci una località...");
        locationToggleButton.setSelected(false);
    }
    
    
    
    private void fillTable(double positive, double negative, int neutral){
        
    }
    
   private void clearTable(){
        for(int i = 0;i<jTable1.getRowCount();i++)
            for(int j = 0;j<jTable1.getColumnCount();j++)
                jTable1.setValueAt("", i, j);
    }
    
    
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(TopTweets.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TopTweets.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TopTweets.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TopTweets.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        gdbm = new GraphDBManager();
        
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TopTweets().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify                     
    private javax.swing.JTextField authorTextField;
    private javax.swing.JToggleButton authorToggleButton;
    private javax.swing.JToggleButton autoUpdateToggleButton;
    private javax.swing.JButton getDataButton;
    private javax.swing.JTextField hashtagTextField;
    private javax.swing.JToggleButton hashtagToggleButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField locationTextField;
    private javax.swing.JToggleButton locationToggleButton;
    private javax.swing.JTextField mentionedTextField;
    private javax.swing.JToggleButton mentionedToggleButton;
    private javax.swing.JTextField sentimentTextField;
    private javax.swing.JToggleButton sentimentToggleButton;
    private javax.swing.JLabel titleLabel;
    // End of variables declaration                   
}
