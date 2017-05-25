/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sdxess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.*;

/**
 *
 * @author kotaro
 */
public class Website {
    
    public static String ajaxGET(String url_string){
        BufferedReader rd = null;
        try {
            URL url = new URL(url_string);
            URLConnection conn = url.openConnection();
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rd != null) {
                try {
                    rd.close();
                } catch (IOException e) {
                }
            }
        }
        return null; 
    }
    
    public static String URLtoASN(String url){
        try {
            String IP = StaticRoutes.NSLookup( url );
            if( IP.compareTo("Unrecognized host") == 0 )
                return null;
            return Website.IPtoASN( IP );
        } catch (IOException ex) {
            Logger.getLogger(Website.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static String IPtoASN(String IP){
        String json = Website.ajaxGET("https://ipinfo.io/" + IP + "/json");
        JSONObject obj = new JSONObject(  json  );
        return obj.get("org").toString().split(" ")[0];
    }
    
    public static boolean isIP(String text){
        return text.matches(  "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + 
                                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + 
                                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + 
                                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$" );
    }
    
    private static Pattern pDomainNameOnly;
    private static final String DOMAIN_NAME_PATTERN = "^((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,6}$";

    static {
            pDomainNameOnly = Pattern.compile(DOMAIN_NAME_PATTERN);
    }

    public static boolean isValidDomainName(String domainName) {
        return pDomainNameOnly.matcher(domainName).find();
    }
    
    
    /******** Class Starts ***********/
    public ArrayList<IPRange> ranges = new ArrayList<IPRange>();
    public String IP = "";
    public String name = "";
    public String ASN = "";
    public boolean isValid = true;
    private boolean routed = false;
    
    public Website(String name) {
        try {
            this.name = name;
            System.out.println("Rerouting " + this.name + "...");
            this.IP = StaticRoutes.NSLookup(name);
            
            
            if( this.ASN == null ){
                isValid = false;
            }else{
                System.out.println("Getting info for: " + this.IP);
                this.ASN = Website.IPtoASN( this.IP );
                System.out.println(this.name + " belongs to: " + this.ASN);
                String HTML = Website.ajaxGET("http://www.radb.net/query/" + 
                    "?advanced_query=1%091%091%091%091%091%091%091%091%091%091&keywords="
                        + ASN + "&query=Query&-K=1&-T=1&-T+option=route&ip_lookup="+
                        "1&ip_option=-L&-i=1&-i+option=origin&-r=1");

                Pattern pattern = Pattern.compile("route:\\s*(([^\\<])*)");
                Matcher matcher = pattern.matcher(HTML);

                String[] parts;
                
                System.out.println("processing " + this.ASN + " IP's");
                int total = 0;
                while(matcher.find()){
                    total++;
                    parts = matcher.group(1).split("/");
                    boolean insert = true;
                    IPRange new_range = new IPRange(parts[0], Integer.parseInt(parts[1]));

                    for( IPRange range : this.ranges ){
                        if( range.contains(new_range) ){
                            insert = false;
                        }
                    }
                    if( insert )
                        this.ranges.add( new_range );
                }

                this.ranges.sort(null);
                this.reduceRanges();
                System.out.println("Reduced " + total + " to " + this.ranges.size() + " IP's");
            }
        } catch (IOException ex) {
            this.isValid = false;
            Logger.getLogger(Website.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String[][] toRangesArray(){
        String[][] result = new String[this.ranges.size()][3];
        for( int i = 0 ; i < this.ranges.size() ; i++ ){
            IPRange range = this.ranges.get(i);
            result[i][0] = range.getIP();
            result[i][1] = range.getMask();
            result[i][2] = range.toString(true);
        }
        return result;
    }

    private void reduceRanges(){
        if( this.routed )
            return;
        
        ArrayList<IPRange> ranges2remove = new ArrayList<IPRange>();
        for( IPRange range : this.ranges ){
            for( IPRange range2 : this.ranges ){
                if( range != range2 ){
                    if( range.contains(range2) ){
                        ranges2remove.add(range2);
                    }
                }
            }
        }
        this.ranges.removeAll(ranges2remove);
    }
    
    public void route(){
        if( !this.routed ){
            this.routed = true;
            this.ranges.forEach((range) -> {
                StaticRoutes.AddStaticRoute( range.getIP() , range.getMask() );
            });
            StaticRoutes.flushDNS();
        }
    }
    
    public void deleteRouting(){
        if( this.routed ){
            this.routed = false;
            this.ranges.forEach((range) -> {
                StaticRoutes.deleteStaticRoute(range.getIP() , range.getMask() , 
                        StaticRoutes.sdxessGateway );
            });
            StaticRoutes.flushDNS();
        }
    }
    
    @Override
    public String toString(){
        return this.name + " (" + this.IP + ")";
    }

}
