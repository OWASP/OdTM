package ab.im;

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

public class IMOntologyGenerator extends OManager{
   private static final Logger LOGGER = Logger.getLogger(LManager.class.getName());
   
   // base model items
   protected static String HasSourceProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#hasSource";
   protected static String HasTargetProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#hasTarget";
   protected static String IsSourceOfProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#isSourceOf";
   protected static String IsTargetOfProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#isTargetOf";
   protected static String ClassifiedClass = "http://www.grsu.by/net/OdTMBaseThreatModel#Classified";
   protected static String ClassifiedStencilClass = "http://www.grsu.by/net/OdTMBaseThreatModel#ClassifiedStencil";   
   protected static String ClassifiedHasEdgeClass = "http://www.grsu.by/net/OdTMBaseThreatModel#ClassifiedHasEdge";
   protected static String ClassifiedIsEdgeClass = "http://www.grsu.by/net/OdTMBaseThreatModel#ClassifiedIsEdge";
   protected static String IsAffectedByProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#isAffectedBy";
   protected static String IsAffectedByTargetsProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#isAffectedByTargets";
   protected static String IsAffectedByTargetProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#isAffectedByTarget";
   protected static String IsAffectedBySourceProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#isAffectedBySource";   
   protected static String ThreatClass = "http://www.grsu.by/net/OdTMBaseThreatModel#Threat";
   protected static String StencilClass = "http://www.grsu.by/net/OdTMBaseThreatModel#Stencil";
   protected static String HasTitleProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#hasTitle";
   
   protected static String refToTacticProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#refToTactic";
   protected static String refToATTCKProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#refToATTCK";
   protected static String refToCAPECProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#refToCAPEC";
   protected static String refToCWEProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#refToCWE";
   protected static String refToCVEProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#refToCVE";
   protected static String isRefToATTCKProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#isRefToATTCK";
   protected static String isRefToCAPECProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#isRefToCAPEC";
   protected static String isRefToCWEProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#isRefToCWE";
   protected static String refToCAPECreasonedProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#refToCAPECreasoned";
   protected static String refToCWEreasonedProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#refToCWEreasoned";
   protected static String refToCVEreasonedProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#refToCVEreasoned";
   protected static String ThreatRestrictionClass = "http://www.grsu.by/net/OdTMBaseThreatModel#ThreatRestriction";
   protected static String SatisfiesProperty = "http://www.grsu.by/net/OdTMBaseThreatModel#satisfiesThreatRestriction";

   // threat model
   // DataSource 
   protected static String DataSourceClass = "http://www.grsu.by/net/OdTMIntegratedModel#DataSource";
   // DataComponent
   protected static String DataComponentClass = "http://www.grsu.by/net/OdTMIntegratedModel#DataComponent";
   // HasDataComponents - indicates that a threat has associated data components
   protected static String HasDataComponentsClass = "http://www.grsu.by/net/OdTMIntegratedModel#HasDataComponents";
   // HasDataComponent - a parent class for HasDataComponent_XXX 
   protected static String HasDataComponentClass = "http://www.grsu.by/net/OdTMIntegratedModel#HasDataComponent";
   protected static String CAPECClass = "http://www.grsu.by/net/OdTMIntegratedModel#CAPEC";
   protected static String ATTCKClass = "http://www.grsu.by/net/OdTMIntegratedModel#ATTCKTechnique";
   protected static String ATTCKTacticClass = "http://www.grsu.by/net/OdTMIntegratedModel#ATTCKTactic";
   protected static String CWEClass = "http://www.grsu.by/net/OdTMIntegratedModel#CWE";
   protected static String CVEClass = "http://www.grsu.by/net/OdTMIntegratedModel#CVE";


   // base model   
   OWLOntology baseModel;
   O bModel;
   
   // target model
   OWLOntology dstmModel;
   O dModel;
   String dstmModelIRI;
   String dstmModelFile;
   String dstmModelFileTTL;
   String dstmModelFileTTL1;
   
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

