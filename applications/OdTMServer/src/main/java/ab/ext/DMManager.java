package ab.ext;

import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.*;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.apibinding.*;
import org.semanticweb.owlapi.util.*;
import org.semanticweb.owlapi.formats.*;
import org.semanticweb.owlapi.reasoner.structural.*;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.model.parameters.*;
import org.semanticweb.owlapi.model.providers.*;
import java.io.*;
import java.util.*;
import java.util.stream.*;
import java.time.Duration;
import java.time.Instant;
import java.util.logging.*;
import ab.base.*;

// manages domain specific models
public class DMManager {
   private static final Logger LOGGER = Logger.getLogger(LManager.class.getName());
      
   ArrayList<DomainModel> models;
   
   public DMManager(){
      models = new ArrayList<DomainModel>();
   }
   
   public boolean init(PManager conf){
      LOGGER.info ("starting initing domain models...");
      
      String folder = conf.get("MODELS_FOLDER");
      if (folder == null){
         LOGGER.severe("Could not get the MODELS_FOLDER variable from the configuration");
         return false; 
      }

      String basemodel = conf.get("BASEMODEL");
      if (basemodel == null){
         LOGGER.severe("Could not get the BASEMODEL variable from the configuration");
         return false; 
      }

      String lst[] = conf.getAsArray("DOMAINMODELS");
      if (lst == null) {
         LOGGER.severe("Could not get the MODELS_FOLDER variable from the configuration");
         return false; 
      }

      for (int i=0;i<lst.length;i++){
         DomainModel m = new DomainModel();
         if (m.init(folder+basemodel,folder+lst[i])){
             models.add(m);
             LOGGER.info ("got "+lst[i]);
         } else {
             LOGGER.severe ("failed to process "+lst[i]);
         }
      }
      
      if (models.size()==0){
         LOGGER.severe ("found no domain model ");
         return false;
      }
      
      return true;
   };
   
}
