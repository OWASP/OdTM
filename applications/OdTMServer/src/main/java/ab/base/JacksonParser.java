
package ab.base;

// https://github.com/FasterXML/jackson-docs/wiki/JacksonStreamingApi
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.*;

import java.time.Duration;
import java.time.Instant;
import java.util.logging.*;


public class JacksonParser{

   private static final Logger LOGGER = Logger.getLogger(LManager.class.getName());

   // own name
   protected String parserName;
   // source file
   protected String dataFile;
   // jackson factory
   protected ObjectMapper mapper;
   protected JsonFactory jFactory;
   protected JsonParser jParser;
   
   // JSON document as a tree
   protected JsonNode document; 

   public JacksonParser (){
      parserName = getClass().getName();
      mapper =  new ObjectMapper();
      jFactory = mapper.getJsonFactory();
      mapper.enable(SerializationFeature.INDENT_OUTPUT);
   }
 
   // init a new tree from scratch
   public boolean init(){
	   document = createEmptyNode();
	   return true;
   }

   // give absolute path to data file 
   // for the line-per-line parsing set initNode=false
   // to init the JsonNode object set initNode=true
   public boolean init(String _dataFile, boolean initNode){
 
      dataFile = _dataFile;
      try {
         jParser = jFactory.createJsonParser(new File(dataFile));
         if (initNode) {
            document = jParser.readValueAsTree();
         }
      } catch (Exception e) {
         e.printStackTrace();
         LOGGER.severe("failed to read JSON file "+dataFile);
         return false;
      }
      LOGGER.info("got "+dataFile);
      return true;
   }

////////////////////////////////////////////////////////////////////////
// get something
////////////////////////////////////////////////////////////////////////

   public String getParserName(){
      return parserName;
   }

   public ObjectMapper getMapper(){
	  return mapper;
   }

   public JsonNode getDocument(){
	   return document;
   }

////////////////////////////////////////////////////////////////////////
// nodes manipulation
////////////////////////////////////////////////////////////////////////

   public JsonNode createEmptyNode(){
	   return mapper.createObjectNode();
   }

   public boolean put(JsonNode node, String name, String value){
	   if ( (node !=null) && (name != null) && (value != null) ){
		   ((ObjectNode)node).put(name, value);
		   return true;
	   }
	   LOGGER.severe("failed to put"); 
	   return false;
   }


////////////////////////////////////////////////////////////////////////
// save something
////////////////////////////////////////////////////////////////////////

   // save the document instance to a given file
   public boolean saveToFile(String fileName){
      try{
         mapper.writeValue(new File(fileName), document); 
         LOGGER.info("wrote "+fileName);
      } catch (Exception e){
         e.printStackTrace();
         LOGGER.severe("failed to save file " +fileName);
         return false;
      }
      return true;
   }


}