      // create target model
      dstmModel = create(dstmModelIRI);
      // import both the schema ontology & the base threat model
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

      dstmModelFileTTL = conf.get("DSTMMODELTTL");
      if (dstmModelFileTTL == null){
         LOGGER.severe("could not get DSTMMODELTTL from properties");
         return false; 
      }
      LOGGER.info ("RDF dataset will be saved as "+dstmModelFileTTL);
      
      dstmModelFileTTL1 = conf.get("DSTMMODELTTL1");
      if (dstmModelFileTTL1 == null){
         LOGGER.severe("could not get DSTMMODELTTL1 from properties");
         return false; 
      }
      LOGGER.info ("RDF dataset will be saved as "+dstmModelFileTTL1);


      return true;
   };   

////////////////////////////////////////////////////////////////////////////////////
// ATTCK Tactics
   public void processATTCKTactic(ATTCKTactic tactic){
      IRI tacticIRI = IRI.create(dModel.getDefaultPrefix()+"ATTCKTactic-"+tactic.getX_mitre_shortname());
      
      if (tactic.getExtID() !=null) {
         dModel.addAxiom(dModel.getIndividualAnnotation(tacticIRI, tactic.getExtID()+" "+tactic.getName(), "en"));
         dModel.addAxiom(dModel.getIndividualComment(tacticIRI, tactic.getExtID()+" "+tactic.getName()+" ("+tactic.getExtUrl()+")", "en"));
      }

      // a tactic belongs to the ATTCKTactic class
      dModel.addAxiom(dModel.getClassAssertionAxiom(IRI.create(ATTCKTacticClass), tacticIRI));


   }
   
