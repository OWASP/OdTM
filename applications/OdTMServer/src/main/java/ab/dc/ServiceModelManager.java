
package ab.dc;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.core.type.*;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.*;
import java.util.Iterator;

import java.util.logging.*;
import java.util.*;

import ab.base.*;


public class ServiceModelManager extends YamlParser{

   private static final Logger LOGGER = Logger.getLogger(LManager.class.getName());
   // a set of managed models
   private List<ServiceModel> models;
   private List<String> configs;
   private List<String> certs;
   private List<String> logs;
   private List<String> datas;
   private List<ServicePort> ports;

   public static final String GenericService = "GenericService";


   public ServiceModel getGenericModel(){
      ServiceModel model = new ServiceModel();
      model.setName(GenericService);
      return model;
   }

   private boolean parseX(String path){
      try{
         JsonNode items = document.path(path);
         Iterator<JsonNode> itr = items.elements();
         List<String> lst = new ArrayList<String>();
         while (itr.hasNext()) {
            JsonNode item = itr.next();
            String str = item.asText(); 
            lst.add(str);            
         }
         
         if (path.equals("configs")) configs = lst;
         if (path.equals("certs")) certs = lst;
         if (path.equals("logs")) logs = lst;
         if (path.equals("datas")) datas = lst;
         
         
      } catch (Exception e) {
          e.printStackTrace();
          LOGGER.severe("failed to parse "+path);
          return false;
      }
      
      
      return true;
   }
   

   private boolean parseY(){
      try{
         // takes objects
         JsonNode items = document.path("ports");
         Iterator<JsonNode> itr = items.elements();
         ports = new ArrayList<ServicePort>();
         while (itr.hasNext()) {
            JsonNode item = itr.next();
            ServicePort port = mapper.treeToValue(item, ServicePort.class);
            ports.add(port);            
         }
         
      } catch (Exception e) {
          e.printStackTrace();
          LOGGER.severe("failed to parse ports");
          return false;
      }

      return true;
   }
 
 
   
   // parse model from YAML file
   public boolean parse(String fileName){
      if (!init(fileName, true)) {
         LOGGER.severe("Could not init the service model");
         return false; 
      }
      
      try{
         // takes objects
         JsonNode items = document.path("services");
         Iterator<JsonNode> itr = items.elements();
         models = new ArrayList<ServiceModel>();
         while (itr.hasNext()) {
            JsonNode item = itr.next();
            ServiceModel model = mapper.treeToValue(item, ServiceModel.class);
            models.add(model);            
         }
         
      } catch (Exception e) {
          e.printStackTrace();
          LOGGER.severe("failed :(((");
          return false;
      }
      if (!parseX("configs")) return false;
      if (!parseX("certs")) return false;
      if (!parseX("logs")) return false;
      if (!parseX("datas")) return false;
      if (!parseY()) return false;

      return true;
   }
 
   // looking for a model with given name
   public ServiceModel findByName(String name){
      for (int i=0;i<models.size();i++){
         ServiceModel model = models.get(i); 
         if (model.getName().equals(name)) return model;
      }
      return null;
   }

   // looking a model with the keyword in its image
   // takes the first match
   public ServiceModel findByImage(String search){
      for (int i=0;i<models.size();i++){
         ServiceModel model = models.get(i);
         List<String> lst = model.getImages();
         if (lst!= null){
            for (int j=0;j<lst.size();j++){
               if (search.startsWith(lst.get(j))){
                   return model;
               }
            }
         }
         
      }
      return null;
   }

   public ServicePort findPort(String value){
      for (int i=0;i<ports.size();i++){
         ServicePort port = ports.get(i);
         if (port.getValue().equals(value)) return port;
      }     
      return null;
   }

   public String findVolumeFunction(String path){
      
      for (int i=(certs.size()-1);i>-1;i--){
         String str = certs.get(i);
         if (path.startsWith(str)) return ServiceModel.CertStorage; 
      }

      for (int i=(configs.size()-1);i>-1;i--){
         String str = configs.get(i);
         if (path.startsWith(str)) return ServiceModel.ConfigStorage; 
      }
      
      for (int i=(logs.size()-1);i>-1;i--){
         String str = logs.get(i);         
         if (path.startsWith(str)) return ServiceModel.LogStorage; 
      }

      for (int i=(datas.size()-1);i>-1;i--){
         String str = datas.get(i);       
         if (path.startsWith(str)) return ServiceModel.DataStorage; 
      }
      
      return null;
   }
 
   // checking if hostPath is the docker socket
   public boolean isVolumeDockerSocker(String path){
      if (path == null) return false;
      if (path.equals("/var/run/docker.sock")) return true;
      return false;
   }

   // checking if hostPath is sensitive
   public boolean isVolumeSensitivePath(String path){
      if (path == null) return false;
      if (path.startsWith("/var/run")) return true;
      if (path.startsWith("/proc")) return true; 
      if (path.equals("/")) return true;     
      return false;
   }
   
   public void debug(){
      for (int i=0;i<models.size();i++) LOGGER.info(models.get(i).toString());
   }
 
}
