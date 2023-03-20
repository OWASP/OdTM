package ab.dc;

import java.io.*;
import java.util.logging.*;
import java.util.*;
import java.util.UUID;

import ab.base.*;

public class DFDPoint {

   private static final Logger LOGGER = Logger.getLogger(LManager.class.getName());
   private String name;
   private String id;


   public DFDPoint(String name,String id){
      this.id = id;
      this.name = name;
   }

   public String getName(){
      return name;
   }

   public String getId(){
      return id;
   }


}