////////////////////////////////////////////////////////////////////////////////////
// ATTCK Attack Patterns

   public void processATTCKAttackPattern(ATTCKAttackPattern pattern){
      IRI patternIRI = IRI.create(dModel.getDefaultPrefix()+pattern.getId());

      String ma = null;
      IRI maIRI = null;
          
      // apply label & comment to the threat (attack pattern)
      if (pattern.getExtID() !=null) {
         dModel.addAxiom(dModel.getIndividualAnnotation(patternIRI, pattern.getExtID(), "en"));
         dModel.addAxiom(dModel.getIndividualComment(patternIRI, pattern.getExtID()+" ("+pattern.getExtUrl()+")", "en"));
 
        // applying references to ATTCK
         ma= "ATTCK-"+O.safeIRI(pattern.getExtID());
         maIRI = IRI.create(dModel.getDefaultPrefix()+ma);
       
         dModel.addAxiom(dModel.getClassAssertionAxiom(IRI.create(ATTCKClass), maIRI));
         dModel.addAxiom(dModel.getObjectPropertyAssertionAxiom(IRI.create(refToATTCKProperty),patternIRI,maIRI));
         dModel.addAxiom(dModel.getIndividualAnnotation(maIRI, "ATT&CK "+pattern.getExtID(), "en"));
         dModel.addAxiom(dModel.getIndividualComment(maIRI, "ATT&CK " +pattern.getExtID()+" ("+pattern.getExtUrl()+")", "en"));
      }
      else LOGGER.severe("no reference to ATTCK "+pattern.getId());

      // an attack pattern belongs to the Threat class
      dModel.addAxiom(dModel.getClassAssertionAxiom(IRI.create(ThreatClass), patternIRI));

      List<ATTCKDataSourceComponent> dsList = pattern.getDataSourceComponents();
      if ( dsList !=null && dsList.size()!=0 ){
         dModel.addAxiom(dModel.getClassAssertionAxiom(IRI.create(HasDataComponentsClass), patternIRI));
         for (int i=0;i<dsList.size();i++){
           String source = dsList.get(i).getSource()+"_DataSource";
           String component = dsList.get(i).getComponent()+"_DataComponent";
           IRI sourceIRI = IRI.create(dModel.getDefaultPrefix()+source);
           IRI componentIRI = IRI.create(dModel.getDefaultPrefix()+component);
           // <Datasource> is a subclass of DataSource
           dModel.addAxiom(dModel.getSubClass(sourceIRI, IRI.create(DataSourceClass)));
           // XXX_DataComponent is subclass of DataComponent
           dModel.addAxiom(dModel.getSubClass(componentIRI, IRI.create(DataComponentClass)));

           String hasComponent = "HasDataComponent_"+dsList.get(i).getComponent();
           IRI hasComponentIRI = IRI.create(dModel.getDefaultPrefix()+hasComponent);
           // HasDataComponent_XXX is a subclass of HasDataComponent
           dModel.addAxiom(dModel.getSubClass(hasComponentIRI, IRI.create(HasDataComponentClass)));
           // an attack pattern belongs to HasDataComponent_XXX
           dModel.addAxiom(dModel.getClassAssertionAxiom(hasComponentIRI, patternIRI));

          
           String isTargetShortName = "IsTargetOf"+component;
           IRI isTargetIRI = IRI.create(dModel.getDefaultPrefix()+isTargetShortName);
           // IsTargetOfSomeDataComponent = isTargetOf some XXX_DataComponent
           OWLAxiom isTargetAxiom = dModel.getDefinedClassSome(isTargetIRI, IRI.create(IsTargetOfProperty), componentIRI);
           dModel.addAxiom(isTargetAxiom);

           // IsTargetOfSomeDataComponent isAffectedBy {threat}
           dModel.addAxiom(dModel.getSubClassValue(isTargetIRI, IRI.create(IsAffectedByProperty), patternIRI));
           // IsTargetOfSomeDataComponent is subclass of ClassifiedIsEdge
           dModel.addAxiom(dModel.getSubClass(isTargetIRI,IRI.create(ClassifiedIsEdgeClass)));

           String flowShortName = "HasTarget"+component;
           IRI flowIRI = IRI.create(dModel.getDefaultPrefix()+flowShortName);
           // XXXFlow hasTarget some IsTargetOfSomeDataComponent 
           dModel.addAxiom(dModel.getDefinedClassSome(flowIRI, IRI.create(HasTargetProperty), isTargetIRI));
           // XXXFlow isAffectedByTarget {pattern}
           // dModel.addAxiom(dModel.getSubClassValue(flowIRI, IRI.create(IsAffectedByTargetProperty), patternIRI));
           // XXXFlow is subclass of ClassifiedHasEdge
           dModel.addAxiom(dModel.getSubClass(flowIRI,IRI.create(ClassifiedHasEdgeClass)));

         }
      } /*else {
         LOGGER.severe("no datasource for "+pattern.getId());
      }*/
      
      // restrctions      
      applyRestrictions(pattern.getXMitrePlatforms(), "Platform", patternIRI);
      applyRestrictions(pattern.getXMitrePermissionsRequired(), "PermissionsRequired", patternIRI);
      applyRestrictions(pattern.getXMitreEffectivePermissions(), "EffectivePermissions", patternIRI);
      applyRestrictions(pattern.getXMitreImpactType(), "ImpactType", patternIRI);

      // external references
      List<ATTCKExternalReference> extList = pattern.getExternalReferences();
      if ( extList !=null && extList.size()!=0 ){
         for (int i=0;i<extList.size();i++){
            ATTCKExternalReference ref = extList.get(i);
            if (ref.getSource_name().equals("capec")) {
               if (ma!=null) {
                  String capec= ref.getExternal_id();
                  IRI capecIRI = IRI.create(dModel.getDefaultPrefix()+capec);
                  // <capecIRI> is an instance of CAPECClass 
                  dModel.addAxiom(dModel.getClassAssertionAxiom(IRI.create(CAPECClass), capecIRI));
                  // <maIRI> refToCAPEC <capecIRI>
                  dModel.addAxiom(dModel.getObjectPropertyAssertionAxiom(IRI.create(refToCAPECProperty),maIRI,capecIRI));
               }
            }            
         }         
      }
            
      // killchain phases (references to tactics)
      List<ATTCKKillChainPhase> kcList = pattern.getKillChainPhases();
      if ( kcList !=null && kcList.size()!=0 ){
         for (int i=0;i<kcList.size();i++){
            ATTCKKillChainPhase kc = kcList.get(i);
            if (kc.getKill_chain_name().equals("mitre-attack")) {
               IRI tacticIRI = IRI.create(dModel.getDefaultPrefix()+"ATTCKTactic-"+kc.getPhase_name()); 
               // <tacticIRI> is an instance of ATTCKTactic
               dModel.addAxiom(dModel.getClassAssertionAxiom(IRI.create(ATTCKTacticClass), tacticIRI));
               // <patternIRI> refToTactic <tacticIRI>
               dModel.addAxiom(dModel.getObjectPropertyAssertionAxiom(IRI.create(refToTacticProperty),patternIRI,tacticIRI));
            }      
         }
      }

   }

   protected void applyRestrictions(List<String> permissions, String restriction, IRI patternIRI){
      if ( permissions !=null && permissions.size()!=0 ){         
         for (int i=0;i<permissions.size();i++){
            String permissionShort = "HasRestriction_"+restriction+"_"+O.safeIRI(permissions.get(i));
            IRI permissionIRI = IRI.create(dModel.getDefaultPrefix()+permissionShort);
            // HasRestriction_XXX_YYY is subclass of ThreatRestrictionClass  
            dModel.addAxiom(dModel.getSubClass(permissionIRI,IRI.create(ThreatRestrictionClass)));
            // {pattern} belongs to HasRestriction_XXX_YYY
            dModel.addAxiom(dModel.getClassAssertionAxiom(permissionIRI, patternIRI));

            String satisfiesShort = "Satisfies_"+permissionShort;
            IRI satisfiesIRI = IRI.create(dModel.getDefaultPrefix()+satisfiesShort);
            // Satisfies_HasRestriction_XXX_YYY is subclass of ClassifiedStencil
            dModel.addAxiom(dModel.getSubClass(satisfiesIRI,IRI.create(ClassifiedStencilClass)));
            // Satisfies_HasRestriction_XXX_YYY = Stensil AND HasRestriction_XXX_YYY
            dModel.addAxiom(dModel.getDefinedClassAnd(satisfiesIRI,IRI.create(StencilClass), permissionIRI));
            // Satisfies_HasRestriction_XXX_YYY satisfiesThreatRestriction {threat}
            dModel.addAxiom(dModel.getSubClassValue(satisfiesIRI, IRI.create(SatisfiesProperty), patternIRI));
            
         }
      }
   }

