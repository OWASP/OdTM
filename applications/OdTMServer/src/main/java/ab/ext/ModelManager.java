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
// to init modeller with base model and given domain model - fillModeller(*,*)
//                                 or with base model only - fillModeller(*)             
public class ModelManager extends OManager{
   private static final Logger LOGGER = Logger.getLogger(LManager.class.getName());
    
   // base model   
   OWLOntology baseModel;
   // list of domain models
   ArrayList<OWLOntology> domainModels;
    
   // init it from a properties file
   // properties are like: 
   //    MODELS_FOLDER: /home/net/gitprojects/OdTM_data/models/
   //    BASEMODEL: OdTMBaseThreatModel.owl
   //    DOMAINMODELS: OdTMCCCTM.owl, SecurityPatternCatalogNaiveSchema.owl.
   public boolean init(PManager conf){
      domainModels = new ArrayList<OWLOntology>();
      
      String folder = conf.get("MODELS_FOLDER");
      if (folder == null){
         LOGGER.severe("could not get MODELS_FOLDER from properties");
         return false; 
      }

      String basemodelname = conf.get("BASEMODEL");
      if (basemodelname == null){
         LOGGER.severe("could not get BASEMODEL from properties");
         return false; 
      }

      String lst[] = conf.getAsArray("DOMAINMODELS");
      if (lst == null) {
         LOGGER.severe("could not get DOMAINMODELS from properties");
         return false; 
      }

      baseModel = loadFromFile(folder+basemodelname);
      if (baseModel == null) {
         LOGGER.severe("failed to init base model");
         return false; 
      }
      LOGGER.info ("got base model "+folder+basemodelname);

      for (int i=0;i<lst.length;i++){
         OWLOntology m = loadFromFile(folder+lst[i]);
         if (m != null){
             domainModels.add(m);
             LOGGER.info ("got domain model "+folder+lst[i]);
         } else {
             LOGGER.severe ("failed to process "+folder+lst[i]);
         }
      }
      
      if (domainModels.size()==0){
         LOGGER.severe ("found no domain models");
         return false;
      }
      
      return true;
   };
   
   // fill modeller with base and domain models
   // needs a modeller instance & iri of domain model
   public boolean fillModeller(ThreatModeller modeller,String iri){
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
   public boolean fillModeller(ThreatModeller modeller){
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
