package ab.dc;

import java.io.*;
import java.util.logging.*;
import java.util.*;
import java.util.UUID;

import ab.base.*;

public class DFDProcess implements DFDItem{

   private boolean isClear = false;

   private static final Logger LOGGER = Logger.getLogger(LManager.class.getName());
   private String name;
   private String realName;
   private String model;
   private UUID id;
   private List<String> labels;

   public DFDProcess(String realName, String model,List<String> lbs,int index){
      id = UUID.randomUUID();
      this.realName = realName;
      this.name = "process"+index;
      this.model = model;
      if (lbs !=null){
         labels = new ArrayList<String>();
         for (int i=0;i<lbs.size();i++) labels.add(lbs.get(i));
      }
   }

   public void makeClear(){
      isClear = true;
   }

   public DFDPoint asDFDPoint(){
      return (new DFDPoint(name,id.toString()));
   }

/*   public String asLine(){
      if (isClear) return name+"("+model+")";
      return name+"/"+realName+"("+model+")";
   }*/

   public String asLine(){
      String add ="";
      if (labels !=null){
         for (int i=0;i<labels.size();i++) add = add +" "+labels.get(i);
      }      
      if (isClear) return name+"("+model+add+")";
      return name+"/"+realName+"("+model+add+")";
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

   public List<String> getLabels(){
      return labels;
   }

   public void addLabel(String label){
      if (labels == null) labels = new ArrayList<String>();
      if ( !(labels.contains(label)) ) labels.add(label);
   }

}
