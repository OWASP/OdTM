package ab.dc;

import java.io.*;
import java.util.logging.*;
import java.util.*;
import java.util.UUID;

import ab.base.*;

public class DFDFlow implements DFDItem{

   private static final Logger LOGGER = Logger.getLogger(LManager.class.getName());

   private boolean isClear = false;

   private String name;
   private String model;
   private String realPortMapping;
   private UUID id;
   private String localPort;
   private DFDPoint source;
   private DFDPoint target;
   private List<String> labels;
   private List<String> realStorageMappings;

   public DFDFlow(DFDPoint source, DFDPoint target, String model,int index,List<String> lbs, String realPortMapping,String realStorageMapping){
      id = UUID.randomUUID();
      this.name = "flow"+index;
      this.model = model;
      this.source = source;
      this.target = target;
      if (lbs !=null){
         labels = new ArrayList<String>();
         for (int i=0;i<lbs.size();i++) labels.add(lbs.get(i));
      }
      this.realPortMapping = realPortMapping;
      if (realPortMapping !=null){
         String[] parts = realPortMapping.split(":");
         if (parts.length ==2) localPort = "port"+parts[1];
      }
      realStorageMappings = new ArrayList<String>();
      if (realStorageMapping !=null) realStorageMappings.add(realStorageMapping);
   }

   public void makeClear(){
      isClear = true;
   }

   public String asLine(){
      String add ="";
      if (labels !=null){
         for (int i=0;i<labels.size();i++) add = add +" "+labels.get(i);
      }
      String add1 = "";
      if ((!isClear) && (localPort != null)) add1="/"+localPort+"/";
      return name+"("+model+add+")"+add1;
   }

   public String asMappings(){
      if (isClear) return null;
      String add = null;
      if (realStorageMappings !=null){
         add = "";
         for (int i=0;i<realStorageMappings.size();i++) add = add +"("+i+")"+realStorageMappings.get(i);
      }
      return add;
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

   public String getLocalPort(){
      if (isClear) return null;
      return localPort;
   }

   public String getRealPortMapping(){
      if (isClear) return null;
      return realPortMapping;
   }

   public List<String> getRealStorageMappings(){
      if (isClear) return null;
      return realStorageMappings;
   }


   public List<String> getLabels(){
      return labels;
   }

   public DFDPoint getSource(){
      return source;
   }

   public DFDPoint getTarget(){
      return target;
   }

}
