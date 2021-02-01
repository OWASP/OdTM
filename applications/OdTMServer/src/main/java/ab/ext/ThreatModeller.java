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
   
   protected ArrayList<OWLOntology> models;
   protected OWLOntology baseModel;    // the first model
   protected OWLOntology domainModel;  // the second one
   protected OWLOntology classModel;   // ???
   protected OWLOntology workModel;    // and abox (i.e. diagram) is here
   protected O bmodel;                 // base threat model
   protected O dmodel;                 // domain specific threat model
   protected O cmodel;                 // class model
   protected O model;                  // processor for workModel, init it with workModel
   
   protected String domainModelIRI;    // will be applied to abox as import
   protected String classModelIRI;     // used to recognize extra classes from domain specific model
   
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
   protected static String IsAffectedByTargetsProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#isAffectedByTargets";
   protected static String IsAffectedByTargetProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#isAffectedByTarget";
   protected static String IsAffectedBySourceProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#isAffectedBySource";   
   protected static String SuggestsProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#suggests";
   protected static String SuggestsThreatCategoryProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#suggestsThreatCategory";
   protected static String SuggestsThreatProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#suggestsThreat"; 
   protected static String HasIDProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#hasID"; 
   protected static String HasTextProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#hasText"; 
   protected static String HasTitleProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#hasTitle";
   protected static String HasDescriptionProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#hasDescription";
   protected static String HasSeverityProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#hasSeverity";
   protected static String LabelsSTRIDE = "http://www.grsu.by/net/OdTMBaseThreatModel#labelsSTRIDE";
   
   // !!! legacy
   // init it with base model & domain model before use 
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
      
      // legacy approach, when the base model is used as a domain model 
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

  // init modeller with 
  //   - the base threat model
  //   - list of extra models
  //   - IRI of the domain threat model (can be null)
  //   - IRI of the class model (can be null)
  public boolean init (OWLOntology _baseModel, ArrayList<OWLOntology> _models, String _domainModelIRI, String _classModelIRI){

      // init the base model
      if (_baseModel !=null){
         baseModel = copyOntology(_baseModel);
         if (baseModel == null){
            LOGGER.severe ("unable to copy base model");      
            return false;
         }
         bmodel = O.create(baseModel);
         LOGGER.info("base model: "+ getBaseModelIRI());
      } else {
         LOGGER.severe ("base model is null");      
         return false;
      }

      // init extra models
      models = new ArrayList<OWLOntology>();         
      for (int i=0;i<_models.size();i++){
         OWLOntology m = copyOntology(_models.get(i));
         models.add(m);
      }
      
      if (_domainModelIRI !=null){
         // init domain model   
         domainModel = getOntologyByIRI(_domainModelIRI);
         if (domainModel == null){
            LOGGER.severe ("no domain model in the list of models");      
            return false;
         }
         dmodel = O.create(domainModel);
         domainModelIRI = _domainModelIRI;
         LOGGER.info("domain model: "+ domainModelIRI);
      } else{
         // use the base model
         domainModel = baseModel;
         dmodel = bmodel;
         domainModelIRI = getBaseModelIRI();
         LOGGER.info("use the base model as a domain model: "+ domainModelIRI);
      }
      
      // init class model
      if (_classModelIRI !=null){
         classModel = getOntologyByIRI(_classModelIRI);
         if (classModel == null){
            LOGGER.severe ("no class model in the list of models");      
            return false;
         }
        cmodel = O.create(classModel);
        classModelIRI = _classModelIRI;
        LOGGER.info("class model: "+ classModelIRI);
      }


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

    protected O getModelByIRI1(IRI iri){       
       // is it dmodel?
       if (dmodel != null){
          if (dmodel.hasDefaultPrefix(iri)) return dmodel;
       }
       // is it cmodel?
       if (cmodel != null){
          if (cmodel.hasDefaultPrefix(iri)) return cmodel;
       }
       // is it bmodel?
       if (bmodel!=null) {
          if (bmodel.hasDefaultPrefix(iri)) return bmodel;
       }
       // ignoring owl:thing
       if (iri.toString().equals("http://www.w3.org/2002/07/owl#Thing")) return null;
       
       //what is it?
       String pr = iri.getNamespace();
       String pr1 = pr.substring(0, pr.length() - 1);
       OWLOntology tmp = getOntologyByIRI(pr1);
       if (tmp!=null) return O.create(tmp);
       
       // failed
       LOGGER.severe("could not found model for "+ iri.toString());
       return null;
    }


    // get model by its iri as OWLOntology
    public OWLOntology getOntologyByIRI(String iri){ 
       for (int i=0;i<models.size();i++){
         OWLOntology tmp = models.get(i);
         if (iri.equals(tmp.getOntologyID().getOntologyIRI().get().toString())) return tmp;
       }      
       LOGGER.severe ("no such model " + iri);      
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
   
   // before use of convertJSONclass
   public boolean addClassModel(String in){
      if (in!=null){
         classModelIRI = in;
      }
      return true;
   }
   
   // parse the "class#CloudApplication" like entities
   public IRI convertJSONclass(String in){
      if (in !=null){
         String[] args = in.split("#");
         if (args.length ==2){
            String prefix = args[0];
            String name = args[1];
            if (prefix.equals("class")){
               String res = classModelIRI+"#"+name;
               return IRI.create(res);
            }
         }
      }
      return null;
   }

    public ArrayList<OWLNamedIndividual> findAggressorsJSON(IRI targetIRI, IRI threatIRI){
       List<OWLNamedIndividual> flows = model.getReasonerObjectPropertyValues(targetIRI,IRI.create(IsEdgeOfProperty)).collect(Collectors.toList());
       if (flows == null){
          LOGGER.severe("no flows");
          return null;
       }
       ArrayList<OWLNamedIndividual> res = new ArrayList<OWLNamedIndividual>();
       for (Iterator<OWLNamedIndividual> iterator = flows.stream().iterator(); iterator.hasNext(); ){
           OWLNamedIndividual  flow = (OWLNamedIndividual)iterator.next();
           String prop;
           OWLAxiom ax1 = model.getObjectPropertyAssertionAxiom(IRI.create(HasSourceProperty), flow.getIRI(), targetIRI);
           if (model.containsAxiom1(ax1)){
              prop = IsAffectedBySourceProperty;
           } else{
              prop = IsAffectedByTargetProperty;
           }
           OWLAxiom ax = model.getObjectPropertyAssertionAxiom(IRI.create(prop), flow.getIRI(), threatIRI);
           if (model.containsAxiom1(ax)){
              res.add(flow);
           }
       }       
       return res;
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

         // get class
         // currently, it is in the 'descripion' tag and has the 'class#' prefix
         String cellClass = cell.path("description").textValue();
         IRI classIRI = convertJSONclass(cellClass);
         // if it is ok ...
         if (classIRI != null){
            // ... adds class
            model.addAxiom(model.getClassAssertionAxiom(classIRI, nameIRI));   
         }
         
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
 
   // a bad primer of Jackson's use...
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
           
         model.flush();  
         // get threats from the ontological model
         List<OWLNamedIndividual> threats = model.getReasonerObjectPropertyValues(nameIRI,IRI.create(IsAffectedByProperty)).sorted().collect(Collectors.toList());
         if (threats.size() !=0) {
            ((ObjectNode)cell).put("hasOpenThreats", "true"); // put the 'hasOpenThreats' tag
            ArrayNode nodes = JsonNodeFactory.instance.arrayNode(); // generate an array
            // process each threat
            for (Iterator<OWLNamedIndividual> iterator4 = threats.stream().iterator(); iterator4.hasNext(); ){
               OWLNamedIndividual tmp = (OWLNamedIndividual)iterator4.next(); // instance of the threat
               O modelOfThreat = getModelByIRI1(tmp.getIRI()); // model from what the threat comes (base or domain) 
               String ruleId = tmp.getIRI().toString(); // get rule ID
               String shortIRI = O.getShortIRI(tmp);
   
               // trying to get title from the 'hasTitle' property...
               String title = modelOfThreat.getSearcherDataPropertyValue(tmp.getIRI(), IRI.create(HasTitleProperty)); // get title
               if (title == null) {
                  // ... or from label
                  title = modelOfThreat.getSeacherLabel(tmp);
               }
               // trying to get description from the 'hasDescription' property ...
               String description = modelOfThreat.getSearcherDataPropertyValue(tmp.getIRI(), IRI.create(HasDescriptionProperty)); // get description
               if (description == null){
                  // ... or from comment
                  description = modelOfThreat.getSeacherComment(tmp);
               }
               
               // get type (!!! only one)  <threat> labelsSTRIDE <some STRIDE>
               OWLNamedIndividual typeInstance = model.getObjectPropertyValueFromOntology(tmp.getIRI(),IRI.create(LabelsSTRIDE),bmodel.getIRI());
               //model.showInstances(model.getReasonerObjectPropertyValues(tmp.getIRI(),IRI.create(LabelsSTRIDE)));
               String type =null;
               if (typeInstance!=null) type = getModelByIRI1(typeInstance.getIRI()).getSearcherDataPropertyValue(typeInstance.getIRI(), IRI.create(HasTitleProperty));   
               
               // if it isn't a dataflow
               if (!cellType.equals("tm.Flow")){
                                  
                  // get list of reasons (i.e. flows) 
                  //List<OWLNamedIndividual> flows = model.getReasonerInstances(HasEdgeValueClass).collect(Collectors.toList());
                  //List<OWLNamedIndividual> flows = model.getReasonerObjectPropertyValues(nameIRI,IRI.create(IsEdgeOfProperty)).collect(Collectors.toList());
                  List<OWLNamedIndividual> flows = findAggressorsJSON(nameIRI,tmp.getIRI());
                  for (Iterator<OWLNamedIndividual> iterator = flows.stream().iterator(); iterator.hasNext(); ){
                     OWLNamedIndividual flow = (OWLNamedIndividual)iterator.next();
                     String reasonText = model.getSearcherDataPropertyValue(flow.getIRI(), IRI.create(HasTextProperty));
                     // copy-past
                     ObjectNode threatNode = nodes.addObject(); // add a JSON node
                     // add ruleId
                     threatNode.put("ruleId", ruleId);
                     // add title
                     if (title !=null) threatNode.put("title", title+" (from "+reasonText+")");
                     else threatNode.put("title", shortIRI+" (from "+reasonText+")");
                     // add description
                     if (description !=null) threatNode.put("description", description+" (because of "+reasonText+")" );
                     else threatNode.put("description", shortIRI+ " (because of "+reasonText+")");
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
    
    public String findAggressors(OWLNamedIndividual target, OWLNamedIndividual threat, List<OWLNamedIndividual> flows){
       //List<OWLNamedIndividual> flows = model.getReasonerObjectPropertyValues(target.getIRI(),IRI.create(IsEdgeOfProperty)).collect(Collectors.toList());
       StringBuffer bf = new StringBuffer();
       for (Iterator<OWLNamedIndividual> iterator = flows.stream().iterator(); iterator.hasNext(); ){
           OWLNamedIndividual  flow = (OWLNamedIndividual)iterator.next();
           
           String prop;
           OWLAxiom ax1 = model.getObjectPropertyAssertionAxiom(IRI.create(HasSourceProperty), flow.getIRI(), target.getIRI());
           if (model.containsAxiom1(ax1)){
              prop = IsAffectedBySourceProperty;
           } else{
              prop = IsAffectedByTargetProperty;
           }
           OWLAxiom ax = model.getObjectPropertyAssertionAxiom(IRI.create(prop), flow.getIRI(), threat.getIRI());
           if (model.containsAxiom1(ax)){           
              bf.append(flow.getIRI().toString());
              bf.append(" ");
           }
       }       
       return bf.toString();
    }

    
    public void analyseWithAIEd(){
       // reason the model
       flushModel();
   
       says ("Hello, I love flow based security analysis."); 
       says ("Starting to analize " +model.getIRI() +" ...");
       // get list of flows
       List<OWLNamedIndividual> flows = model.getReasonerInstances(IRI.create(DataFlowClass)).collect(Collectors.toList());
       says("Let me consider flows of this model...");
       says("The model contains "+flows.size()+" data flow(s):");
       // for the flow suggestions
       List<OWLClass> classifiedHasEdge = model.getReasonerSubclasses(IRI.create(ClassifiedHasEdgeClass)).collect(Collectors.toList());
       // get primary type, source & target, also suggestions
       for (Iterator<OWLNamedIndividual> iterator = flows.stream().iterator(); iterator.hasNext(); ){
          // take a flow
          OWLNamedIndividual flow = (OWLNamedIndividual)iterator.next();
          // describe it
          says("The flow " + flow.toString());
          says("...It belongs to the class(es) " + model.classesToString(model.getSearcherTypes(flow)));
          says("...Its source is " + model.getObjectPropertyValue(flow.getIRI(),IRI.create(HasSourceProperty)));
          says("...Its target is " + model.getObjectPropertyValue(flow.getIRI(),IRI.create(HasTargetProperty)));
          // take a list of threats
          List<OWLNamedIndividual> threats = model.getReasonerObjectPropertyValues(flow.getIRI(),IRI.create(IsAffectedByProperty)).collect(Collectors.toList());
          for (Iterator<OWLNamedIndividual> iterator5 = threats.stream().iterator(); iterator5.hasNext(); ){
              // take a threat
              OWLNamedIndividual tmp = (OWLNamedIndividual)iterator5.next();
              says("...It is affected by the threat " + tmp.toString());
          }

          // if some subclass of ClassifiedHasEdge has the suggestsThreat or suggestsThreatCategory properties
          // it is possible to recommend the creation of extra instances of threats
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
       List<OWLNamedIndividual> targets = model.getReasonerInstances(IRI.create(TargetClass)).collect(Collectors.toList());
       says("Let me consider targets...");
       says("Given model contains "+targets.size()+" target(s):");
       for (Iterator<OWLNamedIndividual> iterator = targets.stream().iterator(); iterator.hasNext(); ){
          // get a target
          OWLNamedIndividual target = (OWLNamedIndividual)iterator.next();
          says("The " + target.toString()+ " target:");
          // primary types
          says("...It belongs to the class(es) " + model.classesToString(model.getSearcherTypes(target)));
 
          // sourced flows 
          List<OWLNamedIndividual> sourceFlows = model.getReasonerObjectPropertyValues(target.getIRI(),IRI.create(IsSourceOfProperty)).collect(Collectors.toList());
          for (Iterator<OWLNamedIndividual> iterator2 = sourceFlows.stream().iterator(); iterator2.hasNext(); ){
              OWLNamedIndividual tmp = (OWLNamedIndividual)iterator2.next();
              says("...It's a source of the flow " + tmp.toString());
          }
          
          // target flows
          List<OWLNamedIndividual> targetFlows = model.getReasonerObjectPropertyValues(target.getIRI(),IRI.create(IsTargetOfProperty)).collect(Collectors.toList());
          for (Iterator<OWLNamedIndividual> iterator3 = targetFlows.stream().iterator(); iterator3.hasNext(); ){
              OWLNamedIndividual tmp = (OWLNamedIndividual)iterator3.next();
              says("...It's a target of the flow " + tmp.toString());
          }
          
          // all the flows
          List<OWLNamedIndividual> edgeFlows = model.getReasonerObjectPropertyValues(target.getIRI(),IRI.create(IsEdgeOfProperty)).collect(Collectors.toList());
 
          // affected threats
          List<OWLNamedIndividual> threats = model.getReasonerObjectPropertyValues(target.getIRI(),IRI.create(IsAffectedByProperty)).collect(Collectors.toList());
          for (Iterator<OWLNamedIndividual> iterator4 = threats.stream().iterator(); iterator4.hasNext(); ){
              // take a threat
              OWLNamedIndividual threat = (OWLNamedIndividual)iterator4.next();
              says("...It is affected by the threat " + threat.toString() + " (reasons: "+findAggressors(target,threat,edgeFlows)+")");
          }

          // for suggestions: possible threats & structure
          // list of the 'classified as an edge' classes
          List<OWLClass> classifiedIsEdge = model.getReasonerSubclasses(IRI.create(ClassifiedIsEdgeClass)).collect(Collectors.toList());
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
