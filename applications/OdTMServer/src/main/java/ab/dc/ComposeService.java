package ab.dc;

import java.io.*;
import java.util.logging.*;
import java.util.*;

import ab.base.*;

public class ComposeService{

   private static final Logger LOGGER = Logger.getLogger(LManager.class.getName());
   private String name;
   private String image;
   private List<String> ports;
   private List<String> networks;
   private List<String> links;
   private List<String> volumes;
   private List<String> depends_on;
   private List<String> volumes_from;

   public String cImage;
   public List<ComposePort> cPorts;
   public List<ComposeVolume> cVolumes;

   public boolean prepare(){
      cImage = getShortImage();
      
      if (ports!= null){
         cPorts = new ArrayList<ComposePort>();
         for (int i=0;i<ports.size();i++){
            cPorts.add(new ComposePort(ports.get(i)));
         }
      }
      
      if (volumes!= null){
         cVolumes = new ArrayList<ComposeVolume>();
         for (int i=0;i<volumes.size();i++){
            cVolumes.add(new ComposeVolume(volumes.get(i)));
         }
      }
      
      
      return true;
   }

   public String getName(){
      return name;
   }

   public void setName(String name){
      this.name = name;
   }

   public String getImage(){
      return image;
   }

   public List<String> getPorts(){
      return ports;
   }

   public List<String> getDepends_on(){
      return depends_on;
   }

   public List<String> getVolumes_from(){
      return volumes_from;
   }


   public List<String> getVolumes(){
      return volumes;
   }

   public List<String> getLinks(){
      return links;
   }

   public List<String> getNetworks(){
      return networks;
   }


   public String list2string(List<String> lst){
      if (lst == null) return null;
      String strn = "";
      for (int i=0;i<lst.size();i++) {
         strn = strn + lst.get(i)+ " ";
      }
      return strn;

   }

   public void debug(){
      LOGGER.info("++++++++++++++++++++++++++++++++++++++++++++++");
      LOGGER.info("service name: " + name);
      LOGGER.info("image: " + image);
      LOGGER.info("ports: " + list2string(ports));
      LOGGER.info("networks: " + list2string(networks));
      LOGGER.info("links: " + list2string(links));            
      LOGGER.info("depends_on: " + list2string(depends_on));      
      LOGGER.info("volumes: " + list2string(volumes));
      LOGGER.info("volumes_from: " + list2string(volumes_from));
      
      
   }

   // nginx:1.19
   // bitnami/nginx
   // jwilder/nginx-proxy:alpine
   // registry.rocket.chat/rocketchat/rocket.chat:4.8.1
   public String getShortImage(){
      if (image == null) return null;
      String[] parts = image.split(":");
      String[] parts1 = parts[0].split("/");
      return parts1[parts1.length-1];
   }

   public String toString(){
      return "name: "+name+" ::: image: "+ image;
   }

}
