
package ab.im;

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


public class ATTCKParser extends JacksonParser{

   private static final Logger LOGGER = Logger.getLogger(LManager.class.getName());
   
   public boolean process(IMOntologyGenerator generator){
      try{
         // takes objects
         JsonNode items = document.path("objects");
         Iterator<JsonNode> itr = items.elements();
         while (itr.hasNext()) {
            JsonNode item = itr.next();
            String itemType = item.path("type").textValue();
            // got an attack pattern
            if (itemType.equals("attack-pattern")){
               ATTCKAttackPattern rec = ATTCKAttackPattern.getATTCKAttackPatternFromNode(mapper,item);
               if (rec != null){
                  generator.processATTCKAttackPattern(rec);
               } else {
                  LOGGER.severe("failed to parse "+item.toString());
               }
            }

            // got a tactic
            if (itemType.equals("x-mitre-tactic")){
               ATTCKTactic rec = ATTCKTactic.getATTCKTacticFromNode(mapper,item);
               if (rec != null){
                  //rec.debug();
                  generator.processATTCKTactic(rec);
               } else {
                  LOGGER.severe("failed to parse "+item.toString());
               }
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
