
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


public class DiagramSaverAsYaml extends YamlParser{

   private static final Logger LOGGER = Logger.getLogger(LManager.class.getName());

   public boolean saveToFile(String fileName, Diagram diagram){
      try{
         mapper.writeValue(new File(fileName), diagram); 
         LOGGER.info("wrote "+fileName);
      } catch (Exception e){
         e.printStackTrace();
         LOGGER.severe("failed to save file " +fileName);
         return false;
      }
      return true;
   }

 
}
