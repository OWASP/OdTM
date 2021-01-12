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

// for generation of a domain-specific threat model
// from a security pattern catalog          
public class DSTMGenerator extends OManager{
   private static final Logger LOGGER = Logger.getLogger(LManager.class.getName());

   // base model items
   protected static String HasSourceProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#hasSource";
   protected static String HasTargetProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#hasTarget";
   protected static String IsSourceOfProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#isSourceOf";
   protected static String IsTargetOfProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#isTargetOf";
   protected static String ClassifiedClass = "http://www.grsu.by/net/OdTMBaseThreatModel#Classified";
   protected static String ClassifiedHasEdgeClass = "http://www.grsu.by/net/OdTMBaseThreatModel#ClassifiedHasEdge";
   protected static String ClassifiedIsEdgeClass = "http://www.grsu.by/net/OdTMBaseThreatModel#ClassifiedIsEdge";
   protected static String IsAffectedByProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#isAffectedBy";
   protected static String IsAffectedByTargetsProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#isAffectedByTargets";
   protected static String ThreatClass = "http://www.grsu.by/net/OdTMBaseThreatModel#Threat";
   protected static String HasTitleProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#hasTitle";
   
   // schema items
   static String PATTERN_CLASS = "http://www.grsu.by/net/SecurityPatternCatalogNaiveSchema#Pattern";
   static String THREAT_PATTERN_CLASS = "http://www.grsu.by/net/SecurityPatternCatalogNaiveSchema#ThreatPattern";
   static String SECURITY_PATTERN_CLASS = "http://www.grsu.by/net/SecurityPatternCatalogNaiveSchema#SecurityPattern";
   
   static String HAS_AFFECTED_COMPONENT_PROPERTY = "http://www.grsu.by/net/SecurityPatternCatalogNaiveSchema#hasAffectedComponent";
   static String HAS_AGGRESSOR_PROPERTY = "http://www.grsu.by/net/SecurityPatternCatalogNaiveSchema#hasAggressor";
   static String HAS_AGGRESSOR_ROLE_PROPERTY = "http://www.grsu.by/net/SecurityPatternCatalogNaiveSchema#hasAggressorRole";
   static String ROLE_SERVER = "http://www.grsu.by/net/SecurityPatternCatalogNaiveSchema#role_Server";

   // base model   
   OWLOntology baseModel;
   O bModel;
   
   // target model
   OWLOntology dstmModel;
   O dModel;
   String dstmModelIRI;
   String dstmModelFile;
   
   // SP schema
   OWLOntology schemaModel;
   // SP catalog extra models
   ArrayList<OWLOntology> catalogExtraModels;
   // SP catalog model
   OWLOntology catalogModel;
   O cModel; 
    
