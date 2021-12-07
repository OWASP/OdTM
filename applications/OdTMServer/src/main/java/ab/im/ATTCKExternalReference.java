package ab.im;

import java.io.*;
import java.util.logging.*;
import java.util.*;

import ab.base.*;

public class ATTCKExternalReference {
   
   private static final Logger LOGGER = Logger.getLogger(LManager.class.getName());

   private String url;
   private String external_id;
   private String source_name;

   public String getUrl(){
      return url;
   }

   public String getExternal_id(){
      return external_id;
   }

   public String getSource_name(){
      return source_name;
   }
 
   public String asString(){
      return "   id: ["+external_id+"] source: ["+source_name+"] url: ["+url+"]";
   }
   
}
