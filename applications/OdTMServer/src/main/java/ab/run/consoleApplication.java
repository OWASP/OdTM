
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

            // class model IRI
            String classModel = conf.get("CLASSMODELIRI");
            // domain model IRI
            String domainModel = conf.get("DOMAINMODELIRI");

            // console application can be run with the Threat Dragon model   
            String tdFile = conf.get("TDFILE");
            String tdFileOut = conf.get("TDOUT");
            
            if ( (tdFile != null) && (tdFileOut != null) ){
               // process the Threat Dragon model
               ThreatDragonManager parser = new ThreatDragonManager();
               if (parser.init(tdFile,manager)){
                  parser.process(domainModel,classModel,tdFileOut);
               } else{
                  LOGGER.severe("could not init TD model"+ tdFile);
               }
               
            }else{
               
               // trying to run the console application against a particular case
               String caseName = conf.get("CASE");

               if (caseName !=null){                                    ///
                  ThreatModeller modeller = manager.createModeller(domainModel);
               
                  if (modeller !=null){
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
