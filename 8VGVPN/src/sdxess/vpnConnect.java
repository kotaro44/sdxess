/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sdxess;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import static sdxess.ExecutorTask.setTimeout;

/**
 *
 * @author kotaro
 */
public class vpnConnect extends javax.swing.JFrame {
    
    public ExecutorTask task;
    public Thread executorThread;
    private Process tunnelProcess;
    private boolean isConnected = false;
    private boolean connectionEstablished = false;
    private StringBuilder sb;
    private BufferedReader br;
    private ArrayList<Website> IPlist;
    private ArrayList<Website> websites;
    private int retries = 0;
    
    //timer variables
    private int seconds = 0;
    private boolean timerstatus = false;
    private Timer timer = new Timer(); 
    private TimeZone tz = TimeZone.getTimeZone("UTC");
    private SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
    public static HostEdit hostEdit = null;
    
    private int originalHeight;
    

    /***************************************************************************
    ***  brief                                                               ***
    ***  serial number ????                                                  ***
    ***  parameter out <none>                                                ***
    ***  parameter in  <none>                                                ***
    ***  return <none>                                                       ***
    *** @param                                                               ***
    ***************************************************************************/
    public vpnConnect(){
        if( !StaticRoutes.isAdmin() ){
            JOptionPane.showMessageDialog(null, "This program has to be run as administrator!");
            System.exit(0);
        }
        
        this.checkCommit();
        
        initComponents();
        this.getConfs();
        sitesBtn.setVisible(false);
        ctimeLbl.setVisible(false);
        
        this.setLocation(100, 100);
        this.setSize(this.getWidth(), 280);
        this.originalHeight = this.getHeight();
      
            /*List<String> Ip = sun.net.dns.ResolverConfiguration.open().nameservers();
            StaticRoutes.DNS = Ip.get(Ip.size()-1);*/
        
        
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                vpnConnect.disconnect();
                System.exit(0);
            }
        });
       
    }
    
    /***************************************************************************
    ***  brief                                                               ***
    ***  serial number ????                                                  ***
    ***  parameter out <none>                                                ***
    ***  parameter in  <none>                                                ***
    ***  return <none>                                                       ***
    *** @param                                                               ***
    ***************************************************************************/
    public void connected( ArrayList<String> sites2reroute , ArrayList<Website> websites){
        this.retries = 0;
        this.IPlist = new ArrayList<Website>();
        this.websites = websites;
        consoleLabel.setText("Connected to " + serverCombo.getSelectedItem());
        sitesBtn.setVisible(true);
        ctimeLbl.setText("Rerouting websites...");
        ctimeLbl.setVisible(true);
        logginPanel.setVisible(false);
        
        this.setSize(this.getWidth(), 177);
        
        this.isConnected = true;
        connectBtn.setText("Disconnect");
        connectBtn.setEnabled(true);
        
   
        if( !redirectCheck.isSelected() )
            StaticRoutes.disableAllTrafficReroute();
        StaticRoutes.flushDNS();
        
        for( int i = 0 ; i < sites2reroute.size() ; i++ ){
            String[] parts = sites2reroute.get(i).split(" ");
            Website website = new Website(parts[0]);
            if( website.isReachable() ){
                
            }else{
                website.IP = parts[1];
                website.isStatic = true;
            }
            this.IPlist.add(website);
        }
        
        this.rerouteSites();
        this.startTimer();
        /*try {
            //save host files
            br = new BufferedReader(new FileReader("C:/Windows/System32/drivers/etc/hosts"));
            sb = new StringBuilder();
            String line = br.readLine();
            
            while (line != null) {
                sb.append(System.lineSeparator());
                sb.append(line);
                line = br.readLine();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(vpnConnect.class.getName()).log(Level.SEVERE, null, ex);
        } catch(IOException ex){
            Logger.getLogger(vpnConnect.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        
    }
    
    /***************************************************************************
    ***  brief                                                               ***
    ***  serial number ????                                                  ***
    ***  parameter out <none>                                                ***
    ***  parameter in  <none>                                                ***
    ***  return <none>                                                       ***
    *** @param                                                               ***
    ***************************************************************************/
    public void updateMessage(String message){
        consoleLabel.setText(message);
    }
    
    /***************************************************************************
    ***  brief                                                               ***
    ***  serial number ????                                                  ***
    ***  parameter out <none>                                                ***
    ***  parameter in  <none>                                                ***
    ***  return <none>                                                       ***
    *** @param                                                               ***
    ***************************************************************************/
    public void reconnecting(boolean previouslyConnected){
        if( previouslyConnected ){
            consoleLabel.setText("Communication lost... reconnecting...");
        }else{
            consoleLabel.setText("Unstable connection... reconnecting...");
            ExecutorTask.setTimeout(() -> this.retryTimeout(), 11000);
            
        }
        userField.setEnabled(false);
        serverCombo.setEnabled(false);
        passField.setEnabled(false);
        connectBtn.setEnabled(false);
        sitesBtn.setVisible(false);
        ctimeLbl.setVisible(false);
        ctimeLbl.setText("");
        if( vpnConnect.hostEdit != null )
            vpnConnect.hostEdit.setVisible(false);
    }
    
    /***************************************************************************
    ***  brief                                                               ***
    ***  serial number ????                                                  ***
    ***  parameter out <none>                                                ***
    ***  parameter in  <none>                                                ***
    ***  return <none>                                                       ***
    *** @param                                                               ***
    ***************************************************************************/
    public void retryTimeout(){
        if( !this.isConnected && !this.connectionEstablished){
            this.disconnected(true);
        }
    }
    
    /***************************************************************************
    ***  brief                                                               ***
    ***  serial number ????                                                  ***
    ***  parameter out <none>                                                ***
    ***  parameter in  <none>                                                ***
    ***  return <none>                                                       ***
    *** @param                                                               ***
    ***************************************************************************/
    public void notconnected(String message){
        consoleLabel.setText("Connection error: " + message);
        userField.setEnabled(true);
        serverCombo.setEnabled(true);
        passField.setEnabled(true);
        connectBtn.setEnabled(true);
        //puttyCheck.setEnabled(true);
        redirectCheck.setEnabled(true);
        redirectCheck.setEnabled(true);
        ctimeLbl.setVisible(false);
        ctimeLbl.setText("");
    }

    /***************************************************************************
    ***  brief                                                               ***
    ***  serial number ????                                                  ***
    ***  parameter out <none>                                                ***
    ***  parameter in  <none>                                                ***
    ***  return <none>                                                       ***
    *** @param                                                               ***
    ***************************************************************************/
    public void disconnected(boolean byError){
        consoleLabel.setText("disconnecting...");
        connectBtn.setEnabled(false);
        sitesBtn.setEnabled(false);
        
        if( vpnConnect.hostEdit != null )
            vpnConnect.hostEdit.setVisible(false);
        

        ExecutorTask.setTimeout(() -> this.disconnectFromVPN(byError), 10);
        
    }
    
    public void disconnectFromVPN(boolean byError){
        this.task.end();
        vpnConnect.disconnect();
        //this.tunnelProcess.destroyForcibly();
        
        this.retries = 0;
        this.isConnected = false;
        this.connectionEstablished = false;
        sitesBtn.setVisible(false);
        userField.setEnabled(true);
        passField.setEnabled(true);
        serverCombo.setEnabled(true);
        connectBtn.setText("Connect");
        connectBtn.setEnabled(true);
        //puttyCheck.setEnabled(true);
        redirectCheck.setEnabled(true);
        logginPanel.setVisible(true);
        sitesBtn.setEnabled(true);
        this.setSize( this.getWidth() , this.originalHeight );
        if( byError )
            consoleLabel.setText("disconnected due time out.");
        else
            consoleLabel.setText("disconnected.");
        ctimeLbl.setVisible(false);
        ctimeLbl.setText("");
        
        
        this.seconds = 0;
        
        
        /*if( puttyCheck.isSelected() ){
            try {
                Process process = Runtime.getRuntime().exec("cmd /c taskkill.exe /F /IM putty.exe");
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                System.out.println("openvpn Process exited.");
            } catch (IOException ex) {
                Logger.getLogger(vpnConnect.class.getName()).log(Level.SEVERE, null, ex);
            }
        }*/
    }
    
    /***************************************************************************
    ***  brief                                                               ***
    ***  serial number ????                                                  ***
    ***  parameter out <none>                                                ***
    ***  parameter in  <none>                                                ***
    ***  return <none>                                                       ***
    *** @param                                                               ***
    ***************************************************************************/
    private void getConfs(){
        File folder = new File("confs");
        File[] listOfFiles = folder.listFiles();

        javax.swing.DefaultComboBoxModel comboModel = new javax.swing.DefaultComboBoxModel<>();
        
        for (File listOfFile : listOfFiles) {
            if (listOfFile.isFile()) {
                comboModel.addElement(listOfFile.getName());
            } 
        }
        
        serverCombo.setModel(comboModel);
    }
    
    /***************************************************************************
    ***  brief                                                               ***
    ***  serial number ????                                                  ***
    ***  parameter out <none>                                                ***
    ***  parameter in  <none>                                                ***
    ***  return <none>                                                       ***
    *** @param                                                               ***
    ***************************************************************************/
    private void createTunnel() {
        try {
            String command = "cmd /c java -jar lib/jTCPfwd.jar 9090 iNET99.Ji8.net:5000";
            this.tunnelProcess = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(this.tunnelProcess.getInputStream()));

            boolean tunnelCreated = false;
            
            String line="";
            while ((line = reader.readLine()) != null && !tunnelCreated) {
           // while((line = reader.readLine()) != null && tunnelCreated == false){
                System.out.println(line);
                if(line.contains("9090->iNET99.Ji8.net:5000")){
                    System.out.println("----------------------------SSH Tunnel created---------------------------");
                    tunnelCreated = true;
                    break;
                }
            }
            
            
            ExecutorTask.setTimeout(() -> this.connectVPN(), 10);
            

        } catch (IOException ex) {
            Logger.getLogger(vpnConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /***************************************************************************
    ***  brief                                                               ***
    ***  serial number ????                                                  ***
    ***  parameter out <none>                                                ***
    ***  parameter in  <none>                                                ***
    ***  return <none>                                                       ***
    *** @param                                                               ***
    ***************************************************************************/
    private void createPuttyTunnel(){
        String pass = String.copyValueOf(passField.getPassword());

        String command = "cmd /c putty -ssh " + userField.getText() + 
                         "@iNET99.Ji8.net -L 9090:iNET99.Ji8.net:5000 -pw " + pass;
       
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            ExecutorTask.setTimeout(() -> this.connectVPN(), 5000);

        } catch (IOException ex) {
            Logger.getLogger(vpnConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /***************************************************************************
    ***  brief                                                               ***
    ***  serial number ????                                                  ***
    ***  parameter out <none>                                                ***
    ***  parameter in  <none>                                                ***
    ***  return <none>                                                       ***
    *** @param                                                               ***
    ***************************************************************************/
    public void connectVPN(){
        
        consoleLabel.setText("connecting to " + serverCombo.getSelectedItem() + "...");
       
        this.task = new ExecutorTask( this , serverCombo.getSelectedItem().toString() );
        this.executorThread = new Thread(task);

        setTimeout(() -> executorThread.start(), 10);
    }
    
    /***************************************************************************
    ***  brief                                                               ***
    ***  serial number ????                                                  ***
    ***  parameter out <none>                                                ***
    ***  parameter in  <none>                                                ***
    ***  return <none>                                                       ***
    *** @param                                                               ***
    ***************************************************************************/
    public static void disconnect(){
        StaticRoutes.flushAddedRoutes();
        try {
            Process process = Runtime.getRuntime().exec("cmd /c taskkill.exe /F /IM openvpn.exe");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            System.out.println("openvpn Process exited.");
            
        } catch (IOException ex) {
            Logger.getLogger(vpnConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /***************************************************************************
    ***  brief                                                               ***
    ***  serial number ????                                                  ***
    ***  parameter out <none>                                                ***
    ***  parameter in  <none>                                                ***
    ***  return <none>                                                       ***
    *** @param                                                               ***
    ***************************************************************************/
    public void connect(){
        String pass = String.copyValueOf(passField.getPassword());

        if( pass.length() == 0 ){
            JOptionPane.showMessageDialog(null, "Please enter a Password!");
            return;
        }

        consoleLabel.setText("creating tunnel to is32...");
        userField.setEnabled(false);
        serverCombo.setEnabled(false);
        passField.setEnabled(false);
        connectBtn.setEnabled(false);
        //puttyCheck.setEnabled(false);
        redirectCheck.setEnabled(false);

        //setTimeout(() -> this.createTunnel(), 10);
        ExecutorTask.setTimeout(() -> this.connectVPN(), 10);
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        verLbl = new javax.swing.JLabel();
        sitesBtn = new javax.swing.JButton();
        logoLbl = new javax.swing.JLabel();
        consoleLabel = new javax.swing.JLabel();
        connectBtn = new javax.swing.JButton();
        ctimeLbl = new javax.swing.JLabel();
        logginPanel = new javax.swing.JPanel();
        serverCombo = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        userField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        redirectCheck = new javax.swing.JCheckBox();
        passField = new javax.swing.JPasswordField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("SDXess");
        setResizable(false);

        verLbl.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        verLbl.setText("Client V1.2.2");

        sitesBtn.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        sitesBtn.setText("Rerouted websites");
        sitesBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sitesBtnActionPerformed(evt);
            }
        });

        logoLbl.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        logoLbl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        logoLbl.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sdxess/SDXess-Logo-Final-small.png"))); // NOI18N

        consoleLabel.setBackground(new java.awt.Color(0, 0, 0));
        consoleLabel.setFont(new java.awt.Font("Arial", 1, 10)); // NOI18N
        consoleLabel.setText("Ready.");

        connectBtn.setText("Connect");
        connectBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectBtnActionPerformed(evt);
            }
        });

        ctimeLbl.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        ctimeLbl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        ctimeLbl.setText("Connection Time");

        jLabel6.setText("Server:");

        jLabel1.setText("User:");

        userField.setText("sdxess");
        userField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                userFieldActionPerformed(evt);
            }
        });

        jLabel2.setText("Password:");

        redirectCheck.setText("redirect All Traffic");

        passField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                passFieldKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                passFieldKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout logginPanelLayout = new javax.swing.GroupLayout(logginPanel);
        logginPanel.setLayout(logginPanelLayout);
        logginPanelLayout.setHorizontalGroup(
            logginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(logginPanelLayout.createSequentialGroup()
                .addGroup(logginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(logginPanelLayout.createSequentialGroup()
                        .addGroup(logginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(jLabel1))
                        .addGap(24, 24, 24))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, logginPanelLayout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                .addGroup(logginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(userField)
                    .addComponent(passField)
                    .addComponent(serverCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .addGroup(logginPanelLayout.createSequentialGroup()
                .addComponent(redirectCheck)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        logginPanelLayout.setVerticalGroup(
            logginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, logginPanelLayout.createSequentialGroup()
                .addGroup(logginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(serverCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(logginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(userField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(logginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addGap(10, 10, 10)
                .addComponent(redirectCheck))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(consoleLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ctimeLbl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(connectBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(logginPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addComponent(verLbl)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 25, Short.MAX_VALUE)
                .addComponent(sitesBtn))
            .addComponent(logoLbl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(logoLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(consoleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(logginPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(connectBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ctimeLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(verLbl, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(sitesBtn, javax.swing.GroupLayout.Alignment.TRAILING)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    
    /***************************************************************************
    ***  brief                                                               ***
    ***  serial number ????                                                  ***
    ***  parameter out <none>                                                ***
    ***  parameter in  <none>                                                ***
    ***  return <none>                                                       ***
    *** @param                                                               ***
    ***************************************************************************/
    private void sitesBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sitesBtnActionPerformed
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                vpnConnect.hostEdit = new HostEdit( websites );
                vpnConnect.hostEdit.setVisible(true);
            }
        });
    }//GEN-LAST:event_sitesBtnActionPerformed

    private void connectBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectBtnActionPerformed
        if( !this.isConnected ){
            this.connect();
        }else{
            this.disconnected(false);
        }
    }//GEN-LAST:event_connectBtnActionPerformed

    private void userFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_userFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_userFieldActionPerformed

    private void passFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_passFieldKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_passFieldKeyPressed

    private void passFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_passFieldKeyReleased
        if( evt.getKeyCode() == 10 ){
            this.connect();
        }
    }//GEN-LAST:event_passFieldKeyReleased

    TimerTask timetask = new TimerTask() {
        @Override
        public void run() {
            seconds++; 
            df.setTimeZone(tz);
            String time = df.format(new Date(seconds*1000));
            ctimeLbl.setText("Connection time: "+time);
            //System.out.println(seconds);
        }
    };
    
    /***************************************************************************
    ***  brief                                                               ***
    ***  serial number ????                                                  ***
    ***  parameter out <none>                                                ***
    ***  parameter in  <none>                                                ***
    ***  return <none>                                                       ***
    *** @param                                                               ***
    ***************************************************************************/
    public void startTimer(){
        seconds = 0;
        if (timerstatus == false){
            timerstatus = true;
            timer.scheduleAtFixedRate(timetask, 1000, 1000);
        }
        //System.out.println("START TIME");
    }

    /***************************************************************************
    ***  brief                                                               ***
    ***  serial number ????                                                  ***
    ***  parameter out <none>                                                ***
    ***  parameter in  <none>                                                ***
    ***  return <none>                                                       ***
    *** @param                                                               ***
    ***************************************************************************/
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
            java.util.logging.Logger.getLogger(vpnConnect.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(vpnConnect.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(vpnConnect.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(vpnConnect.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new vpnConnect().setVisible(true);
            }
        });
    }
   
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton connectBtn;
    private javax.swing.JLabel consoleLabel;
    private javax.swing.JLabel ctimeLbl;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel logginPanel;
    private javax.swing.JLabel logoLbl;
    private javax.swing.JPasswordField passField;
    private javax.swing.JCheckBox redirectCheck;
    private javax.swing.JComboBox<String> serverCombo;
    private javax.swing.JButton sitesBtn;
    private javax.swing.JTextField userField;
    private javax.swing.JLabel verLbl;
    // End of variables declaration//GEN-END:variables

    /***************************************************************************
    ***  brief                                                               ***
    ***  serial number ????                                                  ***
    ***  parameter out <none>                                                ***
    ***  parameter in  <none>                                                ***
    ***  return <none>                                                       ***
    *** @param                                                               ***
    ***************************************************************************/
    private void rerouteSites() {
     
        for (int i = 0; i < this.IPlist.size(); i++) {
            Website website = this.IPlist.get(i);
            
            if( !website.isStatic ){
                System.out.println("Website " + (i+1) +": "+website.name);
                try {
                    StaticRoutes.AddStaticRoute( website.IP );
                } catch (IOException ex) {
                    Logger.getLogger(HostEdit.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        this.startIpTrack();
    }
    
    /***************************************************************************
    ***  brief checks if the ip address of all received websites had change  ***
    ***  serial number ????                                                  ***
    ***  parameter out <none>                                                ***
    ***  parameter in ArrayList<Website>                                     ***
    ***  return <none>                                                       ***
    *** @param                                                               ***
    ***************************************************************************/
    public static void IPCheck(ArrayList<Website> websites){
        try {
            for( int i = 0 ; i < websites.size() ; i++ ){
                Website website = websites.get(i);
                String IP = Website.getClassB( StaticRoutes.NSLookup(website.name) );
                if( !website.isStatic && website.IP.compareTo(IP) != 0 && Website.isIP(IP) ){
                    System.out.println(website.name + " Ip changed, updating routes...");
                    //StaticRoutes.deleteStaticRoute(website.IP);
                    website.IP = IP;
                    StaticRoutes.AddStaticRoute(website.IP);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(vpnConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /***************************************************************************
    ***  brief checks every 5 seconds if the ip of a site changed            ***
    ***  serial number ????                                                  ***
    ***  parameter out <none>                                                ***
    ***  parameter in  <none>                                                ***
    ***  return <none>                                                       ***
    *** @param                                                               ***
    ***************************************************************************/
    private void IpTrack(){
        if( this.isConnected ){
            vpnConnect.IPCheck(this.IPlist);
            ExecutorTask.setTimeout(() -> this.IpTrack(), 5000);
        }
    }
    
    /***************************************************************************
    ***  brief FIrst run for IPtrack function                                ***
    ***  serial number ????                                                  ***
    ***  parameter out <none>                                                ***
    ***  parameter in  <none>                                                ***
    ***  return <none>                                                       ***
    ***************************************************************************/
    private void startIpTrack(){
        ExecutorTask.setTimeout(() -> this.IpTrack(), 5000);
    }

    /***************************************************************************
    ***  brief this function check the commit id from git and print it in    ***
    ***        the console, if it doesn't find it doesn't print anything     ***
    ***  serial number ????                                                  ***
    ***  parameter out <none>                                                ***
    ***  parameter in  <none>                                                ***
    ***  return <none>                                                       ***
    ***************************************************************************/
    public void checkCommit(){
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader("../.git/FETCH_HEAD"));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
  
            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            String everything = sb.toString();
            System.out.println("Commit# " + everything.split("\t")[0]);
        } catch (FileNotFoundException ex) {
            System.out.println("-commit not found-");
        } catch (IOException ex) {
            System.out.println("-commit not found-");
        }
    }

    void stablishedCommunication() {
        this.connectionEstablished = true;
    }
    
}
