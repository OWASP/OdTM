
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


public class ComposeApplicationManager extends YamlParser{

   private static final Logger LOGGER = Logger.getLogger(LManager.class.getName());
   private ComposeApplication app;

   public boolean parse(String fileName, String appName){
      if (!init(fileName, true)) {
         LOGGER.severe("Could not open app" + fileName);
         return false; 
      };
      
      try{
         // new compose app
         app = new ComposeApplication(appName);
 
         // parse services
         JsonNode items = document.path("services");
         Iterator<Map.Entry<String, JsonNode>> itr = items.fields();
         while (itr.hasNext()) {
            Map.Entry<String, JsonNode> entry = itr.next();
            JsonNode serviceNode = entry.getValue();
            String serviceName = entry.getKey();
            ComposeService service = mapper.treeToValue(serviceNode, ComposeService.class);
            service.setName(serviceName);
            app.services.add(service);
         }

      } catch (Exception e) {
          e.printStackTrace();
          LOGGER.severe("failed :(((");
          return false;
      }
      return true;
   }
  
   public ComposeApplication getApp(){
      return app;
   }
 
}
