package ab.dc;

import java.io.*;
import java.util.logging.*;
import java.util.*;

import ab.base.*;

public class ComposeApplication{

   private static final Logger LOGGER = Logger.getLogger(LManager.class.getName());
   private String name;
   public List<ComposeService> services;

   public ComposeApplication(String name){
      services = new ArrayList<ComposeService>();
      this.name = name;
   }

   public String getName(){
      return name;
   }


   public List<ComposeService> getServices(){
      return services;
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
      LOGGER.info("====================================================");
      LOGGER.info(" application name: " + name);
      for (int i=0;i<services.size();i++) services.get(i).debug();
   }

}
