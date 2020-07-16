package ab.base;

import java.util.Properties;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

// deals with configuration taken from a property file
public class PManager{

   private static final Logger LOGGER = Logger.getLogger(LManager.class.getName());

   private Properties props;

   public PManager(){
   }

   public boolean init (String fname){
     try {
        props = new Properties();
        if (fname !=null)props.load(new FileInputStream(fname));
        return true;
     } catch (Exception e){
       LOGGER.severe("could not find "+fname); 
       e.printStackTrace();
       return false;
     }
   }

   public String get(String name){
     return props.getProperty(name); 
   }

   // returns null if no property
   public String[] getAsArray(String name){
     String prop = props.getProperty(name);
     
     if (prop==null) return null;
           
	 String[] parts = prop.split(",");
	 
	 for (int i=0;i<parts.length;i++){
		 parts[i]=parts[i].trim();
     }
	 return parts; 
   }

}
