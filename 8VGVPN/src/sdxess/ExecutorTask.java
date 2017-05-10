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
    private boolean abort = false;
    private String server = "";
    
    public ExecutorTask(vpnConnect frame, String server) {
        this.frame = frame;
        this.server = server;
    }
    
    public void end(){
        this.abort = true;
        if( process != null ){
            process.destroyForcibly();
            process.destroy();
        }
    }
       
    @Override
    public void run() {
        try {
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "openvpn " + this.server + ".ovpn" );
            process = builder.start();
            ArrayList<String> sites2route = new ArrayList<String>();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line="";
             
             setTimeout(() -> this.connectionTimeout(), 25000);
             
             while ((line = reader.readLine()) != null) {
               
                 //CONNECTED TO THE SERVER
                 if( line.contains("Initialization Sequence Completed") && !this.connected   ){
                    this.connected = true;
                    System.out.println("---------------------------------Connection start---------------------------------");
                    frame.connected( sites2route );
                 //RESTARTED CONNECTION
                 }else if( line.contains("Connection reset, restarting") ){
                     frame.reconnecting(this.connected);
                     this.connected = false;
                     this.abort = true;
                     
                 //SDXess ROUTE PARAMETER
                 } else if( line.contains("sdxess-route") && line.contains("Unrecognized option") ){
                     
                    Pattern pattern = Pattern.compile("sdxess-route:[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()){
                        line = matcher.group(0).split(":")[1];
                        sites2route.add(line);
                        System.out.println("Site added for rerouting: " + line);
                    }else{
                        System.out.println(line);
                    }
                    
                 //Static route ROUTE PARAMETER
                 } else if( line.contains("route.exe ADD") && line.contains("255.255.0.0") ){
                    Pattern pattern = Pattern.compile("[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]");
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()){
                        sites2route.add(matcher.group(0));
                    }
                     System.out.println(line);
                 //DEFAULT
                 } else {
                     System.out.println(line);
                 }
             }
             
             if( !this.connected && !this.abort ){
                frame.notconnected("Connection Finished.");
                this.end();
             }
        } catch (IOException e) {
            e.printStackTrace();
        } 
    }
    
    public void connectionTimeout(){
        if( !this.connected && !this.abort ){
            frame.notconnected("Connection timed out");
            this.end();
        }
    }
    
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