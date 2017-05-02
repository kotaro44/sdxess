/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg8vgvpn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

   public class ExecutorTask implements Runnable{

    private vpnConnect frame;
    private Process process = null;
    private boolean connected = false;
    private boolean abort = false;
    
    public ExecutorTask(vpnConnect frame){
        this.frame = frame;
    }
    
    public void end(){
        process.destroyForcibly();
        process.destroy();
    }
       
    @Override
    public void run() {
        try {
            process = Runtime.getRuntime().exec("cmd /c openvpn src/pkg8vgvpn/connection.ovpn");
            //process = Runtime.getRuntime().exec("cmd /c dir");
             BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
             String line="";
             
             setTimeout(() -> this.connectionTimeout(), 25000);
             
             while ((line = reader.readLine()) != null) {
                 System.out.println(line);
                 if( line.contains("Initialization Sequence Completed") && !this.connected   ){
                    this.connected = true;
                    frame.connected();
                 }
             }
             if( !this.connected && !this.abort ){
                frame.notconnected("Server not reachable");
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