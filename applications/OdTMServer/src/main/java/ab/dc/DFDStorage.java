package ab.dc;

import java.io.*;
import java.util.logging.*;
import java.util.*;
import java.util.UUID;

import ab.base.*;

public class DFDStorage implements DFDItem{

   private boolean isClear = false;

   private static final Logger LOGGER = Logger.getLogger(LManager.class.getName());
   private String name;
   private String model;
   private String realName;
   private String realService;
   private UUID id;

   public DFDStorage(String name, String model){
      id = UUID.randomUUID();
      this.name = name;
      this.model = model;      
   }

   public void makeClear(){
      isClear = true;
   }

   public String asLine(){
      if (isClear) return name+"("+model+")";
      return name+"/"+realName+"("+model+")";
   }

/*   public String asFullLine(){
      if (isClear) return name+"("+model+")";
      return name+"/"+realName+"("+model+")";
   }*/

   public DFDPoint asDFDPoint(){
      return (new DFDPoint(name,id.toString()));
   }

   public String getName(){
      return name;
   }

   public String getId(){
      return id.toString();
   }

   public String getModel(){
      return model;
   }

   public String getRealName(){
      if (isClear) return null;
      return realName;
   }
   
   public String getRealService(){
      if (isClear) return null;
      return realService;
   }

   public void setRealName(String realName){
      this.realName = realName;
   }

   public void setRealService(String realService){
      this.realService = realService;
   }


}
