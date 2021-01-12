package ab.base;

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

// Test ontologies for different conditions
public class TManager extends OManager{
   private static final Logger LOGGER = Logger.getLogger(LManager.class.getName());
   private static String APPLIEDAXIOMS ="appliedAxioms";
   private static String EXPECTEDAXIOMS ="expectedAxioms";

   protected O bmodel;  // base model as a special representation of OWLOntology
   protected ArrayList<O> importModels;
   protected ArrayList<String> appliedAxioms;
   protected ArrayList<String> expectedAxioms;

   public boolean initModel(String baseModelPath){
      bmodel = O.create(loadFromFile(baseModelPath));
      if (bmodel == null){
          LOGGER.severe("Unable to read base model from "+baseModelPath);
          return false; 
      }
      System.out.println(">>> MODEL: "+baseModelPath);
      return true;     
   }
   
   // import ontologies
   public boolean initImports(String[] imports){
      importModels = new ArrayList<O>();
      
      for (int i=0;i<imports.length;i++){
         String path = imports[i];
         O tmp = O.create(loadFromFile(path));
         if (tmp == null){
            LOGGER.severe("Unable to read model from "+path);
            return false;
         }
         importModels.add(tmp);
         System.out.println(">>> IMPORT: "+path);   
      }
      
      return true;     
   }
   
   
      
   // load axioms from files
   public boolean initAxioms(String checkFolder){
      appliedAxioms = readFileToArrayList(checkFolder+APPLIEDAXIOMS);
      if (appliedAxioms == null){
          LOGGER.severe("Unable to read applied axioms");
          return false;          
      }
      System.out.println(">>> GOT "+appliedAxioms.size()+ " axioms to apply");
      
      LOGGER.info("got applied axioms "+checkFolder+APPLIEDAXIOMS);
      
      expectedAxioms = readFileToArrayList(checkFolder+EXPECTEDAXIOMS);
      if (expectedAxioms == null){
          LOGGER.severe("Unable to read expected axioms");
          return false;          
      }
      System.out.println(">>> GOT "+expectedAxioms.size()+ " axioms to check");
      LOGGER.info("got expected axioms "+checkFolder+EXPECTEDAXIOMS);
      
      return true;
   }
   
   
   public boolean process(String folder){
      System.out.println (">>> FOLDER: "+ folder);
      if (initAxioms(folder)==false){
         LOGGER.severe("unable to init test in " + folder);
         return false;
      }
      if (applyAxioms()==false){
         LOGGER.severe("could not add all the axioms.");
         return false;
      }
            
      //reason(bmodel.get());
      bmodel.flush();
      //saveToFile(bmodel.get(),"../../tmp.owl");
      
      if (checkAxioms()==false){
         LOGGER.severe("could not check all the axioms.");
         return false;
      }
      return true;
   }
   
   
   public String removePrefix(String in){
      if (in.startsWith("+") || in.startsWith("-")){
         return in.substring(1, in.length());
      }
      return in;
   }
   
   public boolean checkPresence(String in){
      if (in.startsWith("-")) return false;
      return true;
   }
   
   public boolean checkAxioms(){
      return checkAxioms(expectedAxioms, bmodel);
   }
   
   // takes ArrayList of the "[+|-]<axiom>" strings and the O instance
   // checks presence or absence of axiom
   public boolean checkAxioms(ArrayList<String> axioms, O model){
      if (axioms.isEmpty()){
         LOGGER.severe("nothing to do, no check axioms");
         return false;
      }
      int a=0;
      int b=0;
      int c=0;
      for (int i=0;i<axioms.size();i++) {
         a++;
         String src=axioms.get(i);
         boolean isPresent = checkPresence(src);
         OWLAxiom ax=model.simpleStringToAxiom(removePrefix(src));
         if (ax!=null){
            b++;
            boolean isAxiom = model.containsAxiom1(ax); 
            if ( (isPresent && isAxiom ) || ( (!isPresent) && (!isAxiom) ) ){
               //System.out.println(">>> AXIOM PASSED: " +src);
               c++;
            } else {
               System.out.println(">>> AXIOM FAILED: " +src);
            }
         } else {
            LOGGER.severe("got null from "+src);
         }
      }
      if (a!=c){
         System.out.println(">>> TEST FAILED: got - " + a + ", recognized "+ b + ", found - "+ c +" axioms");
         return false;
      } 
      System.out.println(">>> TEST PASSED: " + c + " axioms are correct");         
      return true; 
   }
   
   
   public void reason(){
      reason(bmodel.get());
   }
   
   
   // applier is taken from O
   public boolean applyAxioms(){
      return bmodel.applyAxiomsFromArrayList(appliedAxioms);
   }      
   
   // a simple check taken from O
   // list should contain only axioms without prefixes
   // returns true if all the axioms have been found
   public boolean expectAxioms(){
      return bmodel.testAxiomsFromArrayList(expectedAxioms);
   }

}
