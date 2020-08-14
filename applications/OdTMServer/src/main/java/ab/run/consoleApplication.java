
package ab.run;

import ab.base.*;
import ab.ext.*;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.*;
import org.semanticweb.owlapi.apibinding.*;
import org.semanticweb.owlapi.util.*;
import org.semanticweb.owlapi.formats.*;
import java.io.*;
import java.util.logging.*;

class consoleApplication {
   private final static Logger LOGGER = Logger.getLogger(LManager.class.getName());

   public static void main(String args[]){
 
     if (args.length != 0) {
	     LManager.init();

        LOGGER.info("starting ...");
        PManager conf = new PManager();

       if (conf.init(args[0])) {
          ModelManager manager = new ModelManager();
          if (manager.init(conf)){
            
            // console application can be run with the Threat Dragon model   
            String tdFile = conf.get("TDFILE");
            String tdFileImport = conf.get("TDIRI");
            String tdFileOut = conf.get("TDOUT");
            
            if ( (tdFile != null) && (tdFileOut != null) ){
               // process the Threat Dragon model
               ThreatDragonManager parser = new ThreatDragonManager();
               if (parser.init(tdFile,manager,tdFileImport,tdFileOut)){
                  parser.process();
               } else{
                  LOGGER.severe("could not init TD model"+ tdFile);
               }
               
            }else{
               
               // trying to run the console application against a particular case
               String caseName = conf.get("CASE");
               String caseImport = conf.get("CASEIRI");

               if (caseName !=null){                                    ///
                  ThreatModeller modeller = new ThreatModeller();
                  boolean res;
                  if (caseImport !=null){
                     // apply both base model & domain model
                     res = manager.fillModeller(modeller,caseImport);
                  }else{
                     // apply base model as domain model
                     res = manager.fillModeller(modeller);
                  }
               
                  if (res){
                     modeller.createWorkModelFromFile(caseName);                     
                     modeller.analyseWithAIEd();
                                    
                  } else{
                    LOGGER.severe("could not fill modeller! exiting...");
                  }
                                           
                }else {
                  // todo: init API server
                  LOGGER.severe("please give a case! exiting...");
                } ///
                  
           }
               
           } else {LOGGER.severe("could not init ModelManager! exiting...");};
                                    

         } else { LOGGER.severe("could not find the configuration file!"); }

      } else { LOGGER.severe("give the configuration file!"); }

      LOGGER.info("...done");
   }

}
