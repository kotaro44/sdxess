/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sdxess;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author kotaro
 */
public class Console {
    public static boolean isAdmin = false;
    public static vpnConnect vpnconnect = null;
    
    /***************************************************************************
    ***  brief main function that all project can use to print messages on  ***
    ***        the console                                                   ***
    ***  serial number ????                                                  ***
    ***  parameter out <none>                                                ***
    ***  parameter in  <none>                                                ***
    ***  return <none>                                                       ***
    *** @param                                                               ***
    ***************************************************************************/
    public static void log(String message){
        Console._log(message,false);
    }
    
    public static void log(String message, boolean noTimeStamp){
        Console._log(message,noTimeStamp);
    }
    
    private static void _log(String message, boolean noTimeStamp){
        if( message == null )
            message = "null";
        if( message.length() > 0){
            String final_message = message;
            if( !noTimeStamp ){
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss yyyy");
                final_message = dtf.format(LocalDateTime.now()) + " " + message;
            }
            System.out.println(final_message);

            try{
                Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("log.txt", true), "UTF-8"));
                writer.write(final_message+"\n");
                writer.close();
            } catch (IOException e) {
               // ignore log out errors
            }
        }
    }
}
