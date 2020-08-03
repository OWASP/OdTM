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

// a special representation for a single ontology
// for different manipulations inside the ontology
// it has an OWLOntology object inside
public class O {
   private static final Logger LOGGER = Logger.getLogger(LManager.class.getName());
   
   protected OWLOntology o; // this one
   protected String defaultPrefix;
   
   protected OWLDataFactory df;

   protected OWLReasoner reasoner;
   protected InferredOntologyGenerator generator;
   protected String reasonerName;

/////////////////////////////////////////////////////////////////////////////////
// constructors
////////////////////////////////////////////////////////////////////////////////

   protected O(OWLOntology _o){
      o = _o;
      df = o.getOWLOntologyManager().getOWLDataFactory();
      defaultPrefix = getIRI().toString()+"#";
   }
   
   // to create an object use the O.create(OWLOntology), returns null if ontology is null    
   public static O create (OWLOntology _o){
      if (_o==null) {
          LOGGER.severe("got the empty ontology");
          return null;
      }
      O tmp = new O(_o);
      // inits Hermits
      tmp.initReasoner();
      return tmp;
   }
  
////////////////////////////////////////////////////////////////////////////////////////////
// common functions
///////////////////////////////////////////////////////////////////////////////////////////   
 
   // get the OWLOntology object
   public OWLOntology get(){
      return o;
   }
 
    public IRI getIRI(){
      return o.getOntologyID().getOntologyIRI().get();
   }


   public void addAxiom(OWLAxiom ax){
      o.add(ax);
   }
   
   public void removeAxiom(OWLAxiom ax){
      o.removeAxiom(ax);
   }
  
   // it seems to be legacy approach
   // what is a new one? EntitySearcher.containsAxiom
   public boolean containsAxiom(OWLAxiom ax){
      return o.containsAxiom(ax);
   }
 
/////////////////////////////////////////////////////////////////////////////
// a simple functional parser
///////////////////////////////////////////////////////////////////////////// 

   // returns true if an object belongs to this namespace
   public boolean hasDefaultPrefix(HasIRI in){
      if (in.getIRI().toString().startsWith(defaultPrefix)) return true;
      return false;
   }
   public boolean hasDefaultPrefix(IRI in){
      if (in.toString().startsWith(defaultPrefix)) return true;
      return false;
   }
      
   // takes strings like ":user" or "<http://www.grsu.by/net/OdTMBaseThreatModel#agrees>"
   // and returns the IRI object
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
   
   // hack that enables cutting things (only classes <...> & like this:)
   // 'ObjectSomeValuesFrom(<http://www.grsu.by/net/OdTMBaseThreatModel#suggests> <http://www.grsu.by/net/OdTMBaseThreatModel#HTTPServerComponent>)'
   //    ^^^ MyAxiom.type       ^^^MyAxiom.args[0]                                 ^^^MyAxiom.args[1]
   // similar to simpleStringToAxiom, but takes output of API's streams (EntitySearcher), not functional syntax expressions
   public MyAxiom parseClassExpression(String in){
      // classes are like '<...>'
      if (in.startsWith("<")){
         String[] args = new String[1];
         args[0] = in;
         return new MyAxiom("Class", args);
      }
      
      String[] parts = in.split("\\(");
      if (parts.length ==2) {
          String type = parts[0];
          String arg = parts[1].substring(0, parts[1].length() - 1);
          String[] args = arg.split(" ");
          if (args.length !=0){
             // only two arguments and both are IRIs
             args[0] = parseIRI(args[0]).toString();
             args[1] = parseIRI(args[1]).toString();
             return new MyAxiom(type,args);
          }
      }
      LOGGER.severe("failed to process " +in);
      return null;
   }

