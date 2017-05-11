
package sdxess;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
public class StaticRoutes {
    
    public static String DNS = null;
    public static ArrayList<String> addedRoutes = new ArrayList<>();
    
    
    public static void flushDNS(){    
        ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "ipconfig /flushdns");
        builder.redirectErrorStream(true);
        Process p = null;
        try {
            p = builder.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
    
            String line =  r.readLine();
            while (line != null) {
                System.out.println(line);
                line = r.readLine();
            }

        } catch (IOException ex) {
            Logger.getLogger(StaticRoutes.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
            
    
    public static String NSLookup(String domainname) throws IOException {
        try {
            InetAddress inetHost = InetAddress.getByName(domainname);
            String ip = inetHost.getHostAddress();
            return ip;
        } catch(UnknownHostException ex) {       
            return "Unrecognized host";
        }
    }
    
    public static void AddStaticRoute(String destination_ip, String subnetmask, String gatewayip) throws IOException{
        subnetmask = "255.255.0.0";
        String route = destination_ip+" MASK "+subnetmask+" "+gatewayip;
        ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "route ADD "+ route);
        StaticRoutes.addedRoutes.add(route);
        builder.redirectErrorStream(true);
        Process p = builder.start();
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = "";
        int flag=0;
        while (flag == 0) {
            line = r.readLine();
            if (line == null) {
                flag = 1;
                break; 
            }
            System.out.println(route);
            System.out.println(line);
        }
    }
    
    public static void AddStaticRoute(String destination_ip) throws IOException{
        String subnetmask = "255.255.0.0"; 
        String gatewayip = StaticRoutes.GetTAPInfo(6);
        
        String route = destination_ip+" MASK "+subnetmask+" "+gatewayip;
        ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "route ADD "+ route);
        StaticRoutes.addedRoutes.add(route);
        builder.redirectErrorStream(true);
        Process p = builder.start();
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = "";
        int flag=0;
        while (flag == 0) {
            line = r.readLine();
            if (line == null) {
                flag = 1;
                break; 
            }
            System.out.println(route);
            System.out.println(line);
        }
    }
    
    public static void deleteStaticRoute(String destination_ip ) throws IOException{
        String[] splitted_Ip = destination_ip.split("\\.");
        if( splitted_Ip.length != 4 ){
            System.out.println( destination_ip + " not a valid IP");
            return;
        }
        destination_ip = splitted_Ip[0] + '.' + splitted_Ip[1] + ".0.0";

        String route = "route DELETE "+destination_ip;
        ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", route);
        builder.redirectErrorStream(true);
        Process p = builder.start();
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = "";
        int flag=0;
        while (flag == 0) {
            line = r.readLine();
            if (line == null) {
                flag = 1;
                break; 
            }
            System.out.println(line);
        }
    }
    
