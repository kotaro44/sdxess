/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/*Git test*/
//=======
package sdxess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;

public class ExecutorTask implements Runnable{
    private vpnConnect frame = null;
    private Process process = null;
    private boolean connected = false;
    private boolean abortTimeOut = false;
    private boolean redirectAllTraffic = false;
    private String server = "";
    private ArrayList<Website> final_websites = null;
    
    /***************************************************************************
    ***  brief                                                               ***
    ***  serial number ????                                                  ***
    ***  parameter out <none>                                                ***
    ***  parameter in  <none>                                                ***
    ***  return <none>                                                       ***
    *** @param                                                               ***
    ***************************************************************************/
    public ExecutorTask(vpnConnect frame, String server , 
            boolean redirectAllTraffic , ArrayList<Website> final_websites ) {
        this.frame = frame;
        this.server = server;
        this.redirectAllTraffic = redirectAllTraffic;
        this.final_websites = final_websites;
    }
    
    /***************************************************************************
    ***  brief                                                               ***
    ***  serial number ????                                                  ***
    ***  parameter out <none>                                                ***
    ***  parameter in  <none>                                                ***
    ***  return <none>                                                       ***
    *** @param                                                               ***
    ***************************************************************************/
    public void end(){
        this.abortTimeOut = true;
        if( process != null ){
            process.destroyForcibly();
            process.destroy();
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
    @Override
    public void run() {
        try {
            
            ProcessBuilder builder = null;
            
            if( !Console.isAdmin ){
                InputStream is = null;
                OutputStream os = null;
                try {
                    is = new FileInputStream(new File("confs/" + this.server));
                    os = new FileOutputStream(new File("dat/tmp"));
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = is.read(buffer)) > 0) {
                        os.write(buffer, 0, length);
                    }
                    
                    if( this.redirectAllTraffic ){
                        os.write( ("redirect-gateway def1 bypass-dhcp\n").getBytes() );
                    }else{
                        for( Website website : this.final_websites ){
                            if( website.wasRerouted ){
                                for( IPRange range : website.ranges ){
                                    os.write( ("route " + range.getIP() + " " + range.getMask() + "\n").getBytes() );
                                }
                            }
                        }
                    }
                    
                } finally {
                    is.close();
                    os.close();
                }
                builder = new ProcessBuilder("cmd.exe", "/c", "\"openvpn/openvpn.exe\" dat/tmp");
            } else {
                builder = new ProcessBuilder("cmd.exe", "/c", "\"openvpn/openvpn.exe\" confs/" + this.server );
            }
            
            //ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "dir" );
            
            process = builder.start();
            ArrayList<String> ip2route = new ArrayList<String>();
            ArrayList<String> sites2route = new ArrayList<String>();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line="";
            
             
             this.abortTimeOut = false;
             setTimeout(() -> this.connectionTimeout(), 25000);
             
             while ((line = reader.readLine()) != null) {
               
                 //CONNECTED TO THE SERVER
                 if( line.contains("Initialization Sequence Completed") && !this.connected   ){
                    this.connected = true;
                    Console.log("---Connection start---");
                    frame.connected( ip2route , sites2route );
                 //RESTARTED CONNECTION
                 }else if( line.contains("Connection reset, restarting") || line.contains("Restart pause,") 
                         || line.contains("[server] Inactivity timeout") ){
                     frame.reconnecting();
                     this.abortTimeOut = true;
                     this.connected = false;
                 //Static route ROUTE PARAMETER
                 } else if( line.contains("sdxess-site") && line.contains("Unrecognized option") ){
                    Pattern pattern = Pattern.compile("sdxess-site:[^\\s(]*");
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()){
                        String[] parts = matcher.group(0).split(":");
                        sites2route.add( (parts[1] + ":" + parts[2]).trim() );
                        Console.log("Website added for rerouting: " + parts[1]);
                    }else{
                        Console.log(line,true);
                    }
                 //Static route ROUTE PARAMETER
                 } else if( line.contains("route.exe ADD") ){
                    Pattern pattern = Pattern.compile("[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]");
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()){
                        ip2route.add(matcher.group(0));
                    }
                    
                    pattern = Pattern.compile("ADD\\s(.*)");
                    matcher = pattern.matcher(line);
                    if (matcher.find()){
                        StaticRoutes.addedRoutes.add(matcher.group(1));
                    }
                    
                    frame.updateMessage("Rerouted " + StaticRoutes.addedRoutes.size() + " IP's...");
                    Console.log(line,true);
                 
                 } else if ( line.contains("[server] Peer Connection Initiated") ){ 
                    frame.updateMessage("Peer Connection Initiated...");
                    this.abortTimeOut = true;
                    Console.log(line,true);
                 } else if ( line.contains("Successful ARP Flush on interface") ){ 
                    Pattern pattern = Pattern.compile("\\[(\\d+)\\]");
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()){
                        StaticRoutes.interf = Integer.parseInt(matcher.group(1));
                    }
                    Console.log(line,true);
                 } else if ( line.contains("Exiting due to fatal error") ){ 
                    frame.disconnect(true,null);
                    Console.log(line,true);
                 } else {
                    Console.log(line,true);
                 }
                 
                 
             }
             
             if( !this.connected && !this.abortTimeOut ){
                frame.notconnected("Connection Finished.");
                this.end();
             }
        } catch (IOException e) {
            e.printStackTrace();
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
    public void connectionTimeout(){
        if( !this.connected && !this.abortTimeOut ){
            frame.notconnected("Connection timed out");
            this.end();
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
    public static Thread setTimeout(Runnable runnable, int delay){
        Thread t = new Thread(() -> {
            try {
                Thread.sleep(delay);
                runnable.run();
            }
            catch (Exception e){
                System.err.println(e);
            }
        });
        t.start();
        return t;
    }
}