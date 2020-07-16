
package ab.run;

import ab.base.*;
//import ab.ext.*;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.*;
import org.semanticweb.owlapi.apibinding.*;
import org.semanticweb.owlapi.util.*;
import org.semanticweb.owlapi.formats.*;
import java.io.*;
import java.util.logging.*;

class checkApplication {
   private final static Logger LOGGER = Logger.getLogger(LManager.class.getName());
	 
   public static void main(String args[]){
      //LManager.init();
      LManager.initSEVERE();
      
      LOGGER.info ("starting");
      
      if (args.length == 0) {
         LOGGER.severe("next time give at least a properties file");
         System.exit(1);
      }
      
      PManager conf = new PManager();
      if (conf.init(args[0])) {
         
         TManager tm = new TManager();
         
         String[] imports = conf.getAsArray("IMPORTS");
         if (imports != null){
            if (tm.initImports(imports)==false){
               LOGGER.severe("unable to get imports");
               System.exit(1);
            }
         }
         
         String modelName = conf.get("MODEL");
         if (modelName == null){
            LOGGER.severe("define the 'MODEL' variable in properties");
            System.exit(1);
         }
         if (tm.initModel(modelName)==false){
            LOGGER.severe("unable to get model "+modelName);
            System.exit(1);

         };
      
         String folder = conf.get("FOLDER");
         if (folder == null){
            LOGGER.severe("define the 'FOLDER' variable in properties");
            System.exit(1);
         }     
      
         // process only a folder
         if (tm.process(folder)==false) System.exit(1);
      
            
      } else { LOGGER.severe("could not find the configuration file!"); }
      
   
      
      LOGGER.info("...done");
   }

}
