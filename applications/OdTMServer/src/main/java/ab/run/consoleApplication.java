
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
               
            // console application is always run against a particular case
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
                  //modeller.flushModel();
                  
                  modeller.analyseWithAIEd();
                  
                  //modeller.fillWorkModel();
                  //modeller.saveWorkModelToFile("/home/net/tmp/test.owl");
                  //modeller.test();   
                  
                  //modeller.fillWorkModel();
                  //modeller.saveWorkModelToFile("/home/net/tmp/test1.owl");
                  //modeller.saveWorkModelToFile("/home/net/tmp/test2.owl");
                  
               } else{
                  LOGGER.severe("could not fill modeller! exiting...");
               }
                      
                     
             }else {LOGGER.severe("please give a case! exiting...");} ///
                  
          
               
           } else {LOGGER.severe("could not init ModelManager! exiting...");};
                                    

         } else { LOGGER.severe("could not find the configuration file!"); }

      } else { LOGGER.severe("give the configuration file!"); }

      LOGGER.info("...done");
   }

}
