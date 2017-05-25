/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/*Git test*/
//=======
package sdxess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExecutorTask implements Runnable{
    private vpnConnect frame = null;
    private Process process = null;
    private boolean connected = false;
    private boolean abortTimeOut = false;
    private String server = "";
    
    /***************************************************************************
    ***  brief                                                               ***
    ***  serial number ????                                                  ***
    ***  parameter out <none>                                                ***
    ***  parameter in  <none>                                                ***
    ***  return <none>                                                       ***
    *** @param                                                               ***
    ***************************************************************************/
    public ExecutorTask(vpnConnect frame, String server) {
        this.frame = frame;
        this.server = server;
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
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "\"openvpn/openvpn.exe\" confs/" + this.server );
            //ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "dir" );
            
            process = builder.start();
            ArrayList<String> ip2route = new ArrayList<String>();
            ArrayList<String> sites2route = new ArrayList<String>();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line="";
            
                       
            //if( this != null )
             //   return;
             
             this.abortTimeOut = false;
             setTimeout(() -> this.connectionTimeout(), 25000);
             
             while ((line = reader.readLine()) != null) {
               
                 //CONNECTED TO THE SERVER
                 if( line.contains("Initialization Sequence Completed") && !this.connected   ){
                    this.connected = true;
                    System.out.println("---------------------------------Connection start---------------------------------");
                    frame.connected( ip2route , sites2route );
                 //RESTARTED CONNECTION
                 }else if( line.contains("Connection reset, restarting") || line.contains("Restart pause,") ){
                     frame.reconnecting();
                     this.abortTimeOut = true;
                     this.connected = false;
                 //Static route ROUTE PARAMETER
                 } else if( line.contains("sdxess-website") && line.contains("Unrecognized option") ){
                    Pattern pattern = Pattern.compile("sdxess-website:[^(]*");
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()){
                        String[] parts = matcher.group(0).split(":");
                        sites2route.add(parts[1]);
                        System.out.println("Website added for rerouting: " + parts[1]);
                    }else{
                        System.out.println(line);
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
                 
                 } else if ( line.contains("[server] Peer Connection Initiated") ){ 
                    frame.updateMessage("Peer Connection Initiated...");
                    this.abortTimeOut = true;
                    System.out.println(line);
                 } else if ( line.contains("Exiting due to fatal error") ){ 
                    frame.disconnect(true);
                    System.out.println(line);
                 } else {
                    System.out.println(line);
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
    public static void setTimeout(Runnable runnable, int delay){
        new Thread(() -> {
            try {
                Thread.sleep(delay);
                runnable.run();
            }
            catch (Exception e){
                System.err.println(e);
            }
        }).start();
    }
}