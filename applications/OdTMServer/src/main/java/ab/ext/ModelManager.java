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

// to load base model & domain models according properties file           
public class ModelManager extends OManager{
   private static final Logger LOGGER = Logger.getLogger(LManager.class.getName());
    
   // base model   
   OWLOntology baseModel;
   // list of domain models
   ArrayList<OWLOntology> domainModels;
    
   // init all the models according properties
   // takes the base model from file 
   //    (object: baseModel, prop: 'BASEMODEL: /path/to/it/OdTMBaseThreatModel.owl'
   // takes extra models from file:
   //    (ArrayList domainModels, prop: 'MODELS: /path/to/it/OdTMCCCTM.owl, /path/to/it/SecurityPatternCatalogNaiveSchema.owl'
   public boolean init(PManager conf){
      domainModels = new ArrayList<OWLOntology>();
      
      String basemodelname = conf.get("BASEMODEL");
      if (basemodelname == null){
         LOGGER.severe("could not get BASEMODEL from properties");
         return false; 
      }

      baseModel = loadFromFile(basemodelname);
      if (baseModel == null) {
         LOGGER.severe("failed to init base model");
         return false; 
      }
      LOGGER.info ("got base model "+basemodelname);

      String lst[] = conf.getAsArray("MODELS");
      if (lst != null) {
        // get extra models
        for (int i=0;i<lst.length;i++){
           OWLOntology m = loadFromFile(lst[i]);
           if (m != null){
              domainModels.add(m);
              LOGGER.info ("got domain model "+lst[i]);
           } else {
              LOGGER.severe ("failed to process "+lst[i]);
           }
        }
        if (domainModels.size()==0){
           LOGGER.severe ("found no domain models");
           return false;
        }
        
      } else {
         LOGGER.info("could not get MODELS from properties, uses the base model only");
      }

      return true;
   };
   
   public ThreatModeller createModellerNew(String domainModelIRI, String classModelIRI){
       ThreatModeller modeller = new ThreatModeller();
       if (modeller.init(baseModel,domainModels,domainModelIRI,classModelIRI)) {
          return modeller;
       } else{
          LOGGER.severe("could not init modeller");
          return null;
       }
   }   
   
   // creates the ThreatModeller object 
   //   -with base model and domain model only (imports=<IRI of a domain model>)
   //   -with base moedel as domain model (imports=null)
   public ThreatModeller createModeller(String imports){
       ThreatModeller modeller = new ThreatModeller();
       boolean res;
       
       if (imports !=null){
          // use the base model & a domain model
          res = fillModeller(modeller,imports);
       }else{
          // apply base model as domain model
          res = fillModeller(modeller);
       }

       if (!res) {
          LOGGER.severe("could not create modeller");
          return null;
       }
       return modeller;
   }
   
   // fill modeller with base and domain models
   // needs a modeller instance & iri of domain model
   protected boolean fillModeller(ThreatModeller modeller,String iri){
      
      OWLOntology tmp = getModel(iri);
      if (tmp == null){
         LOGGER.severe ("unable to get domain model");      
         return false;
      }
      
      if (!modeller.init(baseModel,tmp)){
         LOGGER.severe ("unable to get models");      
         return false;
      }
      return true;
   }
   
   // fill modeller with base model as domain model
   // needs a modeller instance
   protected boolean fillModeller(ThreatModeller modeller){
      if (!modeller.init(null,baseModel)){
         LOGGER.severe ("unable to get base model");      
         return false;
      }
      return true;
   }
   
   // get domain model by its iri
   public OWLOntology getModel(String iri){
      for (int i=0;i<domainModels.size();i++){
         OWLOntology tmp = domainModels.get(i);
         if (iri.equals(tmp.getOntologyID().getOntologyIRI().get().toString())) return tmp;
      }      
      LOGGER.severe ("no such model " + iri);      
      return null;
   }
   
   public void printModelIRIs(){
      for (int i=0;i<domainModels.size();i++){
         OWLOntology tmp = domainModels.get(i);
         System.out.println(tmp.getOntologyID().getOntologyIRI().get().toString());
      }
   }
   
}
