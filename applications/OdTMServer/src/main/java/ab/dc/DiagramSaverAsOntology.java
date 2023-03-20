package ab.dc;

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

public class DiagramSaverAsOntology extends OManager{
   private static final Logger LOGGER = Logger.getLogger(LManager.class.getName());
   
   // base model   
   OWLOntology baseModel;
   O bModel;
   
   
   // init everything from a properties file
   public boolean init(String basemodelname){

      if (basemodelname == null){
         LOGGER.severe("null base model");
         return false; 
      }
      baseModel = loadFromFile(basemodelname);
      if (baseModel == null) {
         LOGGER.severe("failed to init base model "+basemodelname);
         return false; 
      }
      bModel = O.create(baseModel);
      LOGGER.info ("got base model "+basemodelname);

      return true;
   };   

   public void addLabels(IRI iri, List<String> labels, O dModel){
      if (labels != null){
         for (int i=0;i<labels.size();i++){
            IRI labelIRI = IRI.create(dModel.getDefaultPrefix()+labels.get(i));
            dModel.addAxiom(dModel.getClassAssertionAxiom(labelIRI , iri));
         }
      }
   }

   public void addLabels2(IRI iri, IRI typeIRI, IRI modelIRI, List<String> labels, O dModel){
      dModel.addAxiom(dModel.getSubClass(modelIRI,typeIRI));
      dModel.addAxiom(dModel.getClassAssertionAxiom(modelIRI, iri));

      if (labels != null){
         for (int i=0;i<labels.size();i++){
            IRI labelIRI = IRI.create(dModel.getDefaultPrefix()+labels.get(i));
            dModel.addAxiom(dModel.getSubClass(labelIRI,typeIRI));
            dModel.addAxiom(dModel.getClassAssertionAxiom(labelIRI , iri));
         }
      }
   }


   public boolean saveToFile(String fileName, Diagram diagram){
      // create target model
      OWLOntology dstmModel = create(T.DModelIRI);
      addImportDeclaration(dstmModel,bModel.getIRI().toString());
      
      O dModel = O.create(dstmModel);
      
      for (int i=0;i<diagram.getProcesses().size();i++){
         DFDProcess ex = diagram.getProcesses().get(i);
         IRI iri = IRI.create(dModel.getDefaultPrefix()+ex.getName());
         IRI modelIRI = IRI.create(dModel.getDefaultPrefix()+ex.getModel());
         IRI typeIRI = IRI.create(T.Process);         
         addLabels2(iri,typeIRI, modelIRI,ex.getLabels(),dModel);
      } 
   
      for (int i=0;i<diagram.getStorages().size();i++){
         DFDStorage ex = diagram.getStorages().get(i);
         IRI iri = IRI.create(dModel.getDefaultPrefix()+ex.getName());
         IRI modelIRI = IRI.create(dModel.getDefaultPrefix()+ex.getModel());
         IRI typeIRI = IRI.create(T.Datastore);         
         addLabels2(iri,typeIRI, modelIRI,null,dModel);         
      } 

      for (int i=0;i<diagram.getExternals().size();i++){
         DFDExternalEntity ex = diagram.getExternals().get(i);
         IRI iri = IRI.create(dModel.getDefaultPrefix()+ex.getName());
         IRI modelIRI = IRI.create(dModel.getDefaultPrefix()+ex.getModel());
         IRI typeIRI = IRI.create(T.ExternalInteractor);         
         addLabels2(iri,typeIRI, modelIRI ,null,dModel);
      } 

      for (int i=0;i<diagram.getFlows().size();i++){
         DFDFlow ex = diagram.getFlows().get(i);
         IRI iri = IRI.create(dModel.getDefaultPrefix()+ex.getName());
         IRI modelIRI = IRI.create(dModel.getDefaultPrefix()+ex.getModel());
         IRI typeIRI = IRI.create(T.DataFlow);         
         addLabels2(iri,typeIRI, modelIRI,ex.getLabels(),dModel);
         
         DFDItem source = diagram.findItem(ex.getSource().getId());
         dModel.addAxiom(dModel.getObjectPropertyAssertionAxiom(IRI.create(T.HasSource), iri ,IRI.create(dModel.getDefaultPrefix()+source.getName())));

         DFDItem target = diagram.findItem(ex.getTarget().getId());         
         dModel.addAxiom(dModel.getObjectPropertyAssertionAxiom(IRI.create(T.HasTarget), iri ,IRI.create(dModel.getDefaultPrefix()+target.getName())));
         
         // !!! add the "relates" property between source and target
         dModel.addAxiom(dModel.getObjectPropertyAssertionAxiom(IRI.create(T.Relates), IRI.create(dModel.getDefaultPrefix()+source.getName()) ,IRI.create(dModel.getDefaultPrefix()+target.getName())));
         

      }
 
      String f1 = fileName+".owl";
      if (!saveToFile(dstmModel,f1)){
         LOGGER.severe("could not save " + f1);
         return false;
      }
      LOGGER.info ("wrote "+f1);

      dModel.fill();
      String f2 = fileName+".ttl";
      if (!saveToFileTTL(dstmModel,f2)){
         LOGGER.severe("could not save " + f2);
         return false;
      }
      LOGGER.info ("wrote "+f2);
      
      return true;
   }
   
}
