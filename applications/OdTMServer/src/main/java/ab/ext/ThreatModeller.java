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
import org.semanticweb.owlapi.util.mansyntax.*;
import org.semanticweb.owlapi.expression.*;
import java.io.*;
import java.util.*;
import java.util.stream.*;
import java.time.Duration;
import java.time.Instant;
import java.util.logging.*;
import java.util.function.*;
import ab.base.*;

// here it is
public class ThreatModeller extends OManager {
   private static final Logger LOGGER = Logger.getLogger(LManager.class.getName());
   
   protected OWLOntology baseModel;    // the first model
   protected OWLOntology domainModel;  // the second one
   protected OWLOntology workModel;    // and abox (i.e. diagram) is here
   protected O bmodel;
   protected O dmodel;
   protected O model;                  // processor for workModel, init it with workModel
   
   protected String domainModelIRI;    // will be applied to abox as import
   
   protected static String DataFlowClass = "http://www.grsu.by/net/OdTMBaseThreatModel#DataFlow";
   protected static String TargetClass = "http://www.grsu.by/net/OdTMBaseThreatModel#Target";
   protected static String HasSourceProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#hasSource";
   protected static String HasTargetProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#hasTarget";
   protected static String IsSourceOfProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#isSourceOf";
   protected static String IsTargetOfProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#isTargetOf";
   protected static String suggestsProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#suggests";
   
   // init it with base model & domain model before use 
   //    you can skip base model if set first arg to null
   public boolean init (OWLOntology _baseModel, OWLOntology _domainModel){
      if (_baseModel !=null){
         baseModel = copyOntology(_baseModel);
         if (baseModel == null){
            LOGGER.severe ("unable to copy base model");      
            return false;
         }
         bmodel = O.create(baseModel);
         LOGGER.info("base model: "+ getBaseModelIRI());
      }
         
      domainModel = copyOntology(_domainModel);
      if (domainModel == null){
         LOGGER.severe ("unable to copy domain model");      
         return false;
      }
      dmodel = O.create(domainModel);
      domainModelIRI = getDomainModelIRI();
      LOGGER.info("domain model: "+ domainModelIRI);
      return true;
   } 
   
   

   public String getBaseModelIRI(){
      return getIRI(baseModel).toString();
   }
   
   public String getDomainModelIRI(){
      return getIRI(domainModel).toString();
   }
   
   
   public boolean createWorkModelFromFile(String filename){
      // get model configuration from file
      ArrayList<String> cc = readFileToArrayList(filename);
      if (cc == null){
         LOGGER.severe ("unable to get work model from " + filename);      
         return false;
      }

      // create empty model with the domain model import
      workModel = create("http://tmp.local");
      addImportDeclaration(workModel,getDomainModelIRI());
      model = O.create(workModel);
      if (model == null) {
         LOGGER.severe ("unable to create model");      
         return false;
      }

       // apply axioms from arraylist
       if (!model.applyAxiomsFromArrayList(cc)){
         LOGGER.severe ("unable to apply axioms");      
         return false;
       }
       
       LOGGER.info("got work model from "+ filename);             
       return true;
   }
   
    public void fillWorkModel(){
       model.fill();
    }
   
    public void flushModel(){
       model.flush();
    }

    private void says(String str){
       String name = "AIEd";       
       System.out.println(name+": "+str);
    }
    
    // to perform an operation with an object (i.e. EntitySearch related methods in O)  
    // we need to know where this object is (in base or domain model)
    protected O getModelByIRI(IRI iri){
       if (bmodel!=null) {
          if (bmodel.hasDefaultPrefix(iri)) return bmodel;
       }
       if (dmodel.hasDefaultPrefix(iri)) return dmodel;
       return null;
    }
    
