/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sdxess;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kotaro
 */
public class Website {
    public String IP = "";
    public String name = "";
    
    public boolean isStatic = false;
    
    public Website(String name){
        this.name = name;
   
        if( Website.isIP(name) ){
            this.IP = Website.getClassB( name );
            this.isStatic = true;
        }else{
            this.updateIp();
        }
    }
    
    public static boolean isIP(String text){
        return text.matches(  "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + 
                                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + 
                                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + 
                                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$" );
    }
        
    public void updateIp(){
        try {
            this.IP = Website.getClassB( StaticRoutes.NSLookup(name) );
        } catch (IOException ex) {
            Logger.getLogger(Website.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public Website(String name, String IP){
        this.IP = IP;
        this.name = name;
    }
    
    public boolean isReachable(){
        return IP.length() != 0;
    }
    
    public String getClassA(){
        String[] splitted_Ip = this.IP.split("\\.");
        return splitted_Ip[0] + ".0.0.0";
    }
    
     public static String getClassA(String IP){
        String[] splitted_Ip = IP.split("\\.");
        if( splitted_Ip.length != 4 ){
            return null;
        }
        return splitted_Ip[0] + ".0.0.0";
    }
    
    public String getClassB(){
        String[] splitted_Ip = this.IP.split("\\.");
        return splitted_Ip[0] + '.' + splitted_Ip[1] + ".0.0";
    }
    
     public static String getClassB(String IP){
        String[] splitted_Ip = IP.split("\\.");
        if( splitted_Ip.length != 4 ){
            return null;
        }
        return splitted_Ip[0] + '.' + splitted_Ip[1] + ".0.0";
    }
     
    public String getClassC(){
        String[] splitted_Ip = this.IP.split("\\.");
        return splitted_Ip[0] + '.' + splitted_Ip[1] + '.' + splitted_Ip[2] + ".0";
    }
    
     public static String getClassC(String IP){
        String[] splitted_Ip = IP.split("\\.");
        if( splitted_Ip.length != 4 ){
            return null;
        }
        return splitted_Ip[0] + '.' + splitted_Ip[1] + '.' + splitted_Ip[2] + ".0";
    }
}
