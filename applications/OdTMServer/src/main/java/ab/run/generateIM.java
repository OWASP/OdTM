
package ab.run;

import ab.base.*;
import ab.im.*;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.*;
import org.semanticweb.owlapi.apibinding.*;
import org.semanticweb.owlapi.util.*;
import org.semanticweb.owlapi.formats.*;
import java.io.*;
import java.util.logging.*;

class generateIM {
   private final static Logger LOGGER = Logger.getLogger(LManager.class.getName());

   public static void main(String args[]){
 
     if (args.length != 0) {
	     LManager.init();

        LOGGER.info("starting ...");
        PManager conf = new PManager();

        if (conf.init(args[0])) {
          /////// initing the target ontology
          LOGGER.info("trying to init the integrated model");
          IMOntologyGenerator ontologyGenerator = new IMOntologyGenerator();
          if (!ontologyGenerator.init(conf)){
             LOGGER.severe("failed to init the ontology generator");
             System.exit(1);             
          }
          
          
          /////// ATT&CK parsing 
          String srcATTCK = conf.get("SRC_ATTCK");
          
          if (srcATTCK == null) {
             LOGGER.severe("apply the SRC_ATTCK option to the properties");
             System.exit(1);
          }
          
          LOGGER.info("trying to parser "+srcATTCK);
          ATTCKParser parser = new ATTCKParser();
          
          if (!parser.init(srcATTCK,true)){
             LOGGER.severe("Could not init the ATTCK parser");
             System.exit(1);
          }     
          parser.process(ontologyGenerator);


          //////// CAPEC parsing
          String srcCAPEC = conf.get("SRC_CAPEC");
          
          if (srcCAPEC == null) {
             LOGGER.severe("apply the SRC_CAPEC option to the properties");
             System.exit(1);
          }
          
          LOGGER.info("trying to parser "+srcCAPEC);
          CAPECParser3 parser2 = new CAPECParser3();
          
          if (!parser2.init(srcCAPEC)){
             LOGGER.severe("Could not init the CAPEC parser");
             System.exit(1);
          }     
          parser2.process(ontologyGenerator);


          //////// CWE parsing
          String srcCWE = conf.get("SRC_CWE");
          
          if (srcCWE == null) {
             LOGGER.severe("apply the SRC_CWE option to the properties");
             System.exit(1);
          }
          
          LOGGER.info("trying to parser "+srcCWE);
          CWEParser parser3 = new CWEParser();
          
          if (!parser3.init(srcCWE)){
             LOGGER.severe("Could not init the CWE parser");
             System.exit(1);
          }     
          parser3.process(ontologyGenerator);


          //////// saving clear model
          LOGGER.info("trying to save the model");
          ontologyGenerator.save();
          
          LOGGER.info("trying to save the dataset");
          ontologyGenerator.saveTTL();

          /////// saving reasoned model
          LOGGER.info("trying to save the filled dataset");          
          ontologyGenerator.saveTTL1();
          
          LOGGER.info("it is ok. done");
            
         
        } else { LOGGER.severe("the configuration file was not found"); }
     } else { LOGGER.severe("give a configuration file!"); }
   }
}
