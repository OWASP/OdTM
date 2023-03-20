package ab.dc;

import java.io.*;
import java.util.logging.*;
import java.util.*;

import ab.base.*;


public class ComposePort{

   private static final Logger LOGGER = Logger.getLogger(LManager.class.getName());
   public String proto;
   public String localPort;
   public String pubPort;
   public String pubIP;
   public boolean isFound = false;
   public boolean isOpenSocket = false;

   // 514:514/udp
   // 127.0.0.1:9200:9200
   // 8080:9000
   public ComposePort(String port){
      String[] parts = port.split("/");
      if (parts.length == 2) proto = parts[1];
      String[] parts1 = parts[0].split(":");
      if (parts1.length == 3){
         pubIP = parts1[0];
         pubPort = parts1[1];
         localPort = parts1[2];
         if (!pubIP.equals("127.0.0.1")) isOpenSocket = true;
      }
      if (parts1.length == 2){
         pubPort = parts1[0];
         localPort = parts1[1];
         isOpenSocket = true;
      }
   }   

   public void debug(){
      LOGGER.info(toString());
   }

   public String toString(){
      return "localPort: "+localPort+" pubPort: "+pubPort+" pubIP: "+ pubIP + " proto: "+proto + " isOpenSocket: "+isOpenSocket;
   }

}
