package ab.ext;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

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
   protected static String ProcessClass = "http://www.grsu.by/net/OdTMBaseThreatModel#Process";
   protected static String ExternalInteractorClass = "http://www.grsu.by/net/OdTMBaseThreatModel#ExternalInteractor";
   protected static String DataStoreClass = "http://www.grsu.by/net/OdTMBaseThreatModel#DataStore";
   protected static String ClassifiedClass = "http://www.grsu.by/net/OdTMBaseThreatModel#Classified";
   protected static String ClassifiedIsEdgeClass = "http://www.grsu.by/net/OdTMBaseThreatModel#ClassifiedIsEdge";
   protected static String ClassifiedHasEdgeClass = "http://www.grsu.by/net/OdTMBaseThreatModel#ClassifiedHasEdge";
   protected static String HasSourceProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#hasSource";
   protected static String HasTargetProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#hasTarget";
   protected static String HasEdgeProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#hasEdge";
   protected static String IsSourceOfProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#isSourceOf";
   protected static String IsTargetOfProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#isTargetOf";
   protected static String IsEdgeOfProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#isEdgeOf";   
   protected static String IsAffectedByProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#isAffectedBy";
   protected static String SuggestsProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#suggests";
   protected static String SuggestsThreatCategoryProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#suggestsThreatCategory";
   protected static String SuggestsThreatProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#suggestsThreat"; 
   protected static String HasIDProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#hasID"; 
   protected static String HasTextProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#hasText"; 
   protected static String HasTitleProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#hasTitle";
   protected static String HasDescriptionProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#hasDescription";
   protected static String HasSeverityProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#hasSeverity";
   protected static String LabelsSTRIDE = "http://www.grsu.by/net/OdTMBaseThreatModel#labelsSTRIDE";
   
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
   

    public void fillWorkModel(){
       model.fill();
    }
   
    public void flushModel(){
       model.flush();
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
    
 
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Threat Modeller & JSON 
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// 
 
   // convert TD objects to ontology's objects
   public IRI convertJSONtype(String type){
      if (type.equals("tm.Process")) return IRI.create(ProcessClass);
      if (type.equals("tm.Actor")) return IRI.create(ExternalInteractorClass);  
      if (type.equals("tm.Store")) return IRI.create(DataStoreClass);
      if (type.equals("tm.Flow")) return IRI.create(DataFlowClass);
      return null;
   }
   
   // takes a JSonNode that represents a diagram and creates a work model.
   // Caution! A poor example of Jackson's use. Use ObjectMapper instead. 
   public boolean createWorkModelFromJSON(JsonNode diagram){
      // create empty model with the domain model import
      workModel = create("http://tmp.local");
      addImportDeclaration(workModel,getDomainModelIRI());
      model = O.create(workModel);
      if (model == null) {
         LOGGER.severe ("unable to create ontology");      
         return false;
      }
      // get all the items of the diagram
      JsonNode cells = diagram.path("diagramJson").path("cells");
      Iterator<JsonNode> itr1 = cells.elements();
      while (itr1.hasNext()) {
         // consider an item
         JsonNode cell = itr1.next(); 

         // get item's ID
         String cellID = cell.path("id").textValue();
         if (cellID == null){
            LOGGER.severe("could not find id ");
            return false;               
         }
         // generate name
         IRI nameIRI = IRI.create(model.getDefaultPrefix()+O.safeIRI(cellID));
         // add ID
         model.addAxiom(model.getIndividualDataProperty(nameIRI,IRI.create(HasIDProperty),cellID));
         
         // get type        
         String cellType = cell.path("type").textValue();
         IRI typeIRI = convertJSONtype(cellType);
         if (typeIRI == null){
            LOGGER.severe("could not find the type "+ cellType);
            return false;   
         }
         // add type
         model.addAxiom(model.getClassAssertionAxiom(typeIRI, nameIRI));

         // get & add text
         String cellText = null;
         if (cellType.equals("tm.Flow")){
            JsonNode labels = cell.path("labels");
            Iterator<JsonNode> itr2 = labels.elements();
            if (itr2.hasNext()) cellText=((JsonNode)itr2.next()).path("attrs").path("text").path("text").textValue();
         } else{
            cellText = cell.path("attrs").path("text").path("text").textValue();
         }
         if (cellText != null) {
            model.addAxiom(model.getIndividualDataProperty(nameIRI,IRI.create(HasTextProperty),cellText));
         }

         // for flows add source & target edges
         if (cellType.equals("tm.Flow") ){
             String sourceID = cell.path("source").path("id").textValue();
             if (sourceID !=null) {
                 IRI sourceIRI = IRI.create(model.getDefaultPrefix()+O.safeIRI(sourceID));
                 model.addAxiom(model.getObjectPropertyAssertionAxiom(IRI.create(HasSourceProperty), nameIRI, sourceIRI));
             }
             String targetID = cell.path("target").path("id").textValue();
             if (targetID !=null) {
                 IRI targetIRI = IRI.create(model.getDefaultPrefix()+O.safeIRI(targetID));
                 model.addAxiom(model.getObjectPropertyAssertionAxiom(IRI.create(HasTargetProperty), nameIRI, targetIRI));
             }
         }
                  
         //         
                  
      }  
      
      return true;
   }
 
   // a bad primer of Jackson's use.
   public boolean applyAxiomsToJSON(JsonNode diagram){
      JsonNode cells = diagram.path("diagramJson").path("cells");
      Iterator<JsonNode> itr1 = cells.elements();
      while (itr1.hasNext()) {
         // consider an item, i.e. cell
         JsonNode cell = itr1.next(); 
         
         // get ID
         String cellID = cell.path("id").textValue();
         if (cellID == null){
            LOGGER.severe("could not find id ");
            return false;               
         }   
         // get name
         IRI nameIRI = IRI.create(model.getDefaultPrefix()+O.safeIRI(cellID));
         
         // get type        
         String cellType = cell.path("type").textValue();
         IRI typeIRI = convertJSONtype(cellType);
         if (typeIRI == null){
            LOGGER.severe("could not find the type "+ cellType);
            return false;   
         }   
           

         
         // get threats from the ontological model
         List<OWLNamedIndividual> threats = model.getReasonerObjectPropertyValues(nameIRI,IRI.create(IsAffectedByProperty)).collect(Collectors.toList());
         if (threats.size() !=0) {
            ((ObjectNode)cell).put("hasOpenThreats", "true"); // put the 'hasOpenThreats' tag
            ArrayNode nodes = JsonNodeFactory.instance.arrayNode(); // generate an array
            // process each threat
            for (Iterator<OWLNamedIndividual> iterator4 = threats.stream().iterator(); iterator4.hasNext(); ){
               OWLNamedIndividual tmp = (OWLNamedIndividual)iterator4.next(); // instance of the threat
               O modelOfThreat = getModelByIRI(tmp.getIRI()); // model from what the threat comes (base or domain) 
               String ruleId = tmp.getIRI().toString(); // get rule ID
               String shortIRI = O.getShortIRI(tmp);
   
               String title = modelOfThreat.getSearcherDataPropertyValue(tmp.getIRI(), IRI.create(HasTitleProperty)); // get title
               String description = modelOfThreat.getSearcherDataPropertyValue(tmp.getIRI(), IRI.create(HasDescriptionProperty)); // get description
               // get type (!!! only one)
               OWLNamedIndividual typeInstance = modelOfThreat.getObjectPropertyValue(tmp.getIRI(),IRI.create(LabelsSTRIDE));
               //String type = typeInstance.getIRI().toString();
               String type = getModelByIRI(typeInstance.getIRI()).getSearcherDataPropertyValue(typeInstance.getIRI(), IRI.create(HasTitleProperty));   
               
               // if it isn't a dataflow
               if (!cellType.equals("tm.Flow")){
                 
                  // trying to find reasons
                  // hasEdge value nameIRI(target) - ask the diagram's model 
                  IRI HasEdgeValueClass = IRI.create(model.getDefaultPrefix()+"HasEdgeValue"+O.safeIRI(cellID));
                  OWLAxiom hasEdgeValue = model.getDefinedClassValue(HasEdgeValueClass, IRI.create(HasEdgeProperty),nameIRI);
                  model.addAxiom(hasEdgeValue);
                  // isAffectedBy value tmp(threat) - ask the diagram's model 
                  IRI IsAffectedByValueClass = IRI.create(model.getDefaultPrefix()+"IsAffectedByValue"+shortIRI);
                  OWLAxiom isAffectedByValue = model.getDefinedClassValue(IsAffectedByValueClass, IRI.create(IsAffectedByProperty),tmp.getIRI());
                  model.addAxiom(isAffectedByValue);
                  // hasEdge some IsAffectedByValueClass 
                  IRI HasEdgeSomeClass = IRI.create(model.getDefaultPrefix()+"hasEdgeSome"+O.getShortIRI(IsAffectedByValueClass));
                  OWLAxiom hasEdgeSome = model.getDefinedClassSome(HasEdgeSomeClass, IRI.create(HasEdgeProperty),IsAffectedByValueClass);
                  model.addAxiom(hasEdgeSome);
                  // (hasEdge value nameIRI(target))=HasEdgeValueClass and HasEdgeSomeClass=(hasEdge some (isAffectedBy value tmp(threat)))
                  IRI AndClass = IRI.create(model.getDefaultPrefix()+O.getShortIRI(HasEdgeValueClass)+"AND"+O.getShortIRI(HasEdgeSomeClass));
                  OWLAxiom andClassAxiom = model.getDefinedClassAnd(AndClass, HasEdgeValueClass,HasEdgeSomeClass);
                  model.addAxiom(andClassAxiom);
                  
                  model.flush();
                  
                  List<OWLNamedIndividual> flows = model.getReasonerInstances(AndClass).collect(Collectors.toList());
                  for (Iterator<OWLNamedIndividual> iterator = flows.stream().iterator(); iterator.hasNext(); ){
                     OWLNamedIndividual flow = (OWLNamedIndividual)iterator.next();
                     String reasonText = model.getSearcherDataPropertyValue(flow.getIRI(), IRI.create(HasTextProperty));
                     // copy-past
                     ObjectNode threatNode = nodes.addObject(); // add a JSON node
                     // add ruleId
                     threatNode.put("ruleId", ruleId);
                     // add title
                     if (title !=null) threatNode.put("title", title+" (from "+reasonText+")");
                     else threatNode.put("title", shortIRI);
                     // add description
                     if (description !=null) threatNode.put("description", description+" (because of "+reasonText+")" );
                     else threatNode.put("description", shortIRI);
                     // add status
                     threatNode.put("status", "Open");
                     // add severity (Medium at the moment)
                     threatNode.put("severity", "Medium");
                     // add type 
                     if (type != null) threatNode.put("type", type);                     

                  }
                  
               }  else {
                  // copy-past
                  ObjectNode threatNode = nodes.addObject(); // add a JSON node
                  // add ruleId
                  threatNode.put("ruleId", ruleId);
                  // add title
                  if (title !=null) threatNode.put("title", title);
                  else threatNode.put("title", shortIRI);
                  // add description
                  if (description !=null) threatNode.put("description", description);
                  else threatNode.put("description", shortIRI);
                  // add status
                  threatNode.put("status", "Open");
                  // add severity (Medium at the moment)
                  threatNode.put("severity", "Medium");
                  // add type 
                  if (type != null) threatNode.put("type", type);
                  
               }        
               
               // standard fields
               // {
               //   "ruleId": "b2a6d40d-d3f8-4750-8e4d-c02cc84b13dc",
               //   "title": "Generic spoofing threat",
               //   "type": "Spoofing",
               //   "status": "Open",
               //   "severity": "Medium",
               //   "description": "A generic spoofing threat",
               //   "$$hashKey": "object:59"
               // }

            }
            ((ObjectNode)cell).set("threats",nodes); // apply nodes to cell (i.e. item)
            
                        
         }

      }      
      //saveToFile(model.get(),"cases/model.owl");
      return true;
   }
 
 
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// AIEd & simple analysis of axioms
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////        

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

    //                                                                        source flows                       target flows        instance i.e. target       class
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
    

    private void says(String str){
       String name = "AIEd";       
       System.out.println(name+": "+str);
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
                    // !!! takes only one value
                    IRI y = tmp.searchForExpressionValue(tmp.getSearcherSuperClasses(cls.getIRI()),"ObjectSomeValuesFrom",IRI.create(SuggestsThreatProperty));
                    if (y!=null) {
                       String instances = model.instancesToString(model.getReasonerInstances(y)); 
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
                    String reasons = tmp.instancesToString(findReasonForTarget(sourceFlows,targetFlows,target,cls));
                    
                    // a recommendation for internal structure
                    // !!! takes only one value
                    IRI x = tmp.searchForExpressionValue(tmp.getSearcherSuperClasses(cls.getIRI()),"ObjectSomeValuesFrom",IRI.create(SuggestsProperty));
                    if (x!=null) {
                       String instances = model.instancesToString(model.getReasonerInstances(x));
                       says("...I suggest to apply an internal component of the <"+ x.toString() + "> class (because of "+reasons+"): "+instances);
                    }

                    // a recommendation for threat categories
                    // !!! takes only one value
                    IRI y = tmp.searchForExpressionValue(tmp.getSearcherSuperClasses(cls.getIRI()),"ObjectSomeValuesFrom",IRI.create(SuggestsThreatCategoryProperty));
                    if (y!=null) {
                       String instances = model.classesToString(model.getReasonerDirectSubclasses(y)); 
                       says("...I suggest to apply threats of the <"+ y.toString()+ "> class (because of "+reasons+"): " +instances);
                    }
                    
                    // a recommendation for threats
                    // !!! takes only one value 
                    IRI z = tmp.searchForExpressionValue(tmp.getSearcherSuperClasses(cls.getIRI()),"ObjectSomeValuesFrom",IRI.create(SuggestsThreatProperty));
                    if (z!=null) {
                       says("...I suggest to apply an instance of the <"+ z.toString()+ "> threat class (because of "+reasons+")");
                    }

                 }
              }
          }
       }
       
       says("Done.");
    }
   

    
    public boolean saveWorkModelToFile(String filename){
       LOGGER.info("trying to save work model as "+ filename);
       return saveToFile(workModel,filename);
    }
   
}
