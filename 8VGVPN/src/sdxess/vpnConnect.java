/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sdxess;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
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
import javax.swing.JOptionPane;
import static sdxess.ExecutorTask.setTimeout;
/**
 *
 * @author kotaro
 */
public class vpnConnect extends javax.swing.JFrame {

    public ExecutorTask task;
    public Thread executorThread;
    private StringBuilder sb;
    private BufferedReader br;
    private ArrayList<String> sites2reroute;
    
    //timer variables
    private int seconds = 0;
    private boolean timerstatus = false;
    private Timer timer = new Timer(); 
    private TimeZone tz = TimeZone.getTimeZone("UTC");
    private SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
    
    /**
     * Creates new form vpnConnect
     */
    public vpnConnect(){
        initComponents();
        disBtn.setEnabled(false);
        sitesBtn.setVisible(false);
        
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                vpnConnect.windowClosing();
            }
        });
        ctime.hide();
       
    }
    
    /***************************************************************************
    ***  brief creates a new SDXess window                                   ***
    ***  serial number ????                                                  ***
    ***  parameter out <none>                                                ***
    ***  parameter in  <none>                                                ***
    ***  return <none>                                                       ***
    ***************************************************************************/
    public void connected( ArrayList<String> sites2reroute ){
        jPanel1.setEnabled(false);
        consoleLabel.setText("Connected to " + serverCombo.getSelectedItem() + " as " + userField.getText());
        disBtn.setEnabled(true);
        sitesBtn.setVisible(true);
        ctime.show();
        this.rerouteSites(sites2reroute);
        this.startTimer();
        try {
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
        }
        
    }
    
    public void reconnecting(){
        consoleLabel.setText("Communication lost... reconnecting...");
        userField.setEnabled(false);
        serverCombo.setEnabled(false);
        passField.setEnabled(false);
        connectBtn.setEnabled(false);
        sitesBtn.setVisible(false);
        disBtn.setEnabled(false);
        ctime.hide();
        ctime.setText("");
        
    }
    
    public void notconnected(String message){
        consoleLabel.setText("Connection error: " + message);
        userField.setEnabled(true);
        serverCombo.setEnabled(true);
        passField.setEnabled(true);
        connectBtn.setEnabled(true);
        ctime.hide();
        ctime.setText("");
        
    }

    /*Interbnal functions*/
    private void createTunnel() {
        try {
            String command = "cmd /c java -jar lib/jTCPfwd.jar 9090 iNET99.Ji8.net:5000";
            Process process = Runtime.getRuntime().exec("cmd /c cd");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            System.out.println(reader.readLine());
            
            process = Runtime.getRuntime().exec(command);
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            boolean tunnelCreated = false;
            
            String line="";
            while ((line = reader.readLine()) != null && !tunnelCreated) {
           // while((line = reader.readLine()) != null && tunnelCreated == false){
                System.out.println(line);
                if(line.contains("forwarders started")){
                    System.out.println("Tunnel created");
                    tunnelCreated = true;
                    break;
                }
            }
            ExecutorTask.setTimeout(() -> this.connectVPN(), 10);
            

        } catch (IOException ex) {
            Logger.getLogger(vpnConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void connectVPN(){
        consoleLabel.setText("connecting to " + serverCombo.getSelectedItem() + "...");
       
        this.task = new ExecutorTask( this , serverCombo.getSelectedItem().toString() );
        this.executorThread = new Thread(task);

        setTimeout(() -> executorThread.start(), 10);
    }
    
    public static void windowClosing(){
        try {
            Process process = Runtime.getRuntime().exec("cmd /c taskkill.exe /F /IM openvpn.exe");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            System.out.println("openvpn Process exited.");
        } catch (IOException ex) {
            Logger.getLogger(vpnConnect.class.getName()).log(Level.SEVERE, null, ex);
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

        jLayeredPane1 = new javax.swing.JLayeredPane();
        jPanel1 = new javax.swing.JPanel();
        connectBtn = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        userField = new javax.swing.JTextField();
        passField = new javax.swing.JPasswordField();
        disBtn = new javax.swing.JButton();
        consoleLabel = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        serverCombo = new javax.swing.JComboBox<>();
        ctime = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        sitesBtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("SDXess");

        connectBtn.setText("Connect");
        connectBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectBtnActionPerformed(evt);
            }
        });

        jLabel1.setText("User:");

        jLabel2.setText("Password:");

        userField.setText("sdxess");

        disBtn.setText("Disconnect");
        disBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                disBtnActionPerformed(evt);
            }
        });

        consoleLabel.setBackground(new java.awt.Color(0, 0, 0));
        consoleLabel.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        consoleLabel.setText("Ready.");

        jLabel6.setText("Server:");

        serverCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "sdxess-is32", "sdxess", "locallinux" }));

        ctime.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        ctime.setText("Connection Time");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(52, 52, 52)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(consoleLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel6)
                                        .addGap(18, 18, 18)
                                        .addComponent(serverCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel2)
                                            .addComponent(jLabel1))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(passField, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(userField, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(95, 95, 95)
                        .addComponent(connectBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(disBtn)))
                .addGap(40, 40, 40))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ctime, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(consoleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(serverCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 13, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(userField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(passField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ctime)
                .addGap(7, 7, 7)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(connectBtn)
                    .addComponent(disBtn))
                .addGap(18, 18, 18))
        );

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sdxess/SDXess-Logo-Final-small.png"))); // NOI18N

        jLayeredPane1.setLayer(jPanel1, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(jLabel3, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout jLayeredPane1Layout = new javax.swing.GroupLayout(jLayeredPane1);
        jLayeredPane1.setLayout(jLayeredPane1Layout);
        jLayeredPane1Layout.setHorizontalGroup(
            jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jLayeredPane1Layout.setVerticalGroup(
            jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jLayeredPane1Layout.createSequentialGroup()
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel5.setText("Desktop Client V1.1.3");

        sitesBtn.setText("Rerouted sites");
        sitesBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sitesBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLayeredPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(sitesBtn))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLayeredPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sitesBtn)
                    .addComponent(jLabel5)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void disBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_disBtnActionPerformed
        this.task.end();
        disBtn.setEnabled(false);
        sitesBtn.setVisible(false);
        jPanel1.setVisible(true);
        userField.setEnabled(true);
        passField.setEnabled(true);
        serverCombo.setEnabled(true);
        connectBtn.setEnabled(true);
        consoleLabel.setText("disconnected.");
        ctime.hide();
        ctime.setText("");
        
        //restore host file
        String file = sb.toString(); 

        vpnConnect.windowClosing();
        HostEdit.saveHosts(file);
    }//GEN-LAST:event_disBtnActionPerformed

    private void sitesBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sitesBtnActionPerformed
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new HostEdit(sites2reroute).setVisible(true);
            }
        });
    }//GEN-LAST:event_sitesBtnActionPerformed

    private void connectBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectBtnActionPerformed
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

        setTimeout(() -> this.createTunnel(), 10);
        
    }//GEN-LAST:event_connectBtnActionPerformed

    TimerTask timetask = new TimerTask() {
        @Override
        public void run() {
            seconds++; 
            df.setTimeZone(tz);
            String time = df.format(new Date(seconds*1000));
            ctime.setText("Connection time: "+time);
            //System.out.println(seconds);
        }
    };
    public void startTimer(){
        seconds = 0;
        if (timerstatus == false){
            timerstatus = true;
            timer.scheduleAtFixedRate(timetask, 1000, 1000);
        }
        //System.out.println("START TIME");
    }
/***END CONNECTION TIMER***/   
    
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
    private javax.swing.JLabel ctime;
    private javax.swing.JButton disBtn;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLayeredPane jLayeredPane1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPasswordField passField;
    private javax.swing.JComboBox<String> serverCombo;
    private javax.swing.JButton sitesBtn;
    private javax.swing.JTextField userField;
    // End of variables declaration//GEN-END:variables

    private void rerouteSites(ArrayList<String> sites2reroute) {
        this.sites2reroute = sites2reroute;
        StaticRoutes SR = new StaticRoutes();
        String website;
        for (int i = 0; i < sites2reroute.size(); i++) {
            website = sites2reroute.get(i);
            System.out.println("Website " + i +": "+website);
            try {
                SR.AddStaticRoute(SR.NSLookup(website), SR.GetTAPInfo(11), SR.GetTAPInfo(6));
            } catch (IOException ex) {
                Logger.getLogger(HostEdit.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
