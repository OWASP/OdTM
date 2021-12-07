package ab.im;

import java.io.*;
import java.util.logging.*;
import java.util.*;

import ab.base.*;

public class ATTCKDataSourceComponent {
   
   private static final Logger LOGGER = Logger.getLogger(LManager.class.getName());

   private String source;
   private String component;

   public String getSource(){
      return source;
   }

   public void setSource(String source){
      this.source = source;
   }

   public String getComponent(){
      return component;
   }

   public void setComponent(String component){
      this.component = component;
   }

   public static ATTCKDataSourceComponent getDataSourceComponent(String src){
     ATTCKDataSourceComponent item = new ATTCKDataSourceComponent();
     String[] parts = src.split(":");
     if (parts.length ==2){
       item.setSource(parts[0].trim().replace(" ",""));         
       item.setComponent(parts[1].trim().replace(" ",""));
       return item;
     }      
    
     return null;
   }

   
}
