
package ab.ext;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.*;
import java.util.Iterator;

import java.util.logging.*;

import ab.base.*;

public class ThreatDragonManager extends JacksonParser{

   private static final Logger LOGGER = Logger.getLogger(LManager.class.getName());
   protected ModelManager modelManager;
   protected String imports;
   protected String output;
   
   public boolean init(String _dataFile, ModelManager _manager, String _imports, String _output){
      if (!init(_dataFile,true)){
         return false;
      }
      modelManager = _manager;
      imports = _imports;
      output = _output;
      return true;
   }
   
   public boolean process(){
      try{
         // get diagrams
         JsonNode diagrams = document.path("detail").path("diagrams");
         Iterator<JsonNode> itr = diagrams.elements();
         // process a diagram
         while (itr.hasNext()) {
            JsonNode diagram = itr.next();
            LOGGER.info("got diagram " + diagram.path("title").textValue());
            ThreatModeller modeller = modelManager.createModeller(imports); 
            if (modeller !=null) {
               if (modeller.createWorkModelFromJSON(diagram)){
                  modeller.flushModel();
                  modeller.applyAxiomsToJSON(diagram);
                  saveToFile(output);
               } else {
                  LOGGER.severe("Could not create a work model for the diagram " +diagram.path("title").textValue());
               }
            } else {
               LOGGER.severe("Could not init modeller for the diagram " +diagram.path("title").textValue());
            }
            
         }
         
      } catch (Exception e) {
          e.printStackTrace();
          LOGGER.severe("failed :(((");
          return false;
      }
      return true;
   }
 
}
