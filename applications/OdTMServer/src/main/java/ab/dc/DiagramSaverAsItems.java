
package ab.dc;

import java.io.*;
import java.util.Iterator;

import java.util.logging.*;
import java.util.*;

import ab.base.*;


public class DiagramSaverAsItems{

   private static final Logger LOGGER = Logger.getLogger(LManager.class.getName());


   public boolean saveToFile(String fileName, Diagram diagram){
      List<String> lines = new ArrayList<String>();
      lines.add("Processes:");      
      for (int i=0;i<diagram.getProcesses().size();i++) lines.add("  - "+diagram.getProcesses().get(i).asLine());
      lines.add("Storages:");      
      for (int i=0;i<diagram.getStorages().size();i++) lines.add("  - "+diagram.getStorages().get(i).asLine());
      lines.add("Externals:");      
      for (int i=0;i<diagram.getExternals().size();i++) lines.add("  - "+diagram.getExternals().get(i).asLine());

      lines.add("Flows:");      
      for (int i=0;i<diagram.getFlows().size();i++) {
         DFDFlow flow = diagram.getFlows().get(i);
         DFDItem source = diagram.findItem(flow.getSource().getId());
         DFDItem target = diagram.findItem(flow.getTarget().getId());
         String add = "";
         if (flow.getRealPortMapping() != null) add = " |"+flow.getRealPortMapping();
         if ((flow.asMappings()!=null) && !(flow.asMappings().equals(""))) add = " |"+flow.asMappings();
         lines.add("  - "+flow.asLine()+": "+source.getName()+" -> "+target.getName()+add);
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
