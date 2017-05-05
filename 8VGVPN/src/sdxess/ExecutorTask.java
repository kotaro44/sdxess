/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sdxess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line="";
             
             setTimeout(() -> this.connectionTimeout(), 25000);
             
             while ((line = reader.readLine()) != null) {
                 System.out.println(line);
                 if( line.contains("Initialization Sequence Completed") && !this.connected   ){
                    this.connected = true;
                    frame.connected();
                 }
                 if( line.contains("Connection reset, restarting") ){
                     frame.reconnecting();
                     this.connected = false;
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