   // We have stream of lines (lst)
   // 'ObjectSomeValuesFrom(<http://www.grsu.by/net/OdTMBaseThreatModel#suggests> <http://www.grsu.by/net/OdTMBaseThreatModel#HTTPServerComponent>)'
   //        ^^^ type                          ^^^ propName                                  ^^^ 
   //                                                                          it returns the last item as IRI
   // !!! it takes only first line with this property
   public IRI searchForExpressionValue(Stream lst, String type, IRI propName){
       for (Iterator<OWLClassExpression> iterator = lst.iterator(); iterator.hasNext(); ){
           OWLClassExpression in = (OWLClassExpression)iterator.next();           
           MyAxiom a = parseClassExpression(in.toString());
           if ( (a !=null) && a.type.equals(type)){              
              if (a.args[0].equals(propName.toString())) return IRI.create(a.args[1]);
           }
       }      
       return null;
   }

   // gets a simple class definition
   // i.e. tokens from first item in stream, which is like ... equivalent <property> some <class>
   // ObjectSomeValuesFrom(<http://www.grsu.by/net/OdTMBaseThreatModel#isTargetOf> <http://www.grsu.by/net/OdTMBaseThreatModel#AgreesHTTPProtocol>)
   //       ^^^ MyAxiom.type         ^^^MyAxiom.args[0] (property)                       ^^^MyAxiom.args[1] (class)
   public MyAxiom searchForSimpleClassDefinition(IRI className){
       Stream lst=getSearcherEquivalentClasses(className);
       for (Iterator<OWLClassExpression> iterator = lst.iterator(); iterator.hasNext(); ){
           OWLClassExpression in = (OWLClassExpression)iterator.next();           
           MyAxiom a = parseClassExpression(in.toString());
           if ( (a !=null) && a.type.equals("ObjectSomeValuesFrom")){
              return a;              
           }
       }      
       return null;
   }