    public void analyseWithAIEd(){
       // reason the model
       flushModel();
       says ("Hello, I love flow based security analysis."); 
       // get model prefix
       says ("Starting to analize " +model.getIRI() +" ...");
       // get list of flows
       Supplier<Stream<OWLNamedIndividual>> flows = () -> model.getReasonerInstances(IRI.create(DataFlowClass));
       says("...................................................");
       says("Given model contains "+flows.get().count()+" data flow(s):");
       model.showInstances(flows.get()); 
       for (Iterator<OWLNamedIndividual> iterator = flows.get().iterator(); iterator.hasNext(); ){
          OWLNamedIndividual flow = (OWLNamedIndividual)iterator.next();
          says("........................");
          says("From what I understand about the " + flow.getIRI().toString() + " flow:");
          says("its primary type is " + model.getPrimaryType(flow).getIRI().toString());
          says("its source is " + model.getObjectPropertyValue(flow.getIRI(),IRI.create(HasSourceProperty)));
          says("its target is " + model.getObjectPropertyValue(flow.getIRI(),IRI.create(HasTargetProperty)));
          //says("it's classified as (all the types):"); 
          //model.showClasses(model.getReasonerTypes(flow));
          //says("it's classified as (direct types):"); 
          //model.showClasses(model.getReasonerDirectTypes(flow));

       }

       // get list of targets
       Supplier<Stream<OWLNamedIndividual>> targets = () -> model.getReasonerInstances(IRI.create(TargetClass));
       says("...................................................");
       says("Given model contains "+targets.get().count()+" target(s):");
       model.showInstances(targets.get()); 
       for (Iterator<OWLNamedIndividual> iterator = targets.get().iterator(); iterator.hasNext(); ){
          OWLNamedIndividual target = (OWLNamedIndividual)iterator.next();
          says("........................");
          says("And what I think of the " + target.getIRI().toString() + " target:");
          says("its primary type is " + model.getPrimaryType(target).getIRI().toString());
          for (Iterator<OWLNamedIndividual> iterator2 = model.getReasonerObjectPropertyValues(target.getIRI(),IRI.create(IsSourceOfProperty)).iterator(); iterator2.hasNext(); ){
              OWLNamedIndividual tmp = (OWLNamedIndividual)iterator2.next();
              says("it's a source of " + tmp.toString());
          }
          
          for (Iterator<OWLNamedIndividual> iterator3 = model.getReasonerObjectPropertyValues(target.getIRI(),IRI.create(IsTargetOfProperty)).iterator(); iterator3.hasNext(); ){
              OWLNamedIndividual tmp = (OWLNamedIndividual)iterator3.next();
              says("it's a target of " + tmp.toString());
          }

          for (Iterator<OWLClass> iterator1 = model.getReasonerDirectTypes(target).iterator(); iterator1.hasNext(); ){
              OWLClass cls = (OWLClass)iterator1.next();
              O tmp = getModelByIRI(cls.getIRI());
              IRI x = tmp.searchForExpressionValue(tmp.getSearcherSuperClasses(cls.getIRI()),"ObjectSomeValuesFrom",IRI.create(suggestsProperty));
              if (x!=null) says("You can add an instance of "+ x.toString() +" (because " +cls.toString()+")");
          }
       }
       
       says("That's all.");
    }
   
    public void test(){
       //String name = "http://www.grsu.by/net/OdTMBaseThreatModel#ContainsHTTPServerComponent";
       String name = "http://www.grsu.by/net/OdTMBaseThreatModel#ContainsHTTPServerComponent";
       //flushModel();
       O bmodel = O.create(domainModel);
       //bmodel.showClassExpressions(bmodel.getSearcherEquivalentClasses(IRI.create(name)) );
       bmodel.showClassExpressions(bmodel.getSearcherSuperClasses(IRI.create(name)) );
       
       IRI x = bmodel.searchForExpressionValue(bmodel.getSearcherSuperClasses(IRI.create(name)),"ObjectSomeValuesFrom",IRI.create(suggestsProperty));
       System.out.println("xxxx " +x+ " zzzzz");
       //String str = "ObjectPropertyAssertion(<http://www.grsu.by/net/OdTMBaseThreatModel#agrees> :flow <http://www.grsu.by/net/OdTMBaseThreatModel#protocol_HTTP>)";
       //String str = "ObjectPropertyAssertion(<http://www.grsu.by/net/OdTMBaseThreatModel#isSourceOf> :user :flow)";
       // OWLAxiom ax = model.simpleStringToAxiom(str);

               
    }
    
    public boolean saveWorkModelToFile(String filename){
       LOGGER.info("trying to save work model as "+ filename);
       return saveToFile(workModel,filename);
    }
   
}