   // init everything from a properties file
   public boolean init(PManager conf){

      // base threat model
      String basemodelname = conf.get("BASEMODEL");
      if (basemodelname == null){
         LOGGER.severe("could not get BASEMODEL from properties");
         return false; 
      }
      baseModel = loadFromFile(basemodelname);
      if (baseModel == null) {
         LOGGER.severe("failed to init base model "+basemodelname);
         return false; 
      }
      bModel = O.create(baseModel);
      LOGGER.info ("got base model "+basemodelname);

      // target model's IRI as a string
      dstmModelIRI = conf.get("DSTMIRI");
      if (dstmModelIRI == null){
         LOGGER.severe("could not get DSTMIRI from properties");
         return false; 
      }

      // SP schema      
      String schemamodelname = conf.get("SCHEMAMODEL");
      if (basemodelname == null){
         LOGGER.severe("could not get SCHEMAMODEL from properties");
         return false; 
      }
      schemaModel = loadFromFile(schemamodelname);
      if (schemaModel == null) {
         LOGGER.severe("failed to init schema model "+schemamodelname);
         return false; 
      }
      LOGGER.info ("got base model "+schemamodelname);

      // SP catalog folder
      String folder = conf.get("CATALOGFOLDER");
      if (folder == null){
         LOGGER.severe("could not get CATALOGFOLDER from properties");
         return false; 
      }
      // SP catalog extra models
      catalogExtraModels = new ArrayList<OWLOntology>();
      String lst[] = conf.getAsArray("CATALOGEXTRAMODELS");
      if (lst == null) {
         LOGGER.severe("could not get CATALOGEXTRAMODELS from properties");
         return false; 
      }
      for (int i=0;i<lst.length;i++){
         OWLOntology m = loadFromFile(folder+lst[i]);
         if (m != null){
             catalogExtraModels.add(m);
             LOGGER.info ("got domain model "+folder+lst[i]);
         } else {
             LOGGER.severe ("failed to process "+folder+lst[i]);
         }
      }

      // SP catalog model
      String catalogmodelname = conf.get("CATALOGMODEL");
      if (catalogmodelname == null){
         LOGGER.severe("could not get CATALOGMODEL from properties");
         return false; 
      }
      catalogModel = loadFromFile(folder+catalogmodelname);
      if (catalogModel == null) {
         LOGGER.severe("failed to init the catalog model "+folder+catalogmodelname);
         return false; 
      }
      cModel = O.create(catalogModel);
      LOGGER.info ("got catalog model "+folder+catalogmodelname);

      // create target model
      dstmModel = create(dstmModelIRI);
      // import both the schema ontology & the base threat model
      addImportDeclaration(dstmModel,cModel.getIRI().toString());
      addImportDeclaration(dstmModel,bModel.getIRI().toString());
      dModel = O.create(dstmModel);
      LOGGER.info ("created dstm model "+dstmModelIRI);
      
      // target model's file as a string
      dstmModelFile = conf.get("DSTMMODEL");
      if (dstmModelFile == null){
         LOGGER.severe("could not get DSTMMODEL from properties");
         return false; 
      }
      LOGGER.info ("dstm model will be saved as "+dstmModelFile);

      return true;
   };

   // find primary classes for given instances 
   // (instance of the schema and a SP catalog should belong to only one class);
   // uses cModel
   protected ArrayList<OWLClass> getClassesFromInstances(Stream<OWLNamedIndividual> instances){
      ArrayList<OWLClass> out = new ArrayList<OWLClass>();
      for (Iterator<OWLNamedIndividual> iterator = instances.iterator(); iterator.hasNext(); ){
         OWLNamedIndividual instance = (OWLNamedIndividual)iterator.next();
         OWLClass cls = cModel.getReasonerDirectType(instance);
         if (cls != null) out.add(cls);
      }
      return out;
   }