//////////////////////////////////////////////////////////////////////////////
// CAPEC

   public void processCAPECAttack (CAPECAttack attack){
      // CAPEC-XXX
      String shortName = "CAPEC-"+attack.ID;
      String attackURL = "https://capec.mitre.org/data/definitions/"+attack.ID+".html";
      IRI attackIRI = IRI.create(dModel.getDefaultPrefix()+shortName);

      // CAPEC-XXX is CAPEC
      dModel.addAxiom(dModel.getClassAssertionAxiom(IRI.create(CAPECClass), attackIRI));
      // annotation
      dModel.addAxiom(dModel.getIndividualAnnotation(attackIRI, shortName, "en"));            
      // comment
      dModel.addAxiom(dModel.getIndividualComment(attackIRI, shortName+" ("+attackURL+")", "en"));
      
      for (int ii=0; ii<attack.CWEs.size(); ii++){
         // CWE-XXX
         IRI cweIRI = IRI.create(dModel.getDefaultPrefix()+"CWE-"+attack.CWEs.get(ii));
         dModel.addAxiom(dModel.getClassAssertionAxiom(IRI.create(CWEClass), cweIRI));
         dModel.addAxiom(dModel.getObjectPropertyAssertionAxiom(IRI.create(refToCWEProperty),attackIRI,cweIRI));
      }

      for (int ii=0; ii<attack.ATTCKs.size(); ii++){
         String ma= "ATTCK-"+O.safeIRI(attack.ATTCKs.get(ii));
         IRI maIRI = IRI.create(dModel.getDefaultPrefix()+ma);
         // <CAPEC> isRefToATTCK <ATTCK>
         dModel.addAxiom(dModel.getObjectPropertyAssertionAxiom(IRI.create(isRefToATTCKProperty),attackIRI,maIRI));
      }      
      // find ATTCK references by their labels, their come from CAPEC as "TXXX"
      // it needs to map a label "TXXX" ...
      //     ... to name of threat (like #attack-pattern--a62a8db3-f23a-4d8f-afd6-9dbc77e7813b)
     /* List<OWLNamedIndividual> attcks = dModel.getSearcherInstances(IRI.create(ThreatClass)).sorted().collect(Collectors.toList());
      for (int ii=0; ii<attack.ATTCKs.size(); ii++){
         String attckLabel = attack.ATTCKs.get(ii);
         for (int iii=0; iii<attcks.size(); iii++){
             OWLNamedIndividual attck = attcks.get(iii);
             String tmpLabel = dModel.getSeacherLabel(attck);
             if (tmpLabel.equals(attckLabel)){
                // <CAPEC> isRefToATTCK ATTCK
                dModel.addAxiom(dModel.getObjectPropertyAssertionAxiom(IRI.create(isRefToATTCKProperty),attackIRI,attck.getIRI()));
                break;
             }   
         }
      }*/

      
   }
   
