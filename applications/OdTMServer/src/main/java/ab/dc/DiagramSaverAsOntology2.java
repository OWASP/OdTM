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

public class DiagramSaverAsOntology2 extends OManager{
   private static final Logger LOGGER = Logger.getLogger(LManager.class.getName());
   
   // base model   
   OWLOntology baseModel;
   O bModel;

   // list of domain models
   ArrayList<OWLOntology> domainModels;
   
   String classModel;
   
   // init everything from a properties file
   public boolean init(String basemodelname, String lst[], String classes){

      if (basemodelname == null){
         LOGGER.severe("null base model");
         return false; 
      }
      
      if (lst == null){
         LOGGER.severe("null models list");
         return false; 
      }

      if (classes == null){
         LOGGER.severe("unknown class model");
         return false; 
      }

      
      baseModel = loadFromFile(basemodelname);
      if (baseModel == null) {
         LOGGER.severe("failed to init base model "+basemodelname);
         return false; 
      }
      bModel = O.create(baseModel);
      LOGGER.info ("got base model "+basemodelname);

      domainModels = new ArrayList<OWLOntology>();
      for (int i=0;i<lst.length;i++){
         OWLOntology m = loadFromFile(lst[i]);
         if (m != null){
            domainModels.add(m);
            LOGGER.info ("got domain model "+lst[i]);
         } else {
            LOGGER.severe ("failed to process "+lst[i]);
            return false;
         }
      }
      classModel = classes;


      return true;
   };   

   public void addLabels(IRI iri, IRI typeIRI, IRI modelIRI, List<String> labels, O dModel){
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

   public void addLabels2(IRI iri, List<String> labels2, O dModel){
      if (labels2 != null){
         for (int i=0;i<labels2.size();i++){
            IRI labelIRI = IRI.create(classModel+"#"+labels2.get(i));
            dModel.addAxiom(dModel.getClassAssertionAxiom(labelIRI , iri));
         }
      }
   }



   public boolean saveToFile(String fileName, Diagram diagram){
      // create target model
      OWLOntology dstmModel = create(T.DModelIRI);
      addImportDeclaration(dstmModel,bModel.getIRI().toString());
      for (int i=0;i<domainModels.size();i++){
          OWLOntology m = domainModels.get(i);
          addImportDeclaration(dstmModel,m.getOntologyID().getOntologyIRI().get().toString());
      }
      
      O dModel = O.create(dstmModel);
      
      for (int i=0;i<diagram.getProcesses().size();i++){
         DFDProcess ex = diagram.getProcesses().get(i);
         IRI iri = IRI.create(dModel.getDefaultPrefix()+ex.getName());
         IRI modelIRI = IRI.create(dModel.getDefaultPrefix()+ex.getModel());
         IRI typeIRI = IRI.create(T.Process);         
         addLabels(iri,typeIRI, modelIRI,ex.getLabels(),dModel);
         // addLabels2(iri,ex.getLabels2(),dModel);
         IRI labelIRI = IRI.create(classModel+"#"+"CloudApplication");
         dModel.addAxiom(dModel.getClassAssertionAxiom(labelIRI, iri));            

         IRI labelIRI2 = IRI.create(classModel+"#"+"Container");
         dModel.addAxiom(dModel.getClassAssertionAxiom(labelIRI2, iri));            
         
      } 
   
      for (int i=0;i<diagram.getStorages().size();i++){
         DFDStorage ex = diagram.getStorages().get(i);
         IRI iri = IRI.create(dModel.getDefaultPrefix()+ex.getName());
         IRI modelIRI = IRI.create(dModel.getDefaultPrefix()+ex.getModel());
         IRI typeIRI = IRI.create(T.Datastore);         
         addLabels(iri,typeIRI, modelIRI,null,dModel);         

         if (ex.getModel().equals("HostStorage")){
             IRI labelIRI2 = IRI.create(classModel+"#"+"HostStorage");
             dModel.addAxiom(dModel.getClassAssertionAxiom(labelIRI2, iri));            
         }

         if (ex.getModel().equals("DockerVolume")){
             IRI labelIRI2 = IRI.create(classModel+"#"+"ContainerVolume");
             dModel.addAxiom(dModel.getClassAssertionAxiom(labelIRI2, iri));            
         }

         if (ex.getModel().equals("DockerSocket")){
             IRI labelIRI2 = IRI.create(classModel+"#"+"ContainerSocket");
             dModel.addAxiom(dModel.getClassAssertionAxiom(labelIRI2, iri));            
         }


      } 

      for (int i=0;i<diagram.getExternals().size();i++){
         DFDExternalEntity ex = diagram.getExternals().get(i);
         IRI iri = IRI.create(dModel.getDefaultPrefix()+ex.getName());
         IRI modelIRI = IRI.create(dModel.getDefaultPrefix()+ex.getModel());
         IRI typeIRI = IRI.create(T.ExternalInteractor);
         if (ex.getModel().equals("RemoteUser")){
            IRI labelIRI = IRI.create(classModel+"#"+"RemoteUser");
            dModel.addAxiom(dModel.getClassAssertionAxiom(labelIRI, iri));            
         }         
         addLabels(iri,typeIRI, modelIRI ,null,dModel);
      } 

      for (int i=0;i<diagram.getFlows().size();i++){
         DFDFlow ex = diagram.getFlows().get(i);
         IRI iri = IRI.create(dModel.getDefaultPrefix()+ex.getName());
         IRI modelIRI = IRI.create(dModel.getDefaultPrefix()+ex.getModel());
         IRI typeIRI = IRI.create(T.DataFlow);         
         addLabels(iri,typeIRI, modelIRI,ex.getLabels(),dModel);
         
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