   // creates the class like 'hasSource some <Source Class> hasTarget some <Target Class>'
   // uses dModel
   // returns IRI
   public IRI hasSourceANDhasTarget(OWLClass source, OWLClass target){
      String sourceShortName = source.getIRI().getShortForm().toString();
      String targetShortName = target.getIRI().getShortForm().toString();

      // hasSource some <Source Class>
      String hasSourceShortName = "HasSource"+sourceShortName;
      IRI hasSourceIRI = IRI.create(dModel.getDefaultPrefix()+hasSourceShortName);
      OWLAxiom hasSourceAxiom = dModel.getDefinedClassSome(hasSourceIRI, IRI.create(HasSourceProperty), source.getIRI());
      dModel.addAxiom(hasSourceAxiom);

      // hasTarget some <Target Class>
      String hasTargetShortName = "HasTarget"+targetShortName;
      IRI hasTargetIRI = IRI.create(dModel.getDefaultPrefix()+hasTargetShortName);
      OWLAxiom hasTargetAxiom = dModel.getDefinedClassSome(hasTargetIRI, IRI.create(HasTargetProperty), target.getIRI());
      dModel.addAxiom(hasTargetAxiom);
      
      IRI andClass = IRI.create(dModel.getDefaultPrefix()+ hasSourceShortName+ "And" +hasTargetShortName);
      OWLAxiom andClassAxiom= dModel.getDefinedClassAnd(andClass, hasSourceIRI, hasTargetIRI);
      dModel.addAxiom(andClassAxiom);
      
      return andClass;
   }



   
   // creates the class like '<Source Class> and isSourceOf some (hasTarget some <Target Class>)'
   // uses dModel
   // returns IRI
   public IRI sourceAndHasTarget(OWLClass source, OWLClass target){
      String sourceShortName = source.getIRI().getShortForm().toString();
      String targetShortName = target.getIRI().getShortForm().toString();

      // hasTarget some <Target Class>
      String hasTargetShortName = "HasTarget"+targetShortName;
      IRI hasTargetIRI = IRI.create(dModel.getDefaultPrefix()+hasTargetShortName);
      OWLAxiom hasTargetAxiom = dModel.getDefinedClassSome(hasTargetIRI, IRI.create(HasTargetProperty), target.getIRI());
      dModel.addAxiom(hasTargetAxiom);
      
      // isSourceOf some (hasTarget some <Target Class>)
      String isSourceOfShortName = "IsSourceOf"+hasTargetShortName;                   
      IRI isSourceOfIRI = IRI.create(dModel.getDefaultPrefix()+isSourceOfShortName);
      OWLAxiom isSourceOfAxiom = dModel.getDefinedClassSome(isSourceOfIRI, IRI.create(IsSourceOfProperty),hasTargetIRI );
      dModel.addAxiom(isSourceOfAxiom);
      
      // <Source Class> and isSourceOf some (hasTarget some <Target Class>)
      IRI andClass = IRI.create(dModel.getDefaultPrefix()+ sourceShortName+ "And" +isSourceOfShortName);
      OWLAxiom andClassAxiom= dModel.getDefinedClassAnd(andClass, source.getIRI(), isSourceOfIRI);
      dModel.addAxiom(andClassAxiom);
      
      LOGGER.info("... flow: " + sourceShortName + " >>> " +targetShortName);
      
      return andClass;
   }
      
   // creates the class like '<Target Class> and isTargetOf some (hasSource some <Source Class>)'
   // uses dModel
   // returns IRI
   public IRI targetAndHasSource(OWLClass target, OWLClass source){
      String sourceShortName = source.getIRI().getShortForm().toString();
      String targetShortName = target.getIRI().getShortForm().toString();

      // hasSource some <Source Class>
      String hasSourceShortName = "HasSource"+sourceShortName;
      IRI hasSourceIRI = IRI.create(dModel.getDefaultPrefix()+hasSourceShortName);
      OWLAxiom hasSourceAxiom = dModel.getDefinedClassSome(hasSourceIRI, IRI.create(HasSourceProperty), source.getIRI());
      dModel.addAxiom(hasSourceAxiom);
      
      // isTargetOf some (hasSource some <Source Class>)
      String isTargetOfShortName = "IsTargetOf"+hasSourceShortName;                   
      IRI isTargetOfIRI = IRI.create(dModel.getDefaultPrefix()+isTargetOfShortName);
      OWLAxiom isTargetOfAxiom = dModel.getDefinedClassSome(isTargetOfIRI, IRI.create(IsTargetOfProperty),hasSourceIRI);
      dModel.addAxiom(isTargetOfAxiom);
      
      // <Target Class> and isTargetOf some (hasSource some <Source Class>)
      IRI andClass = IRI.create(dModel.getDefaultPrefix()+ targetShortName+ "And" +isTargetOfShortName);
      OWLAxiom andClassAxiom= dModel.getDefinedClassAnd(andClass, target.getIRI(), isTargetOfIRI);
      dModel.addAxiom(andClassAxiom);
      
      LOGGER.info("... flow: " + targetShortName + " <<< " +sourceShortName);
      
      return andClass;
   }


