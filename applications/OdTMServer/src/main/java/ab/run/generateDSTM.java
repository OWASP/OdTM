
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

class generateDSTM {
   private final static Logger LOGGER = Logger.getLogger(LManager.class.getName());

   public static void main(String args[]){
 
     if (args.length != 0) {
	     LManager.init();

        LOGGER.info("starting ...");
        PManager conf = new PManager();

        if (conf.init(args[0])) {
           DSTMGenerator generator = new DSTMGenerator();
           if (generator.init(conf)){
              if (generator.process()){
                 if (generator.save()) {
                    LOGGER.info("...done");
                 } else {LOGGER.severe ("failed to save target model");}
            
              } else { LOGGER.severe ("failed to process data"); }
           } else { LOGGER.severe ("failed to init generator"); }

        } else { LOGGER.severe("the configuration file was not found"); }
        
        
     } else { LOGGER.severe("give a configuration file!"); }
     
   }

}