/////////////////////////////////////////////////////////////////////////////////////////////////
// CWE

   public void processCWEVulnerability (CWEVulnerability vuln){
      String shortName = "CWE-"+vuln.ID;
      String cweURL = "https://cwe.mitre.org/data/definitions/"+vuln.ID+".html";
      IRI cweIRI = IRI.create(dModel.getDefaultPrefix()+shortName);

      // CWE-XXX is CWE
      dModel.addAxiom(dModel.getClassAssertionAxiom(IRI.create(CWEClass), cweIRI));
      // annotation
      dModel.addAxiom(dModel.getIndividualAnnotation(cweIRI, shortName, "en"));            
      // comment
      dModel.addAxiom(dModel.getIndividualComment(cweIRI, shortName+" ("+cweURL+")", "en"));

      for (int ii=0; ii<vuln.CAPECs.size(); ii++){
         IRI capecIRI = IRI.create(dModel.getDefaultPrefix()+vuln.CAPECs.get(ii));
         dModel.addAxiom(dModel.getClassAssertionAxiom(IRI.create(CAPECClass), capecIRI));
         dModel.addAxiom(dModel.getObjectPropertyAssertionAxiom(IRI.create(isRefToCAPECProperty),cweIRI,capecIRI));
      }
      
      // https://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-2005-1971
      // https://www.cve.org/CVERecord?id=CVE-2005-1971
      // https://nvd.nist.gov/vuln/detail/CVE-2005-1971
      for (int ii=0; ii<vuln.CVEs.size(); ii++){
         String cveID = vuln.CVEs.get(ii);
         IRI cveIRI = IRI.create(dModel.getDefaultPrefix()+ cveID);
         dModel.addAxiom(dModel.getClassAssertionAxiom(IRI.create(CVEClass),cveIRI));
         dModel.addAxiom(dModel.getObjectPropertyAssertionAxiom(IRI.create(refToCVEProperty),cweIRI,cveIRI));
         // label
         dModel.addAxiom(dModel.getIndividualAnnotation(cveIRI, cveID, "en"));            
         // comment
         dModel.addAxiom(dModel.getIndividualComment(cveIRI, cveID+" (https://www.cve.org/CVERecord?id="+cveID+")", "en"));
      }


   }

////////////////////////////////////////////////////////////////////////////

   public boolean save(){
      return saveToFile(dstmModel,dstmModelFile);
   }

   public boolean saveTTL(){
      return saveToFileTTL(dstmModel, dstmModelFileTTL);
   }

   public boolean saveTTL1(){
      dModel.fill();
      return saveToFileTTL(dstmModel, dstmModelFileTTL1);
   }

   
}
