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

// A special representation for an ontology
public class O {
   private static final Logger LOGGER = Logger.getLogger(LManager.class.getName());
   
   protected OWLOntology o;
   protected OWLDataFactory df;

   protected String defaultPrefix;

   protected O(OWLOntology _o){
      o = _o;
      df = o.getOWLOntologyManager().getOWLDataFactory();
      defaultPrefix = getIRI().toString()+"#";
      LOGGER.info("got ontology with prefix "+defaultPrefix);
   }
   
   // to create an object use the O.create(OWLOntology), returns null if ontology is null 
   public static O create(OWLOntology _o){
      if (_o==null) {
          LOGGER.severe("tryied to use an empty ontology");
          return null;
      }
      return new O(_o);      
   }
   
   // get the OWLOntology object
   public OWLOntology get(){
      return o;
   }
   
   public IRI parseIRI(String in){
      if (in.startsWith(":")){
         String name = in.substring(1, in.length());
         return IRI.create(defaultPrefix,name);
      }
      if (in.startsWith("<")){
         String name = in.substring(1, in.length());
         return IRI.create(name.substring(0, name.length() - 1));
      }
      return null;
   }
   
   
   // converts functional syntax string to axiom
   // like ObjectPropertyAssertion(:crosses :flow :line)
   public OWLAxiom simpleStringToAxiom(String in){
      String[] parts = in.split("\\(");
      if (parts.length ==2) {
          String type = parts[0];
          String arg = parts[1].substring(0, parts[1].length() - 1);
          String[] args = arg.split(" ");
          if (args.length !=0){
              if (type.equals("ClassAssertion")){
                  if (args.length ==2) return getClassAssertionAxiom (parseIRI(args[0]),parseIRI(args[1]));
              }
              
              if (type.equals("ObjectPropertyAssertion")){
                  if (args.length ==3) return getObjectPropertyAssertionAxiom(parseIRI(args[0]),parseIRI(args[1]),parseIRI(args[2]));
              }
              
          }
      }
      LOGGER.severe("failed to process " +in);
      return null;
   }
   
   // adds axioms represented as strings (functional syntax) in arraylist to current ontology
   public boolean applyAxiomsFromArrayList(ArrayList<String> axioms){
      if (axioms.isEmpty()){
         LOGGER.severe("no axioms in ArrayList");
         return false;
      }
      int a=0;
      int b=0;
      for (int i=0;i<axioms.size();i++) {
         a++;
         OWLAxiom ax=simpleStringToAxiom(axioms.get(i));
         if (ax!=null){
            b++;
            addAxiom(ax);
         }else {
            LOGGER.severe("got null from "+axioms.get(i));
         }
      }
      if (a!=b){
         LOGGER.severe("got " + a+ " axioms, added only "+ b +" axioms");
         return false;
      } else {
         LOGGER.info("processed " + b+ " axioms");         
      }
      return true; 
   }
   
   
   public IRI getIRI(){
      return o.getOntologyID().getOntologyIRI().get();
   }

   // create class assertion axiom, i.e. map an individual to a class
   public OWLAxiom getClassAssertionAxiom(IRI className, IRI individualName){
      if (className == null || individualName == null){
         LOGGER.severe("got null argument");
         return null;
      }
      OWLClass cls = df.getOWLClass(className);
      OWLNamedIndividual ind  = df.getOWLNamedIndividual(individualName);
      OWLClassAssertionAxiom ax = df.getOWLClassAssertionAxiom(cls, ind);
      return ax;
   }

   // create property assertion axiom, i.e. map two individuals with a property
   public OWLAxiom getObjectPropertyAssertionAxiom(IRI propName, IRI individual1Name, IRI individual2Name){
      if (propName == null || individual1Name == null || individual2Name == null){
         LOGGER.severe("got null argument");
         return null;
      }
      OWLNamedIndividual ind1  = df.getOWLNamedIndividual(individual1Name);
      OWLNamedIndividual ind2  = df.getOWLNamedIndividual(individual2Name);
      OWLObjectProperty prop = df.getOWLObjectProperty(propName); 
      OWLAxiom ax = df.getOWLObjectPropertyAssertionAxiom(prop, ind1, ind2); 
      return ax;  
   }   
   
   public void addAxiom(OWLAxiom ax){
      o.add(ax);
   }
   
   public boolean containsAxiom(OWLAxiom ax){
      return o.containsAxiom(ax);
   }
   
   // a simple test for an ontology
   // check presence of axioms given as ArrayList of string in functional syntax
   // return true if the ontology has all the axioms from the list
   public boolean testAxiomsFromArrayList(ArrayList<String> axioms){
      if (axioms.isEmpty()){
         LOGGER.severe(">>> EXCUTION FAILED: nothing to do, no axioms");
         return false;
      }
      int a=0;
      int b=0;
      int c=0;
      for (int i=0;i<axioms.size();i++) {
         a++;
         String src=axioms.get(i);
         OWLAxiom ax=simpleStringToAxiom(src);
         if (ax!=null){
            b++;
            if (containsAxiom(ax)){
               LOGGER.info(">>> passed - " +src);
               c++;
            } else {
               LOGGER.info(">>> failed - " +src);
            }
         } else {
            LOGGER.severe("got null from "+src);
         }
      }
      if (a!=c){
         LOGGER.info(">>> TEST FAILED: got - " + a + ", recognize "+ b + ", found - "+ c +" axioms");
         return false;
      } 
      LOGGER.info(">>> TEST PASSED: found " + c + " axioms");         
      return true; 
   }

   
}




