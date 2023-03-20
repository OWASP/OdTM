
package ab.dc;

import java.io.*;
import java.util.Iterator;

import java.util.logging.*;
import java.util.*;

import ab.base.*;


public class DiagramSaverAsFlows{

   private static final Logger LOGGER = Logger.getLogger(LManager.class.getName());


   public boolean saveToFile(String fileName, Diagram diagram){
      List<String> lines = new ArrayList<String>();      
      for (int i=0;i<diagram.getFlows().size();i++){
         DFDFlow flow = diagram.getFlows().get(i);
         DFDItem source = diagram.findItem(flow.getSource().getId());
         DFDItem target = diagram.findItem(flow.getTarget().getId());
         String line = flow.asLine()+ ": " +source.asLine()+" -> "+target.asLine()+ " : "+flow.getRealPortMapping()+"|"+flow.asMappings();
         lines.add(line);
      }
      
      try (
        FileWriter fw = new FileWriter(fileName)) {
        for (int i=0;i<lines.size();i++) fw.write(lines.get(i)+'\n');
        fw.close();
      } catch (Exception e) {
        e.printStackTrace();
        LOGGER.severe("failed to save file " +fileName);
        return false;
      }
      LOGGER.info("wrote "+fileName); 
      return true;
   }
 
}
