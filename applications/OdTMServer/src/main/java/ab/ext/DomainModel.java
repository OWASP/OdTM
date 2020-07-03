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

// OWL processor for a domain specific model
public class DomainModel extends OManager{
   private static final Logger LOGGER = Logger.getLogger(LManager.class.getName());

   protected OWLOntology bmodel;  // base model
   protected OWLOntology dmodel;  // domain specific model   
   protected OWLOntology amodel;  // DFD diagram
   protected OWLOntology model;   // merged model 


   // load base & domain models from files
   public boolean init(String baseModelPath, String domainModelPath){
      bmodel = loadFromFile(baseModelPath);
      if (bmodel == null){
          LOGGER.severe("Unable to read base model from "+baseModelPath);
          return false; 
      }
      LOGGER.info("got base model from "+baseModelPath);
      
      dmodel = loadFromFile(domainModelPath);
      if (dmodel == null){
          LOGGER.severe("Unable to read domain model from "+domainModelPath);
          return false; 
      }
      LOGGER.info("got domain model from "+domainModelPath);

      return true;    
   }
   
   
    
}
