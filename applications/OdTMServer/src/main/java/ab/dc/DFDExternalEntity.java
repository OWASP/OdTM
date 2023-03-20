package ab.dc;

import java.io.*;
import java.util.logging.*;
import java.util.*;
import java.util.UUID;

import ab.base.*;

public class DFDExternalEntity implements DFDItem{

   private static final Logger LOGGER = Logger.getLogger(LManager.class.getName());
   private String name;
   private String model;
   private UUID id;

   public DFDExternalEntity(String name, String model){
      id = UUID.randomUUID();
      this.name = name;
      this.model = model;
      
   }

   public String asLine(){
      return name+"("+model+")";
   }

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

}
