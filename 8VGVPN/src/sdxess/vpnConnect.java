/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sdxess;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import static sdxess.ExecutorTask.setTimeout;

/**
 *
 * @author kotaro
 */
public class vpnConnect extends javax.swing.JFrame {
    
    public ExecutorTask task = null;
    public Thread executorThread = null;
    private boolean isConnected = false;
    private boolean connectionEstablished = false;
    private StringBuilder sb = null;
    private BufferedReader br = null;
    private ArrayList<Website> IPlist = null;
    private ArrayList<Website> websites = new ArrayList<>();
    
    //timer variables
    private int seconds = 0;
    private boolean timerstatus = false;
    private Timer timer = new Timer(); 
    private TimeZone tz = TimeZone.getTimeZone("UTC");
    private SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
    public static HostEdit hostEdit = null;
    private SystemTray tray = null;
    private TrayIcon trayIcon = null;
    
    private int originalHeight = 0;
    private TimerTask timetask = null;
    
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
        }else{
            
            consoleLabel.setText("Logging in " + 
                    serverCombo.getSelectedItem() + "...");
            
            userField.setEnabled(false);
            serverCombo.setEnabled(false);
            passField.setEnabled(false);
            connectBtn.setEnabled(false);
            redirectCheck.setEnabled(false);
            
            this.repaint();
            
            if( !StaticRoutes.checkLogin( this.userField.getText() , pass) ){
                userField.setEnabled(true);
                serverCombo.setEnabled(true);
                passField.setEnabled(true);
                connectBtn.setEnabled(true);
                redirectCheck.setEnabled(true);
                consoleLabel.setText("User/Password is incorrect.");
                return;
            }
            
            consoleLabel.setText("connecting to " + 
                    serverCombo.getSelectedItem() + "...");
           
            this.task = new ExecutorTask( this , 
                    serverCombo.getSelectedItem().toString() );
            this.executorThread = new Thread(task);
            setTimeout(() -> executorThread.start(), 10);
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
    public void disconnect(boolean byError){
        consoleLabel.setText("disconnecting...");
        connectBtn.setEnabled(false);
        sitesBtn.setEnabled(false);
        hideBtn.setEnabled(false);
        
        if( vpnConnect.hostEdit != null )
            vpnConnect.hostEdit.setVisible(false);

        ExecutorTask.setTimeout(() -> this.disconnectFromVPN(byError), 10);   
    }
    
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
        
        this.killOpenvpn();
        this.checkCommit();
        
        initComponents();
        this.getConfs();
        sitesBtn.setVisible(false);
        hideBtn.setVisible(false);
        ctimeLbl.setVisible(false);
        
        this.setLocation(100, 100);
        this.setSize(this.getWidth(), 280);
        this.originalHeight = this.getHeight();
      
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                disconnect(false);
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
    public void connected( ArrayList<String> sites2reroute , ArrayList<String> websites){
        StaticRoutes.Start();
        
        this.IPlist = new ArrayList<Website>();
        consoleLabel.setText("Connected to " + serverCombo.getSelectedItem());
        
        ctimeLbl.setText("Rerouting default websites...");
        ctimeLbl.setVisible(true);
        logginPanel.setVisible(false);
        this.setSize(this.getWidth(), 177);
        this.repaint();
        
        this.websites = new ArrayList<>();
        for( String domain : websites ){
            ctimeLbl.setText("Crawling " + domain + " IP's...");
            Website website = new Website(domain);
            this.websites.add( website  );
            ctimeLbl.setText("Rerouting " + website.name + "...");
            website.route();
        }
   
        if( !redirectCheck.isSelected() ){
            StaticRoutes.disableAllTrafficReroute();
            sitesBtn.setVisible(true);
        }
        StaticRoutes.flushDNS();

       
        this.startTimer();
        this.isConnected = true;
        connectBtn.setText("Disconnect");
        connectBtn.setEnabled(true);
        hideBtn.setVisible(true);
        
        this.hideOnTray();
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
    public void reconnecting(){
        connectBtn.setEnabled(false);
        sitesBtn.setVisible(false);
        hideBtn.setVisible(false);
        ctimeLbl.setVisible(false);
        ctimeLbl.setText("");
      
        if( this.isConnected ){
            consoleLabel.setText("Communication lost... reconnecting...");
            this.respawn();
        }else{
            consoleLabel.setText("Unstable connection... reconnecting...");
        }
        
        this.isConnected = false;
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
            this.disconnect(true);
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
        redirectCheck.setEnabled(true);
        ctimeLbl.setVisible(false);
        ctimeLbl.setText("");
    }
    
    public void disconnectFromVPN(boolean byError){
        if( this.task != null )
            this.task.end();
        
        StaticRoutes.flushAddedRoutes();
        
        websites.forEach((website) -> {
            website.deleteRouting();
        }); 
        
        this.isConnected = false;
        
        connectBtn.setText("Connect");
        connectBtn.setEnabled(true);
        sitesBtn.setVisible(false);
        sitesBtn.setEnabled(true);
        hideBtn.setVisible(false);
        hideBtn.setEnabled(true);
        
        userField.setEnabled(true);
        passField.setEnabled(true);
        serverCombo.setEnabled(true);
        redirectCheck.setEnabled(true);
        logginPanel.setVisible(true);
  
        this.setSize( this.getWidth() , this.originalHeight );
        if( byError )
            consoleLabel.setText("disconnected due error.");
        else
            consoleLabel.setText("Ready.");
        ctimeLbl.setVisible(false);
        ctimeLbl.setText("");
        this.seconds = 0;
        this.stopTimer();
        
        this.killOpenvpn();
    }
    
    public void killOpenvpn(){
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
    private void getConfs(){
       File folder = new File("confs");
        File[] listOfFiles = folder.listFiles();

        javax.swing.DefaultComboBoxModel<String> comboModel = new javax.swing.DefaultComboBoxModel<>();
        
        for (File listOfFile : listOfFiles) {
            if (listOfFile.isFile()) {
                comboModel.addElement(listOfFile.getName());
            } 
        }
        
        serverCombo.setModel(comboModel);
    }
    
    public void respawn(){
        setVisible(true);
        tray.remove(trayIcon);
    }
    
    
    
    /***************************************************************************
    ***  brief                                                               ***
    ***  serial number ????                                                  ***
    ***  parameter out <none>                                                ***
    ***  parameter in  <none>                                                ***
    ***  return <none>                                                       ***
    *** @param                                                               ***
    ***************************************************************************/
    public void startTimer(){
        timetask = new TimerTask() {
            @Override
            public void run() {
                seconds++; 
                df.setTimeZone(tz);
                String time = df.format(new Date(seconds*1000));
                ctimeLbl.setText("Connection time: "+time);
                //System.out.println(seconds);
            }
        };
        seconds = 0;
        if (timerstatus == false){
            timerstatus = true;
            timer.scheduleAtFixedRate(timetask, 1000, 1000);
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
    public void stopTimer(){
        this.timetask.cancel();
        this.timetask = null;
        timerstatus = false;
    }
    
    /***************************************************************************
    ***  brief                                                               ***
    ***  serial number ????                                                  ***
    ***  parameter out <none>                                                ***
    ***  parameter in  <none>                                                ***
    ***  return <none>                                                       ***
    *** @param                                                               ***
    ***************************************************************************/
    public void hideOnTray(){
        if (SystemTray.isSupported()) {
            // get the SystemTray instance
            this.tray = SystemTray.getSystemTray();
            // load an image           
            try {
                BufferedImage trayIconImage = ImageIO.read(getClass().getResource("/sdxess/icon.png"));
           
                // create a action listener to listen for default action executed on the tray icon
                ActionListener listener = new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        respawn();
                    }
                };
                PopupMenu popup = new PopupMenu();
                MenuItem defaultItem = new MenuItem();
                defaultItem.addActionListener(listener);
                popup.add(defaultItem);
                int trayIconWidth = new TrayIcon(trayIconImage).getSize().width;
                int trayIconHeight = new TrayIcon(trayIconImage).getSize().height;
                this.trayIcon = new TrayIcon(trayIconImage.getScaledInstance(trayIconWidth, trayIconHeight, Image.SCALE_SMOOTH));
                this.trayIcon.addActionListener(listener);
                try {
                    tray.add(this.trayIcon);
                } catch (AWTException e) {
                    System.err.println(e);
                }
                this.setVisible(false);
                
            } catch (IOException ex) {
                Logger.getLogger(vpnConnect.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            // disable tray option in your application or
            // perform other actions
        }
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
        hideBtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("SDXess");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setResizable(false);

        verLbl.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        verLbl.setText("Client V1.2.10");

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

        hideBtn.setText("_");
        hideBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hideBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(verLbl)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(sitesBtn))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(ctimeLbl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(connectBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(logginPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(consoleLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGap(56, 56, 56)
                        .addComponent(logoLbl)
                        .addGap(18, 18, 18)
                        .addComponent(hideBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(hideBtn)
                    .addComponent(logoLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(consoleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
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
            this.disconnect(false);
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

    private void hideBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hideBtnActionPerformed
       this.hideOnTray();
    }//GEN-LAST:event_hideBtnActionPerformed

    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
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
    private javax.swing.JButton hideBtn;
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
}