   public boolean process(){
      // get patterns' list from cModel, e.g. the catalog model 
      Stream<OWLNamedIndividual> patterns = cModel.getReasonerInstances(IRI.create(PATTERN_CLASS));
      for (Iterator<OWLNamedIndividual> iterator = patterns.iterator(); iterator.hasNext(); ){
          // get a pattern
          OWLNamedIndividual pattern = (OWLNamedIndividual)iterator.next();
          // get its type, e.g. threat pattern or security pattern
          boolean isThreatPattern = cModel.isReasonerIndividualBelongsToClass(pattern.getIRI(),IRI.create(THREAT_PATTERN_CLASS));          
          // get affected components '<pattern> hasAffectedComponent <affectedComponent>'
          ArrayList<OWLClass> affectedComponents = getClassesFromInstances(cModel.getReasonerObjectPropertyValues(pattern.getIRI(),IRI.create(HAS_AFFECTED_COMPONENT_PROPERTY)));
          // get aggressor components '<pattern> hasAggressor <aggressorComponent>'
          ArrayList<OWLClass> aggressorComponents = getClassesFromInstances(cModel.getReasonerObjectPropertyValues(pattern.getIRI(),IRI.create(HAS_AGGRESSOR_PROPERTY)));
          // get aggressor role
          // by default aggressor is a client
          // it becomes a server, if meets the ...role_Server '<pattern> hasAggressorRole <role_Server|role_Client>'
          boolean isClient = true;
          List<OWLNamedIndividual> aggressorRoles = cModel.getReasonerObjectPropertyValues(pattern.getIRI(),IRI.create(HAS_AGGRESSOR_ROLE_PROPERTY)).collect(Collectors.toList());
          for (OWLNamedIndividual aggressorRole : aggressorRoles) {
             if (aggressorRole.getIRI().equals(IRI.create(ROLE_SERVER))) isClient = false;
          }
          LOGGER.info("Pattern: "+pattern.getIRI().toString()+ " isThreatPattern: "+isThreatPattern+ " (Aggressor)_isClient="+isClient);
          
          // create templates
          // for each affected component ...
          for (OWLClass affectedComponent : affectedComponents) {
             // ... they takes all the aggressors
             for (OWLClass aggressorComponent : aggressorComponents){
                String aggressorShortName = aggressorComponent.getIRI().getShortForm().toString();
                String affectedShortName = affectedComponent.getIRI().getShortForm().toString();
                
                IRI andIRI; // IRI of template for target (isAffectedBy)
                IRI andFlowIRI; // IRI of template for flow (isAffectedByTargets)
                if (isClient){
                   // aggressor is a client, 
                   // set template like <affectedComponent> <<< <aggressorComponent>
                   andIRI = targetAndHasSource (affectedComponent, aggressorComponent);
                   andFlowIRI = hasSourceANDhasTarget(aggressorComponent, affectedComponent);
                } else {
                   // aggressor is a server, 
                   // set template like <affectedComponent> >>> <aggressorComponent>
                   andIRI = sourceAndHasTarget(affectedComponent, aggressorComponent);
                   andFlowIRI = hasSourceANDhasTarget(affectedComponent, aggressorComponent);
                }  
                                    
                
                if (isThreatPattern){
                   // it is a threat pattern
                   // map it to the template like '<and class> isAffectedBy <pattern instance>'
                   dModel.addAxiom(dModel.getSubClassValue(andIRI, IRI.create(IsAffectedByProperty), pattern.getIRI()));
                   // set like '<and class> is subclass of ClassifiedIsEdge'
                   dModel.addAxiom(dModel.getSubClass(andIRI,IRI.create(ClassifiedIsEdgeClass)));
                   // set like '<pattern> is an instance of the Threat class'
                   dModel.addAxiom(dModel.getClassAssertionAxiom(IRI.create(ThreatClass),pattern.getIRI()));
                                      
                   // create a template for a flow like '<andFlow class> isAffectedByTargets <pattern instance>'
                   dModel.addAxiom(dModel.getSubClassValue(andFlowIRI, IRI.create(IsAffectedByTargetsProperty), pattern.getIRI()));
                   // set like '<andFlow class> is subclass of ClassifiedHasEdge'
                   dModel.addAxiom(dModel.getSubClass(andFlowIRI,IRI.create(ClassifiedHasEdgeClass)));

                   
                } else{
                   // it is a security pattern
                   // ...
                }
                
             }
             
          }

      }

      return true;
   }
      
   public boolean save(){
      return saveToFile(dstmModel,dstmModelFile);
   }
      
      
}