    public static void flushAddedRoutes(){
        for( int i = 0 ; i < StaticRoutes.addedRoutes.size() ; i++ ){
            try {
                StaticRoutes.deleteStaticRouteCMD(StaticRoutes.addedRoutes.get(i));
            } catch (IOException ex) {
                Logger.getLogger(StaticRoutes.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        StaticRoutes.addedRoutes = new ArrayList<>();
    }
    
    public static void deleteStaticRouteCMD(String route ) throws IOException{
        ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "route DELETE "+ route);
        builder.redirectErrorStream(true);
        Process p = builder.start();
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = "";
        int flag=0;
        while (flag == 0) {
            line = r.readLine();
            if (line == null) {
                flag = 1;
                break; 
            }
            System.out.println(line);
        }
    }
    
    public static String GetTAPInfo(int tapindex) throws IOException {
        ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "openvpn --show-net");
        builder.redirectErrorStream(true);
        Process p = builder.start();
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = "";
        int x =0,y=-1;
        String[] tapinfo = new String[10];
        while (x == 0) {
            line = r.readLine();
            if (line == null) {
                x = 1; 
                //break;
            }else{
                if(line.contains("TAP-Windows Adapter")){
                    y=0;
                }
                if(y!=-1 && y<=9){
                    tapinfo[y] = line.toString();
                    //System.out.println("tapinfo "+y+" "+tapinfo[y]);
                    y++;
                }
            }
        }
        String gettapconfig = "";
        String replacetext = "";
        String startvalue;
        int flag = 0;
        int index = 0;
        switch (tapindex) {
            case 1: case 2: case 4: case 7: case 8: //1 index, 2 guid, 4 mac, 7 dhcp lease obtained, 8,dhcp lease expires
                if(tapindex==1){replacetext="Index = ";}
                if(tapindex==2){replacetext="GUID = ";}
                if(tapindex==4){replacetext="MAC = ";}
                if(tapindex==7){replacetext="DHCP LEASE OBTAINED = ";}
                if(tapindex==8){replacetext="DHCP LEASE EXPIRES  = ";}
                gettapconfig = (tapinfo[tapindex].replace(replacetext,"")).trim();
            break;
            case 3: case 5: case 6: //3 ip, 5 gateway ip, 6 dhcp server ip  
                if(tapindex==3){replacetext="IP = ";}
                if(tapindex==5){replacetext="GATEWAY = ";}
                if(tapindex==6){replacetext="DHCP SERV = ";}
                String ip = (tapinfo[tapindex].replace(replacetext,"")).trim();
                for(x=0; x<ip.length(); x++){
                    if(Character.isDigit(ip.charAt(x))){
                        gettapconfig = gettapconfig + ip.charAt(x);
                    }else{
                        if(ip.charAt(x)=='.'){
                            gettapconfig = gettapconfig + ip.charAt(x);
                        }    
                        if(ip.charAt(x)=='/'){
                            break;
                        }
                    }
                }
            break;
            case 9: case 10: case 11: //9 ip mask, 10 gateway ip mask, 11 dhcp server ip mask 
                if(tapindex==9){replacetext="IP = "; index=3;}
                if(tapindex==10){replacetext="GATEWAY = ";index=5;}
                if(tapindex==11){replacetext="DHCP SERV = ";index=6;}
                startvalue = (tapinfo[index].replace(replacetext,"")).trim();
                for(x=0; x<startvalue.length(); x++){
                    if(startvalue.charAt(x)=='/'){
                        for(y=(x+1); y<startvalue.length(); y++){ 
                            if(Character.isDigit(startvalue.charAt(y))){
                                gettapconfig = gettapconfig + startvalue.charAt(y);
                            }else{
                                if(startvalue.charAt(y)=='.'){
                                    gettapconfig = gettapconfig + startvalue.charAt(y);
                                }else{
                                    break;
                                }
                            }   
                        }   
                    }
                }
            break;
            case 12: //12 dns server1 ip,
               index=9;
               startvalue = (tapinfo[index].replace("DNS SERV = ","")).trim();
               flag = 0;
               for(x=0; x<startvalue.length(); x++){
                    if(Character.isDigit(startvalue.charAt(x))){
                        gettapconfig = gettapconfig + startvalue.charAt(x);
                    }else{
                        if(startvalue.charAt(x)=='.'){
                            gettapconfig = gettapconfig + startvalue.charAt(x);
                        }    
                        if(startvalue.charAt(x)=='/'){
                            break;
                        }
                    }
                }
                   if (flag==1){
                       break;
                   }
            break;
            case 13: // 13 dns server1 nm
                index=9;
                startvalue = (tapinfo[index].replace("DNS SERV = ","")).trim();
                flag = 0;
                for(x=0; x<startvalue.length(); x++){
                    if(startvalue.charAt(x)=='/'){
                        flag = 1;
                        for(y=(x+1); y<startvalue.length(); y++){ 
                            if(Character.isDigit(startvalue.charAt(y))){
                                gettapconfig = gettapconfig + startvalue.charAt(y);
                            }else{
                                if(startvalue.charAt(y)=='.'){
                                    gettapconfig = gettapconfig + startvalue.charAt(y);
                                }else{
                                    break;
                                }
                            }   
                        }   
                    }
                    if (flag==1){
                        break;
                    }
                }
            break;
            case 14://DNS Server 2 ip
                index = 9;
                startvalue = (tapinfo[index].replace("DNS SERV = ","")).trim();
                for(x=0; x<startvalue.length(); x++){
                    if(startvalue.charAt(x)==' '){
                        for(y=(x+1); y<startvalue.length(); y++){ 
                            if(Character.isDigit(startvalue.charAt(y))){
                                gettapconfig = gettapconfig + startvalue.charAt(y);
                            }else{
                                if(startvalue.charAt(y)=='.'){
                                    gettapconfig = gettapconfig + startvalue.charAt(y);
                                }    
                                if(startvalue.charAt(y)=='/'){
                                    break;
                                }
                            }      
                        }
                    }
                }
            break;
            case 15://DNS Server 2 nm
                index = 9;
                startvalue= (tapinfo[index].replace("DNS SERV = ","")).trim();
                for(x=0; x<startvalue.length(); x++){
                    if(startvalue.charAt(x)==' '){
                        for(y=(x+1); y<startvalue.length(); y++){ 
                            if(startvalue.charAt(y)=='/'){
                                for(int z=(y+1); z<startvalue.length(); z++){ 
                                    if(Character.isDigit(startvalue.charAt(z))){
                                        gettapconfig = gettapconfig + startvalue.charAt(z);
                                    }else{
                                        if(startvalue.charAt(z)=='.'){
                                            gettapconfig = gettapconfig + startvalue.charAt(z);
                                        }else{
                                            break;
                                        }
                                    }   
                                }   
                            }
                        }
                    }
                }
            break;
            default:
                gettapconfig = null;   
            break;
        }
        return gettapconfig;
    }

    public static void main(String[] args) throws IOException {
        StaticRoutes test = new StaticRoutes();
        /*System.out.println(test.GetTAPInfo(1));
        System.out.println(test.GetTAPInfo(2));
        System.out.println(test.GetTAPInfo(3));
        System.out.println(test.GetTAPInfo(4));
        System.out.println(test.GetTAPInfo(5));
        System.out.println(test.GetTAPInfo(6));
        System.out.println(test.GetTAPInfo(7));
        System.out.println(test.GetTAPInfo(8));
        System.out.println(test.GetTAPInfo(9));
        System.out.println(test.GetTAPInfo(10));
        System.out.println(test.GetTAPInfo(11));
        System.out.println(test.GetTAPInfo(12));
        System.out.println(test.GetTAPInfo(13));
        System.out.println(test.GetTAPInfo(14));
        System.out.println(test.GetTAPInfo(15));*/
        //System.out.println(test.NSLookup("google.com"));
        //test.AddStaticRoute(test.NSLookup("youtube.com"), test.GetTAPInfo(11), test.GetTAPInfo(6));
    }
}
