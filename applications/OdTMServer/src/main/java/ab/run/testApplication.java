
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

class testApplication {
   private final static Logger LOGGER = Logger.getLogger(LManager.class.getName());
	 
   public static String NAME = "testApplication";
   public static boolean DEBUG = true;
   public static String OWLEXT = ".owl";

   public static void main(String args[]){
 
     if (args.length != 0) {
	   LManager.init();

         LOGGER.info("starting ...");
         PManager conf = new PManager();

         if (conf.init(args[0])) {
            //
            //
            //String models[] = conf.getAsArray("DOMAINMODELS");
            //DomainModel dm = new DomainModel();
            DMManager dmm = new DMManager();
            dmm.init(conf);
            
            //dm.init(conf.get("MODELS_FOLDER")+conf.get("BASEMODEL"),conf.get("MODELS_FOLDER")+models[0]);
            
         } else { LOGGER.severe("could not find the configuration file!"); }

      } else { LOGGER.severe("give the configuration file!"); }

      LOGGER.info("...done");
   }

}
