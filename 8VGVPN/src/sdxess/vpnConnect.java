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
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import org.json.JSONObject;
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
    private ArrayList<Website> temp_websites = null;
    
    //timer variables
    private int seconds = 0;
    private boolean timerstatus = false;
    private Timer timer = new Timer(); 
    private TimeZone tz = TimeZone.getTimeZone("UTC");
    private SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
    public static HostEdit hostEdit = null;
    public static boolean controlPanelOpen = false;
    private SystemTray tray = null;
    private TrayIcon trayIcon = null;
    
    private int originalHeight = 0;
    private TimerTask timetask = null;
    private String user = "";
    
    /***************************************************************************
    ***  brief this is the main function that triggers all the connection    ***
    ***        to openvpn                                                    ***
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
            
            if( Console.isAdmin ){
                consoleLabel.setText("Logging in " + 
                        serverCombo.getSelectedItem() + "...");
            }else{
                consoleLabel.setText("Logging in to SDXess...");
            }
            
            userField.setEnabled(false);
            serverCombo.setEnabled(false);
            passField.setEnabled(false);
            connectBtn.setEnabled(false);
            signupLbl.setEnabled(false);
            
            this.repaint();
            
            JSONObject user = StaticRoutes.checkLogin( this.userField.getText() , pass);
            if( user == null ){
                userField.setEnabled(true);
                serverCombo.setEnabled(true);
                passField.setEnabled(true);
                connectBtn.setEnabled(true);
                consoleLabel.setText("User/Password is incorrect.");
                passField.setText("");
                return;
            }
            
            this.user = (String)user.get("name");
            
            int accountTypeNum = Integer.parseInt((String)user.get("data_plan"));

            boolean valid = true;
            switch (accountTypeNum) {
                case 1:
                    HostEdit.accountType = ACType.BASIC;
                    break;
                case 2:
                    HostEdit.accountType = ACType.STARTER;
                    break;
                case 3:
                    HostEdit.accountType = ACType.ADVANCED;
                    break;
                default:
                    valid = false;
                    HostEdit.accountType = ACType.NOTYPE;
                    break;
            }
            
            if( valid ){
                this.connect2vpn(false,null);
            } else {
                JOptionPane.showMessageDialog(this, "Your account is not active, please visit http://sdxess.com to check the status of your account.");
                userField.setEnabled(true);
                serverCombo.setEnabled(true);
                passField.setEnabled(true);
                connectBtn.setEnabled(true);
                passField.setText("");
                consoleLabel.setText("Ready.");
            }
        }
    }
    
    public void connect2vpn( boolean redirectAllTraffic , ArrayList<Website> final_websites ){
        if( Console.isAdmin ){
            consoleLabel.setText("connecting to " + 
                    serverCombo.getSelectedItem() + "...");

            this.task = new ExecutorTask( this , 
                    serverCombo.getSelectedItem().toString() , false , null );
            this.executorThread = new Thread(task);
            setTimeout(() -> executorThread.start(), 10);
        }else{
            if( final_websites != null ){
                this.temp_websites = final_websites;
                this.task = new ExecutorTask( this , 
                    serverCombo.getSelectedItem().toString() , redirectAllTraffic , final_websites );
                this.executorThread = new Thread(task);
                setTimeout(() -> executorThread.start(), 10);
            } else {
                this.prepareWebsitesNoAdmin();
                java.awt.EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        vpnConnect.hostEdit = new HostEdit( websites , true , false );
                        vpnConnect.hostEdit.setVisible(true);
                    }
                });
            }
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
    public void disconnect(boolean byError , Runnable callback ){
        if( !this.isConnected ){
            if( callback != null )
                callback.run();
            return;
        }
        
        consoleLabel.setText("disconnecting...");
        connectBtn.setEnabled(false);
        sitesBtn.setEnabled(false);
        hideBtn.setEnabled(false);
        
        if( vpnConnect.hostEdit != null )
            vpnConnect.hostEdit.setVisible(false);

        Thread t = ExecutorTask.setTimeout(() -> this.disconnectFromVPN(byError,callback), 10);
        
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
        
        
        URL iconURL = getClass().getResource("/sdxess/icon.png");
        ImageIcon icon = new ImageIcon(iconURL);
        this.setIconImage(icon.getImage());
        
        
        this.killOpenvpn();
        this.checkCommit();
        
        initComponents();
        verLbl.setText("Client V" + StaticRoutes.version);
        this.getConfs();
        sitesBtn.setVisible(false);
        hideBtn.setVisible(false);
        ctimeLbl.setVisible(false);
        updateLbl.setVisible(false);
        
        this.setLocation(100, 100);
        //this.setSize(this.getWidth(), 280);
        this.originalHeight = this.getHeight();
      
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                Runnable callback = ()->exit();
                disconnect(false, callback );
            }
        });
        
  
       
    }
    
    public void exit(){
        Console.log("SDXess finished correctly!");
        System.exit(0);
    }
    
    /***************************************************************************
    ***  brief                                                               ***
    ***  serial number ????                                                  ***
    ***  parameter out <none>                                                ***
    ***  parameter in  <none>                                                ***
    ***  return <none>                                                       ***
    *** @param                                                               ***
    ***************************************************************************/
    public void connected( ArrayList<String> sites2reroute , ArrayList<String> website_domains){
        StaticRoutes.Start();
        
        this.IPlist = new ArrayList<Website>();
        consoleLabel.setText("Connected to " + serverCombo.getSelectedItem() + " as " + this.user );
        
        ctimeLbl.setVisible(true);
        logginPanel.setVisible(false);
        this.repaint();
        
        if( Console.isAdmin ){
            ctimeLbl.setText("Rerouting default websites...");
        
            //this.setSize(this.getWidth(), 200);
            

            this.websites = new ArrayList<>();

            ArrayList<Website> restored_sites = HostEdit.restoreWebsites();

            if( restored_sites != null ){
                if( !restored_sites.isEmpty() ){
                    for( Website website : restored_sites ){
                        ctimeLbl.setText("Restoring " + website.name + "...");
                        this.websites.add(website);
                        if( website.wasRerouted )
                            website.route();
                    }
                }

                for( String domain : website_domains ){
                    boolean addDefaultSite = true;
                    String[] parts = domain.split(":");

                    for( Website other : this.websites ){
                        if( other.ASN.compareTo(parts[1]) == 0 ){
                            addDefaultSite = false;
                        }
                    }

                    if( addDefaultSite ){
                       ctimeLbl.setText("Reading " + parts[0] + " from server... ");
                        Website website = new Website(parts[0]);
                        this.websites.add( website  );
                    }
                }

                if( HostEdit.accountType == ACType.BASIC ){
                    updateLbl.setVisible(true);
                }else{
                    sitesBtn.setVisible(true);
                    sitesBtn.setEnabled(true);
                    HostEdit.saveWebsites(this.websites);
                }
            }

            StaticRoutes.flushDNS();
        }else{
            if( HostEdit.accountType == ACType.BASIC ){
                updateLbl.setVisible(true);
            }else{
                sitesBtn.setVisible(true);
                sitesBtn.setEnabled(true);
            }
        }

        this.startTimer();
        this.isConnected = true;
        connectBtn.setText("Disconnect");
        connectBtn.setEnabled(true);
        hideBtn.setVisible(true);
        signupLbl.setVisible(false);
        this.hideOnTray();
    }
    
    public void prepareWebsitesNoAdmin(){
        this.websites = new ArrayList<>();
        ArrayList<Website> restored_sites = HostEdit.restoreWebsites();
        if( restored_sites != null ){
            if( !restored_sites.isEmpty() ){
                for( Website website : restored_sites ){
                    ctimeLbl.setText("Restoring " + website.name + "...");
                    this.websites.add(website);
                    
                }
            }
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
        updateLbl.setVisible(false);
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
            this.disconnect(true,null);
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
        passField.setText("");
        connectBtn.setEnabled(true);
        ctimeLbl.setVisible(false);
        ctimeLbl.setText("");
    }
    
    public void disconnectFromVPN(boolean byError, Runnable callback){
        if( this.task != null )
            this.task.end();
        
        StaticRoutes.flushAddedRoutes();
        
        websites.forEach((website) -> {
            consoleLabel.setText("disconnecting from " + website.name + "...");
            website.deleteRouting();
        }); 
        
        this.isConnected = false;
        
        connectBtn.setText("Connect");
        connectBtn.setEnabled(true);
        ctimeLbl.setVisible(false);
        ctimeLbl.setText("");
        passField.setText("");
        
        hideBtn.setVisible(false);
        hideBtn.setEnabled(true);
        
        if( HostEdit.accountType == ACType.BASIC ){
            updateLbl.setVisible(false);
        }else{
            sitesBtn.setVisible(false);
            sitesBtn.setEnabled(true);
        }
        
        userField.setEnabled(true);
        passField.setEnabled(true);
        serverCombo.setEnabled(true);
        logginPanel.setVisible(true);
        signupLbl.setEnabled(true);
        signupLbl.setVisible(true);
        
        this.tray.remove(this.trayIcon);
        this.trayIcon = null;
  
        this.setSize( this.getWidth() , this.originalHeight );
        if( byError )
            consoleLabel.setText("disconnected due error.");
        else
            consoleLabel.setText("Ready.");
        
        
        this.seconds = 0;
        this.stopTimer();
        
        this.repaint();
        
        this.killOpenvpn();
        
        if( callback != null )
            callback.run();
    }
    
    public void killOpenvpn(){
        try {
            Process process = Runtime.getRuntime().exec("cmd /c taskkill.exe /F /IM openvpn.exe");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            Console.log("openvpn Process exited.");
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
                //Console.log(seconds);
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
        if( this.trayIcon != null ){
            this.setVisible(false);
            return;
        }
        
        if (SystemTray.isSupported()) {
            // get the SystemTray instance
            this.tray = SystemTray.getSystemTray();
            // load an image           
            try {
                BufferedImage trayIconImage = ImageIO.read(getClass().getResource("/sdxess/trayicon.png"));
           
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
        logoLbl = new javax.swing.JLabel();
        consoleLabel = new javax.swing.JLabel();
        ctimeLbl = new javax.swing.JLabel();
        logginPanel = new javax.swing.JPanel();
        serverCombo = new javax.swing.JComboBox<>();
        serverLbl = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        userField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        passField = new javax.swing.JPasswordField();
        absolutePanel = new javax.swing.JPanel();
        hideBtn = new javax.swing.JButton();
        signupLbl = new javax.swing.JLabel();
        absolutePanelStatus = new javax.swing.JPanel();
        sitesBtn = new javax.swing.JButton();
        updateLbl = new javax.swing.JLabel();
        centerPanel = new javax.swing.JPanel();
        connectBtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("SDXess");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        verLbl.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        verLbl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        verLbl.setText("Client VX.X.X");
        verLbl.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        logoLbl.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        logoLbl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        logoLbl.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sdxess/title.png"))); // NOI18N

        consoleLabel.setBackground(new java.awt.Color(0, 0, 0));
        consoleLabel.setFont(new java.awt.Font("Arial", 1, 10)); // NOI18N
        consoleLabel.setText("Ready.");

        ctimeLbl.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        ctimeLbl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        ctimeLbl.setText("Connection Time");

        serverLbl.setText("Server:");

        jLabel1.setText("User:");

        userField.setText("sdxess3@ji8.net");
        userField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                userFieldActionPerformed(evt);
            }
        });
        userField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                userFieldKeyPressed(evt);
            }
        });

        jLabel2.setText("Password:");

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
                            .addComponent(serverLbl)
                            .addComponent(jLabel1))
                        .addGap(24, 24, 24))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, logginPanelLayout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                .addGroup(logginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(userField, javax.swing.GroupLayout.DEFAULT_SIZE, 303, Short.MAX_VALUE)
                    .addComponent(passField)
                    .addComponent(serverCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        logginPanelLayout.setVerticalGroup(
            logginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, logginPanelLayout.createSequentialGroup()
                .addGroup(logginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(serverCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(serverLbl))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(logginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(userField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(logginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addGap(50, 50, 50))
        );

        absolutePanel.setFocusable(false);
        absolutePanel.setMaximumSize(new java.awt.Dimension(59, 18));
        absolutePanel.setMinimumSize(new java.awt.Dimension(59, 48));
        absolutePanel.setPreferredSize(new java.awt.Dimension(59, 18));
        absolutePanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        hideBtn.setText("_");
        hideBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        hideBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hideBtnActionPerformed(evt);
            }
        });
        absolutePanel.add(hideBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 0, -1, -1));

        signupLbl.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        signupLbl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        signupLbl.setText("<html> <a href=\"\">Sign up</a></html>");
        signupLbl.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        signupLbl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                signupLblMouseClicked(evt);
            }
        });
        absolutePanel.add(signupLbl, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 5, 39, -1));

        absolutePanelStatus.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        sitesBtn.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        sitesBtn.setText("Control Panel");
        sitesBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        sitesBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sitesBtnActionPerformed(evt);
            }
        });
        absolutePanelStatus.add(sitesBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 0, -1, -1));

        updateLbl.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        updateLbl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        updateLbl.setText("<html> <a href=\"\">Upgrade your account!</a></html>");
        updateLbl.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        updateLbl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                updateLblMouseClicked(evt);
            }
        });
        absolutePanelStatus.add(updateLbl, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 120, 20));

        connectBtn.setText("Connect");
        connectBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        connectBtn.setMaximumSize(new java.awt.Dimension(153, 23));
        connectBtn.setMinimumSize(new java.awt.Dimension(153, 23));
        connectBtn.setPreferredSize(new java.awt.Dimension(153, 23));
        connectBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectBtnActionPerformed(evt);
            }
        });
        centerPanel.add(connectBtn);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(verLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(absolutePanelStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(logoLbl, javax.swing.GroupLayout.DEFAULT_SIZE, 310, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(absolutePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(ctimeLbl, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(centerPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(logginPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(consoleLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(logoLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(absolutePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(1, 1, 1)
                .addComponent(consoleLabel)
                .addGap(18, 18, 18)
                .addComponent(logginPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(centerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ctimeLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 46, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(verLbl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(absolutePanelStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
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
        if( !vpnConnect.controlPanelOpen ){
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    if( Console.isAdmin )
                        vpnConnect.hostEdit = new HostEdit( websites , false , false);
                    else
                        vpnConnect.hostEdit = new HostEdit( temp_websites , true , true );
                    vpnConnect.hostEdit.setVisible(true);
                }
            });
        }
    }//GEN-LAST:event_sitesBtnActionPerformed

    private void connectBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectBtnActionPerformed
        if( !this.isConnected ){
            this.connect();
        }else{
            this.disconnect(false,null);
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

    private void signupLblMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_signupLblMouseClicked
        Website.openSDXWebsite();
    }//GEN-LAST:event_signupLblMouseClicked

    private void hideBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hideBtnActionPerformed
        this.hideOnTray();
    }//GEN-LAST:event_hideBtnActionPerformed

    private void updateLblMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_updateLblMouseClicked
        Website.openSDXWebsite();
    }//GEN-LAST:event_updateLblMouseClicked

    private void userFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_userFieldKeyPressed
        if( evt.getKeyCode() == 10 ){
            this.connect();
        }
    }//GEN-LAST:event_userFieldKeyPressed

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
    
        
        JSONObject response = StaticRoutes.checkVersion();
        String version = response.getString("version");
        if( version.compareTo(StaticRoutes.version) != 0 ){
            try {
                JOptionPane.showMessageDialog(null, "This Version of SDXess is outdated! please update your SDXess version! current:" + 
                        StaticRoutes.version + " update:" + version);
                java.awt.Desktop.getDesktop().browse(new URI(response.getString("download")));
            } catch (URISyntaxException ex) {
                Logger.getLogger(vpnConnect.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(vpnConnect.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            System.exit(0);
        }
        
        
        
        Console.isAdmin = StaticRoutes.isAdmin();
        
        vpnConnect.checkInstances();
        Console.log("SDXess App Started!");
       
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                Console.vpnconnect = new vpnConnect();
                Console.vpnconnect.setVisible(true);
            }
        });
    }
    
    public static void checkInstances(){
        try {
            if( checkIfAlreadyRunning() ){
                JOptionPane.showMessageDialog(null,"Another SDXess Instance is already running in this machines!");
                System.exit(0);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null,"Another SDXess Instance is already running in this machines or Try to run this program as Administrator!");
            System.exit(0);
        }
    }
    
    static File file;
    static FileChannel fileChannel;
    static FileLock lock;
    static boolean running = false;

    @SuppressWarnings("resource")
    public static boolean checkIfAlreadyRunning() throws IOException {
        file = new File("app.lock");
        if (!file.exists()) {
            file.createNewFile();
            running = false;
        } else {
            file.delete();
        }

        fileChannel = new RandomAccessFile(file, "rw").getChannel();
        lock = fileChannel.tryLock();

        if (lock == null) {
            fileChannel.close();
            return true;
        } 
        ShutdownHook shutdownHook = new ShutdownHook();
        Runtime.getRuntime().addShutdownHook(shutdownHook);

        return running;
    }

    public static void unlockFile() {
        try {
            if (lock != null)
                lock.release();
            fileChannel.close();
            file.delete();
            running = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ShutdownHook extends Thread {
        public void run() {
            unlockFile();
        }
    }
   
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel absolutePanel;
    private javax.swing.JPanel absolutePanelStatus;
    private javax.swing.JPanel centerPanel;
    private javax.swing.JButton connectBtn;
    private javax.swing.JLabel consoleLabel;
    private javax.swing.JLabel ctimeLbl;
    private javax.swing.JButton hideBtn;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel logginPanel;
    private javax.swing.JLabel logoLbl;
    private javax.swing.JPasswordField passField;
    private javax.swing.JComboBox<String> serverCombo;
    private javax.swing.JLabel serverLbl;
    private javax.swing.JLabel signupLbl;
    private javax.swing.JButton sitesBtn;
    private javax.swing.JLabel updateLbl;
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
            Console.log("Commit# " + everything.split("\t")[0]);
        } catch (FileNotFoundException ex) {
            Console.log("-commit not found-");
        } catch (IOException ex) {
            Console.log("-commit not found-");
        }
    }
}
