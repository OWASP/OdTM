package ab.im;

import java.io.*;
import java.util.logging.*;
import java.util.*;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.core.type.*;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import ab.base.*;

public class ATTCKTactic extends ATTCKAbstractRecord{
   
   private static final Logger LOGGER = Logger.getLogger(LManager.class.getName());

   // format description: https://github.com/mitre-attack/attack-stix-data/blob/master/USAGE.md
   // x_mitre_shortname : string

   private String x_mitre_shortname;

   public String getX_mitre_shortname(){
      return x_mitre_shortname;
   }
   
   public static ATTCKTactic getATTCKTacticFromNode(ObjectMapper mapper,JsonNode node){
     try {
        ATTCKTactic rec = mapper.treeToValue(node, ATTCKTactic.class);

        if (node.has("external_references")) {
            rec.setExternalReferences (mapper.readValue(node.path("external_references").toString(), new TypeReference<List<ATTCKExternalReference>>(){}) );             
            rec.setExtID("mitre-attack"); // find ID in external references
        }


        return rec;
     } catch (Exception e) {
        e.printStackTrace();
        LOGGER.severe("failed to read JSON node "+ node.toString());
        return null;
     }
   }


 
   public void debug(){
      System.out.println("--------------------------");
      System.out.println("extID: ["+extID+"]  extURL: ["+extUrl+"]");
      System.out.println("type: ["+type+"] id: ["+id+ "] name: ["+name+"]");
      System.out.println("short_name: ["+x_mitre_shortname+"]");

   }
   
}
