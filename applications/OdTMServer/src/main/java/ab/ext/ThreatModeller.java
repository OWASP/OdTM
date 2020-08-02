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
   protected static String ClassifiedClass = "http://www.grsu.by/net/OdTMBaseThreatModel#Classified";
   protected static String ClassifiedIsEdgeClass = "http://www.grsu.by/net/OdTMBaseThreatModel#ClassifiedIsEdge";
   protected static String ClassifiedHasEdgeClass = "http://www.grsu.by/net/OdTMBaseThreatModel#ClassifiedHasEdge";
   protected static String HasSourceProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#hasSource";
   protected static String HasTargetProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#hasTarget";
   protected static String IsSourceOfProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#isSourceOf";
   protected static String IsTargetOfProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#isTargetOf";
   protected static String IsEdgeOfProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#isEdgeOf";   
   protected static String IsAffectedByProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#isAffectedBy";
   protected static String suggestsProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#suggests";
   protected static String suggestsThreatCategoryProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#suggestsThreatCategory";
   protected static String suggestsThreatProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#suggestsThreat"; 
   
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
       // ignoring owl:thing
       if (iri.toString().equals("http://www.w3.org/2002/07/owl#Thing")) return null;
       // failed
       LOGGER.severe("could not found model for "+ iri.toString());
       return null;
    }
    
    public Stream<OWLNamedIndividual> findReasonForTarget(List<OWLNamedIndividual> sourceFlows, List<OWLNamedIndividual> targetFlows, OWLNamedIndividual target, OWLClass cls){
       O tmp = getModelByIRI(cls.getIRI());
       MyAxiom ax = tmp.searchForSimpleClassDefinition(cls.getIRI());
       if (ax != null){ // ax.args[0] - property, ax.args[1] - class
          ArrayList<OWLNamedIndividual> lst = new ArrayList();
          // if isSourceOf
          if (ax.args[0].equals(IsSourceOfProperty)) {
             for (Iterator<OWLNamedIndividual> iterator = sourceFlows.stream().iterator(); iterator.hasNext(); ){
                OWLNamedIndividual flow = (OWLNamedIndividual)iterator.next();
                if (model.isReasonerIndividualBelongsToClass(flow.getIRI(),IRI.create(ax.args[1])) ) lst.add (flow);
             }
             return lst.stream();
          }
          
          // if isTargetOf
          if (ax.args[0].equals(IsTargetOfProperty)) {
             for (Iterator<OWLNamedIndividual> iterator = targetFlows.stream().iterator(); iterator.hasNext(); ){
                OWLNamedIndividual flow = (OWLNamedIndividual)iterator.next();
                if (model.isReasonerIndividualBelongsToClass(flow.getIRI(),IRI.create(ax.args[1])) ) lst.add (flow);
             }
             return lst.stream();           
          }
          
          // if isEdgeOf
          // a bit of copy-paste
          if (ax.args[0].equals(IsEdgeOfProperty)) {
             for (Iterator<OWLNamedIndividual> iterator = targetFlows.stream().iterator(); iterator.hasNext(); ){
                OWLNamedIndividual flow = (OWLNamedIndividual)iterator.next();
                if (model.isReasonerIndividualBelongsToClass(flow.getIRI(),IRI.create(ax.args[1])) ) lst.add (flow);
             }
             for (Iterator<OWLNamedIndividual> iterator = sourceFlows.stream().iterator(); iterator.hasNext(); ){
                OWLNamedIndividual flow = (OWLNamedIndividual)iterator.next();
                if (model.isReasonerIndividualBelongsToClass(flow.getIRI(),IRI.create(ax.args[1])) ) lst.add (flow);
             }
             return lst.stream();
                        
          }
          
       }
       return null;
    }
    
    public String instancesToString(Stream<OWLNamedIndividual> lst){
       if (lst != null){
          StringBuffer bf = new StringBuffer();
          for (Iterator<OWLNamedIndividual> iterator = lst.iterator(); iterator.hasNext(); ){
             OWLNamedIndividual flow = (OWLNamedIndividual)iterator.next();
             bf.append(flow.toString());
             bf.append(" ");
          }
          return bf.toString();
       }
       return null;
    }

    public String classesToString(Stream<OWLClass> lst){
       if (lst != null){
          StringBuffer bf = new StringBuffer();
          for (Iterator<OWLClass> iterator = lst.iterator(); iterator.hasNext(); ){
             OWLClass flow = (OWLClass)iterator.next();
             bf.append(flow.toString());
             bf.append(" ");
          }
          return bf.toString();
       }
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
       says("Let me consider flows of this model...");
       says("The model contains "+flows.get().count()+" data flow(s):");
       List<OWLClass> classifiedHasEdge = model.getReasonerSubclasses(IRI.create(ClassifiedHasEdgeClass)).collect(Collectors.toList());
       // get primary type, source & target, also suggestions
       for (Iterator<OWLNamedIndividual> iterator = flows.get().iterator(); iterator.hasNext(); ){
          OWLNamedIndividual flow = (OWLNamedIndividual)iterator.next();
          says("The " + flow.toString()+ " flow:");
          says("...Its primary type is " + model.getPrimaryType(flow).toString());
          says("...Its source is " + model.getObjectPropertyValue(flow.getIRI(),IRI.create(HasSourceProperty)));
          says("...Its target is " + model.getObjectPropertyValue(flow.getIRI(),IRI.create(HasTargetProperty)));

          List<OWLNamedIndividual> threats = model.getReasonerObjectPropertyValues(flow.getIRI(),IRI.create(IsAffectedByProperty)).collect(Collectors.toList());
          for (Iterator<OWLNamedIndividual> iterator5 = threats.stream().iterator(); iterator5.hasNext(); ){
              OWLNamedIndividual tmp = (OWLNamedIndividual)iterator5.next();
              says("...It is affected by the " + tmp.toString() + " threat");
          }

          for (Iterator<OWLClass> iterator1 = model.getReasonerTypes(flow).iterator(); iterator1.hasNext(); ){
              OWLClass cls = (OWLClass)iterator1.next();
              if (classifiedHasEdge.contains(cls)){ // check only subclasses of the 'ClassifiedHasEdge' class
                 O tmp = getModelByIRI(cls.getIRI());
                 if (tmp!=null){
                    // a recommendation for threats
                    IRI y = tmp.searchForExpressionValue(tmp.getSearcherSuperClasses(cls.getIRI()),"ObjectSomeValuesFrom",IRI.create(suggestsThreatProperty));
                    if (y!=null) {
                       String instances = instancesToString(model.getReasonerInstances(y)); 
                       says("...I suggest to apply threats of the <"+ y.toString()+ "> class: " +instances);
                    }
                 }
              }
          }
 
       }

       // get list of targets
       Supplier<Stream<OWLNamedIndividual>> targets = () -> model.getReasonerInstances(IRI.create(TargetClass));
       says("Let me consider targets...");
       says("Given model contains "+targets.get().count()+" target(s):");
       // model.showInstances(targets.get()); 
       for (Iterator<OWLNamedIndividual> iterator = targets.get().iterator(); iterator.hasNext(); ){
          OWLNamedIndividual target = (OWLNamedIndividual)iterator.next();
          says("The " + target.toString()+ " target:");
          // primary type
          says("...Its primary type is " + model.getPrimaryType(target).toString());

          // sourced flows 
          List<OWLNamedIndividual> sourceFlows = model.getReasonerObjectPropertyValues(target.getIRI(),IRI.create(IsSourceOfProperty)).collect(Collectors.toList());
          // sourced and targeted flows
          for (Iterator<OWLNamedIndividual> iterator2 = sourceFlows.stream().iterator(); iterator2.hasNext(); ){
              OWLNamedIndividual tmp = (OWLNamedIndividual)iterator2.next();
              says("...It's a source of flow " + tmp.toString());
          }
          
          // target flows
          List<OWLNamedIndividual> targetFlows = model.getReasonerObjectPropertyValues(target.getIRI(),IRI.create(IsTargetOfProperty)).collect(Collectors.toList());
          for (Iterator<OWLNamedIndividual> iterator3 = targetFlows.stream().iterator(); iterator3.hasNext(); ){
              OWLNamedIndividual tmp = (OWLNamedIndividual)iterator3.next();
              says("...It's a target of flow " + tmp.toString());
          }

          // affected threats
          List<OWLNamedIndividual> threats = model.getReasonerObjectPropertyValues(target.getIRI(),IRI.create(IsAffectedByProperty)).collect(Collectors.toList());
          for (Iterator<OWLNamedIndividual> iterator4 = threats.stream().iterator(); iterator4.hasNext(); ){
              OWLNamedIndividual tmp = (OWLNamedIndividual)iterator4.next();
              says("...It is affected by the " + tmp.toString() + " threat");
          }


          // list of the 'classified as an edge' classes
          List<OWLClass> classifiedIsEdge = model.getReasonerSubclasses(IRI.create(ClassifiedIsEdgeClass)).collect(Collectors.toList());
          // model.showClasses(model.getReasonerTypes(target));
          for (Iterator<OWLClass> iterator1 = model.getReasonerTypes(target).iterator(); iterator1.hasNext(); ){
              OWLClass cls = (OWLClass)iterator1.next();
              if (classifiedIsEdge.contains(cls)){ // check only subclasses of the 'ClassifiedIsEdge' class
                 O tmp = getModelByIRI(cls.getIRI());
                 if (tmp!=null){
                    String reasons = instancesToString(findReasonForTarget(sourceFlows,targetFlows,target,cls));
                    
                    // a recommendation for internal structure
                    IRI x = tmp.searchForExpressionValue(tmp.getSearcherSuperClasses(cls.getIRI()),"ObjectSomeValuesFrom",IRI.create(suggestsProperty));
                    if (x!=null) {
                       String instances = instancesToString(model.getReasonerInstances(x));
                       says("...I suggest to apply an internal component of the <"+ x.toString() + "> class (because of "+reasons+"): "+instances);
                    }

                    // a recommendation for threat categories
                    IRI y = tmp.searchForExpressionValue(tmp.getSearcherSuperClasses(cls.getIRI()),"ObjectSomeValuesFrom",IRI.create(suggestsThreatCategoryProperty));
                    if (y!=null) {
                       //String reasons = instancesToString(findReasonForTarget(sourceFlows,targetFlows,target,cls));
                       String instances = classesToString(model.getReasonerDirectSubclasses(y)); 
                       says("...I suggest to apply threats of the <"+ y.toString()+ "> class (because of "+reasons+"): " +instances);
                    }
                    
                    // a recommendation for threats 
                    IRI z = tmp.searchForExpressionValue(tmp.getSearcherSuperClasses(cls.getIRI()),"ObjectSomeValuesFrom",IRI.create(suggestsThreatProperty));
                    if (z!=null) {
                       //String reasons = instancesToString(findReasonForTarget(sourceFlows,targetFlows,target,cls));
                       //String instances = classesToString(model.getReasonerSubclasses(z)); 
                       says("...I suggest to apply an instance of the <"+ z.toString()+ "> threat class (because of "+reasons+")");
                    }

                 }
              }
          }
       }
       
       says("Done.");
    }
   
    public void test(){
       //String name = "http://www.grsu.by/net/OdTMBaseThreatModel#ContainsHTTPServerComponent";
       String name = "http://www.grsu.by/net/OdTMBaseThreatModel#TestClass";

       flushModel();
       MyAxiom ax = dmodel.searchForSimpleClassDefinition(IRI.create(name));
       if (ax !=null) { ax.print();}
       else {System.out.println("ax=null");}
       
       
       //O bmodel = O.create(domainModel);
       //dmodel.showClassExpressions(dmodel.getSearcherEquivalentClasses(IRI.create(name)) );
       //bmodel.showClassExpressions(bmodel.getSearcherSuperClasses(IRI.create(name)) );
       //Stream tmp = dmodel.getSearcherEquivalentClasses(IRI.create(name));
       //MyAxiom ax = dmodel.searchForExpressionValue(tmp);
       //for (Iterator<OWLClassExpression> iterator = tmp.iterator(); iterator.hasNext(); ){
       //   OWLClassExpression cls = (OWLClassExpression)iterator.next();
       //   MyAxiom ax = dmodel.searchForSimpleClassDefinition(IRI.create(name));
       //   System.out.println(cls.toString());
       //}
       //IRI x = dmodel.searchForExpressionValue(dmodel.getSearcherEquivalentClasses(IRI.create(name)),"ObjectSomeValuesFrom",IRI.create(suggestsProperty));
       //System.out.println("xxxx " +x+ " zzzzz");
       //String str = "ObjectPropertyAssertion(<http://www.grsu.by/net/OdTMBaseThreatModel#agrees> :flow <http://www.grsu.by/net/OdTMBaseThreatModel#protocol_HTTP>)";
       //String str = "ObjectPropertyAssertion(<http://www.grsu.by/net/OdTMBaseThreatModel#isSourceOf> :user :flow)";
       // OWLAxiom ax = model.simpleStringToAxiom(str);
    }
    
    public boolean saveWorkModelToFile(String filename){
       LOGGER.info("trying to save work model as "+ filename);
       return saveToFile(workModel,filename);
    }
   
}
