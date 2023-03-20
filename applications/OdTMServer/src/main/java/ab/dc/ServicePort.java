package ab.dc;

import java.io.*;
import java.util.logging.*;
import java.util.*;

import ab.base.*;

public class ServicePort{

   private static final Logger LOGGER = Logger.getLogger(LManager.class.getName());
   private String name;
   private String value;
   private String label;

   public String getName(){
      return name;
   }

   public String getValue(){
      return value;
   }
   
   public String getLabel(){
      return label;
   }


}
