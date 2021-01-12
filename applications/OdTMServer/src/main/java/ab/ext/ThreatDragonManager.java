
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
   
   // parse Threat Dragon JSON to document
   public boolean init(String _dataFile, ModelManager _manager){
      if (!init(_dataFile,true)){
         return false;
      }
      modelManager = _manager;
      return true;
   }
   
   // processes diagrams from the document 
   // with given domain model and class model (as IRIs, can be null)
   // and saves target document to 'outputFile' 
   public boolean process(String domainModelIRI, String classModelIRI, String outputFile){
      try{
         // get all the diagrams
         JsonNode diagrams = document.path("detail").path("diagrams");
         Iterator<JsonNode> itr = diagrams.elements();
         // process a diagram
         while (itr.hasNext()) {
            JsonNode diagram = itr.next();
            LOGGER.info("got diagram " + diagram.path("title").textValue());
            // creates the ThreatModeller instance
            ThreatModeller modeller = modelManager.createModellerNew(domainModelIRI,classModelIRI);
            if (modeller !=null) {
               // creates work model
               if (modeller.createWorkModelFromJSON(diagram)){
                  // flush it
                  modeller.flushModel();
                  // apply reasoned axioms to the document
                  modeller.applyAxiomsToJSON(diagram);
                  // save the document to a JSON file
                  saveToFile(outputFile);
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
