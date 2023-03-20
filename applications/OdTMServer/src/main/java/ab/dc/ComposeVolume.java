package ab.dc;

import java.io.*;
import java.util.logging.*;
import java.util.*;

import ab.base.*;


public class ComposeVolume{

   private static final Logger LOGGER = Logger.getLogger(LManager.class.getName());
   public String name;
   public String hostPath;
   public String localPath;
   public String mode;
   // if mode == "ro"
   public boolean isReadOnly = false;
   public String str;
   
   public String type;
   public static final String GenericVolume = "GenericVolume";
   // a volume that is on host filesystem 
   public static final String HostFolder = "HostFolder";
   // a Docker that has a name
   public static final String DockerVolume = "DockerVolume";
   // a Docker volume that has no name
   public static final String DockerAnonymousVolume = "DockerAnonymousVolume";
   // ??? a Docker temporary volume
   public static final String DockerTmpVolume = "DockerTmpVolume";

   //        hostPath           localPath     mode
   // ./images/nginx/config:/etc/nginx/conf.d:ro
   //   name    localPath
   // db_data:/var/lib/mysql
   //   localPath
   // /var/lib/mysql
   public ComposeVolume(String volume){
      str = volume;
      String[] parts = volume.split(":");
      if (parts.length == 3) {
         mode = parts[2];
         if (mode.equals("ro")) isReadOnly = true;
      }
      if (parts.length>1){
         localPath = parts[1];
         if (parts[0].startsWith("/") || parts[0].startsWith(".") || parts[0].startsWith("~")) hostPath = parts[0];
         else name = parts[0];
      } else localPath = parts[0];
      
      type = GenericVolume;
      if (hostPath == null && name == null) type = DockerAnonymousVolume;
      if (hostPath != null) type = HostFolder;
      if (name != null) type = DockerVolume;
      
   }   

   public String toString(){
      return "localPath: "+localPath+" type: "+type +" name: "+name+" hostPath: "+hostPath+" isReadOnly: "+ isReadOnly;
   }

   public void debug(){
      LOGGER.info(toString());
   }

}