   // converts functional syntax string to axiom
   // like "ObjectPropertyAssertion(:crosses :flow :line)"
   //   or "ClassAssertion(<http://www.grsu.by/net/OdTMBaseThreatModel#NetworkFlow> :flow)"
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
      } 
      return true; 
   }

    public static String instancesToString(Stream<OWLNamedIndividual> lst){
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

    public static String classesToString(Stream<OWLClass> lst){
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

//////////////////////////////////////////////////////////////////////////      
// reasoner
//////////////////////////////////////////////////////////////////////////
      
   public void initReasoner(){
      reasoner = new Reasoner.ReasonerFactory().createReasoner(o);
      reasonerName = reasoner.getReasonerName()+" "+reasoner.getReasonerVersion().toString();
      generator = new InferredOntologyGenerator(reasoner);
      //LOGGER.info("got " +reasonerName);
   }
   
   public void fill(){
      reasoner.flush();
      generator.fillOntology(df, o);
      LOGGER.info("fill "+getIRI().toString()); 
   }
   
   public void flush(){
      reasoner.flush();
      LOGGER.info("flushed "+getIRI().toString()); 
   }
   
   
///////////////////////////////////////////////////////////////////////////////////
// create different axioms with the OWLDataFactory object 
// http://owlcs.github.io/owlapi/apidocs_5/org/semanticweb/owlapi/model/OWLDataFactory.html
//////////////////////////////////////////////////////////////////////////////////

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

//////////////////////////////////////////////////////////////////////////////////////
// results from EntitySearcher (aka non reasoned results)
// http://owlcs.github.io/owlapi/apidocs_5/org/semanticweb/owlapi/search/EntitySearcher.html
//////////////////////////////////////////////////////////////////////////////////////

   // get superclass expressions for a given class
   public Stream<OWLClassExpression> getSearcherSuperClasses(OWLClass cls){
      return EntitySearcher.getSuperClasses(cls,o);
   }
   public Stream<OWLClassExpression> getSearcherSuperClasses(IRI className){
      return getSearcherSuperClasses(df.getOWLClass(className));
   }

   // for a given class returns equivalent class expressions
   // e.g. 'ObjectSomeValuesFrom(<http://www.grsu.by/net/OdTMBaseThreatModel#isTargetOf> ObjectSomeValuesFrom(<http://www.grsu.by/net/OdTMBaseThreatModel#agrees> <http://www.grsu.by/net/OdTMBaseThreatModel#HTTPProtocol>))'
   public Stream<OWLClassExpression> getSearcherEquivalentClasses(OWLClass cls){
      return EntitySearcher.getEquivalentClasses(cls,o);
   }
   public Stream<OWLClassExpression> getSearcherEquivalentClasses(IRI className){
      return getSearcherEquivalentClasses(df.getOWLClass(className));
   }

   // get types for an individual
   // returns only direct types
   public Stream<OWLClass> getSearcherTypes(OWLNamedIndividual individual){
      return EntitySearcher.getTypes(individual,o).map(x -> x.asOWLClass());
   }
   public Stream<OWLClass> getSearcherTypes(IRI individualName){
      return getSearcherTypes(df.getOWLNamedIndividual(individualName));
   }

   // get instances for a class
   // returns only direct instances
   public Stream<OWLNamedIndividual> getSearcherInstances(OWLClass cls){
      return EntitySearcher.getInstances(cls,o).map(x -> x.asOWLNamedIndividual());
   }
   public Stream<OWLNamedIndividual> getSearcherInstances(IRI className){
      return getSearcherInstances(df.getOWLClass(className));
      //return EntitySearcher.getInstances(df.getOWLClass(className),o).map(x -> x.asOWLNamedIndividual());
   }
   

   // assumes that instance belongs to only one class
   // this route returns that class
   public OWLClass getPrimaryType(OWLNamedIndividual instance){
      Iterator<OWLClass> iterator = getSearcherTypes(instance).iterator();
      if (iterator.hasNext()) return (OWLClass)iterator.next(); 
      LOGGER.severe("instance does not have type - " + instance.getIRI().toString());
      return null;
   }
   public OWLClass getPrimaryType(IRI instanceName){
      return getPrimaryType(df.getOWLNamedIndividual(instanceName));
   }


//////////////////////////////////////////////////////////////////////////////////////
// find different axioms with the OWLReasoner object (aka reasoned results)
// https://owlcs.github.io/owlapi/apidocs_5/org/semanticweb/owlapi/reasoner/OWLReasoner.html
/////////////////////////////////////////////////////////////////////////////////////

   // get subclasses of given class
   // also returns owl:Nothing
   public Stream<OWLClass> getReasonerSubclasses(OWLClass cls){
      return reasoner.subClasses(cls);
   }
   public Stream<OWLClass> getReasonerSubclasses(IRI className){
      return reasoner.subClasses(df.getOWLClass(className));
   }

   // get direct subclasses of given class
   public Stream<OWLClass> getReasonerDirectSubclasses(OWLClass cls){
      return reasoner.subClasses(cls,true);
   }
   public Stream<OWLClass> getReasonerDirectSubclasses(IRI className){
      return reasoner.subClasses(df.getOWLClass(className),true);
   }   
      
   // get all types for an individual
   public Stream<OWLClass> getReasonerTypes(OWLNamedIndividual individual){
      return reasoner.types(individual,false);
      // OWLReasoner.getTypes that returns org.semanticweb.owlapi.reasoner.NodeSet has been depricated, 
      // despite there was no a such individual, it would return <http://www.w3.org/2002/07/owl#Thing>
      //    it returns owl:Thing in any case, filter it while usage
   }
   public Stream<OWLClass> getReasonerTypes(IRI individualName){
      return getReasonerTypes(df.getOWLNamedIndividual(individualName));
      //return reasoner.types(df.getOWLNamedIndividual(individualName),false);
   }
   
   // get direct type of an individual
   public Stream<OWLClass> getReasonerDirectTypes(OWLNamedIndividual individual){
      return reasoner.types(individual,true);
   } 
   public Stream<OWLClass> getReasonerDirectTypes(IRI individualName){
      return getReasonerDirectTypes(df.getOWLNamedIndividual(individualName));
      // return reasoner.types(df.getOWLNamedIndividual(individualName),true);
   } 
     

   // get all instances of given class
   public Stream<OWLNamedIndividual> getReasonerInstances(OWLClass cls){
      return reasoner.instances(cls,false);
   }
   public Stream<OWLNamedIndividual> getReasonerInstances(IRI className){
      return getReasonerInstances(df.getOWLClass(className));
      // return reasoner.instances(df.getOWLClass(className),false);
   }
   
   // get all the instances
   public Stream<OWLNamedIndividual> getReasonerAllInstances(){
      return getReasonerInstances(IRI.create("http://www.w3.org/2002/07/owl#Thing"));
   } 

   // get the instances with default prefix (i.e. local instances)
   public Stream<OWLNamedIndividual> getReasonerHasDefaultPrefixInstances(){
      Stream<OWLNamedIndividual> stream = getReasonerAllInstances();
      return stream.filter(x-> hasDefaultPrefix(x));
   }
   
   // get values of a given property for a given instance
   public Stream<OWLNamedIndividual> getReasonerObjectPropertyValues(OWLNamedIndividual instance,OWLObjectProperty property){
      return reasoner.objectPropertyValues(instance,property);
   } 
   public Stream<OWLNamedIndividual> getReasonerObjectPropertyValues(IRI instanceName,IRI propertyName){
      return reasoner.objectPropertyValues(df.getOWLNamedIndividual(instanceName),df.getOWLObjectProperty(propertyName));
   } 

   // assumes that instance belongs to only one class
   public OWLNamedIndividual getObjectPropertyValue(OWLNamedIndividual instance,OWLObjectProperty property){
      Iterator<OWLNamedIndividual> iterator = getReasonerObjectPropertyValues(instance,property).iterator();
      if (iterator.hasNext()) return (OWLNamedIndividual)iterator.next(); 
      LOGGER.severe("instance does not have this propery - " + instance.getIRI().toString() + "  "+ property.getIRI().toString());
      return null;
   }
   public OWLNamedIndividual getObjectPropertyValue(IRI instanceName,IRI propertyName){
      return getObjectPropertyValue(df.getOWLNamedIndividual(instanceName),df.getOWLObjectProperty(propertyName));
   }   
   
   
   public boolean isReasonerIndividualBelongsToClass(OWLNamedIndividual instance, OWLClass cls){
      for (Iterator<OWLClass> iterator = getReasonerTypes(instance).iterator(); iterator.hasNext(); ){
          OWLClass in = (OWLClass)iterator.next();
          if (in.equals(cls)) return true;           
      }
      return false;
   }
   public boolean isReasonerIndividualBelongsToClass(IRI instanceName, IRI className){
       return isReasonerIndividualBelongsToClass(df.getOWLNamedIndividual(instanceName),df.getOWLClass(className));
   }

/////////////////////////////////////////////////////////////////////////////////////
// temporary & debug functions
////////////////////////////////////////////////////////////////////////////////////
   
   public void showClasses(Stream<OWLClass> lst){
       for (Iterator<OWLClass> iterator = lst.iterator(); iterator.hasNext(); ){
           OWLClass in = (OWLClass)iterator.next();
           System.out.println("... "+in.getIRI().toString());
       }
   }

   public void showClassExpressions(Stream<OWLClassExpression> lst){
       for (Iterator<OWLClassExpression> iterator = lst.iterator(); iterator.hasNext(); ){
           OWLClassExpression in = (OWLClassExpression)iterator.next();
           System.out.println("... ["+ in.getClassExpressionType().toString() +"]: "+in.toString());
       }
   }


   public void showInstances(Stream<OWLNamedIndividual> lst){
       for (Iterator<OWLNamedIndividual> iterator = lst.iterator(); iterator.hasNext(); ){
          OWLNamedIndividual in = (OWLNamedIndividual)iterator.next();
          System.out.println("... "+in.getIRI().toString());
       }
   }

   // a simple test for an ontology (after the  InferredOntologyGenerator.fillOntology )
   // check presence of axioms given as ArrayList of string in functional syntax
   // return true if the ontology has all the axioms from the list
   // gives a lot of console output
   public boolean testAxiomsFromArrayList(ArrayList<String> axioms){
      if (axioms.isEmpty()){
         LOGGER.severe(">>> EXECUTION FAILED: nothing to do, no axioms");
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
