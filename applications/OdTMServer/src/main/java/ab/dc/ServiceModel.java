package ab.dc;

import java.io.*;
import java.util.logging.*;
import java.util.*;

import ab.base.*;

public class ServiceModel{

   private static final Logger LOGGER = Logger.getLogger(LManager.class.getName());
   private String name;
   // images
   private List<String> images;
   // labels, i.e. extra classes
   private List<String> labels;

   public static final String DockerSocket = "DockerSocket";
   public static final String GenericStorage = "GenericStorage";
   public static final String CertStorage = "CertStorage";
   public static final String ConfigStorage = "ConfigStorage";
   public static final String LogStorage = "LogStorage";
   public static final String DataStorage = "DataStorage";
   
   
   public String getName(){
      return name;
   }


   public void setName(String name){
      this.name = name;
   }


   public List<String> getImages(){
      return images;
   }

   public List<String> getLabels(){
      return labels;
   }


   public String list2string(List<String> lst){
      if (lst == null) return null;
      String strn = "";
      for (int i=0;i<lst.size();i++) {
         strn = strn + lst.get(i)+ " ";
      }
      return strn;

   }

}
