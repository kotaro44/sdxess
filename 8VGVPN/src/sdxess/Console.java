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
import java.io.PrintWriter;
import java.io.Writer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import static sdxess.vpnConnect.file;

/**
 *
 * @author kotaro
 */
public class Console {
    public static void log(String message){
        Console._log(message,false);
    }
    
    public static void log(String message, boolean noTimeStamp){
        Console._log(message,noTimeStamp);
    }
    
    private static void _log(String message, boolean noTimeStamp){
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
