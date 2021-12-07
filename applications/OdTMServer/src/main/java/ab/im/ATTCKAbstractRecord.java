package ab.im;

import java.io.*;
import java.util.logging.*;
import java.util.*;

import ab.base.*;

public abstract class ATTCKAbstractRecord {
   
   private static final Logger LOGGER = Logger.getLogger(LManager.class.getName());

   protected String id;
   protected String type;
   protected String name;
   
   protected List<ATTCKExternalReference> externalReferences;

   protected String extID; // ID from external references
   protected String extUrl; // URL from external references
   
   public boolean setExtID(String sourceType){
      for (int i=0;i<externalReferences.size();i++) {
         ATTCKExternalReference ref = externalReferences.get(i);
         if (ref.getSource_name().equals(sourceType)) {
            extID = ref.getExternal_id();
            extUrl = ref.getUrl();
            return true;
         }
      }
      return false;
   }

   public void setExternalReferences(List<ATTCKExternalReference> externalReferences){
      this.externalReferences = externalReferences;
   }

   public List<ATTCKExternalReference> getExternalReferences(){
      return externalReferences;
   }


   public String getId(){
      return id;
   }

   public String getExtID(){
      return extID;
   }

   public String getExtUrl(){
      return extUrl;
   }
   
   public String getType(){
      return type;
   }
   
   public String getName(){
      return name;
   }

 

   
}
