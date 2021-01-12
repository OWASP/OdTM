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
import org.semanticweb.owlapi.rdf.rdfxml.renderer.OWLOntologyXMLNamespaceManager;
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

   protected OWLDocumentFormat format;
   protected OWLOntologyXMLNamespaceManager namespaces;


/////////////////////////////////////////////////////////////////////////////////
// constructors
////////////////////////////////////////////////////////////////////////////////

   protected O(OWLOntology _o){
      o = _o;
      df = o.getOWLOntologyManager().getOWLDataFactory();
      defaultPrefix = getIRI().toString()+"#";
      // init namespaces
      format = o.getFormat();
      //format = o.getOWLOntologyManager().getOntologyFormat(o);
	  namespaces = new OWLOntologyXMLNamespaceManager(o, format);

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
// http://owlcs.github.io/owlapi/apidocs_5/org/semanticweb/owlapi/model/OWLOntology.html
///////////////////////////////////////////////////////////////////////////////////////////   
 
   // get the OWLOntology object
   public OWLOntology get(){
      return o;
   }
 
    public IRI getIRI(){
      return o.getOntologyID().getOntologyIRI().get();
   }

   public String getDefaultPrefix(){
      return defaultPrefix;
   }

   public void addAxiom(OWLAxiom ax){
      o.add(ax);
   }
   
   public void removeAxiom(OWLAxiom ax){
      o.removeAxiom(ax);
   }
  
   // !!! don't use it
   // it seems to be legacy approach
   // what is a new one? EntitySearcher.containsAxiom
   public boolean containsAxiom(OWLAxiom ax){
      return o.containsAxiom(ax);
   }

   public boolean containsAxiom1(OWLAxiom ax){
      return reasoner.isEntailed(ax);
   }

   public boolean isReasonerConsistent(){
      return reasoner.isConsistent();
   }


   public Stream<OWLAxiom> getAxioms(boolean includeImports){
	   if (includeImports == false) return o.axioms(Imports.EXCLUDED);
	   return o.axioms(Imports.INCLUDED);
   }
      
   //http://owlcs.github.io/owlapi/apidocs_5/org/semanticweb/owlapi/model/AxiomType.html
   public Stream<OWLAxiom> getDeclarationAxioms(boolean includeImports){
      Stream<OWLAxiom> stream = getAxioms(includeImports);
      return stream.filter(x-> x.getAxiomType().equals(AxiomType.DECLARATION));
   }

   public Stream<OWLAxiom> getDataPropertyDeclarationAxioms(boolean includeImports){
      Stream<OWLAxiom> stream = getDeclarationAxioms(includeImports);
      return stream.filter(x-> x.toString().startsWith("Declaration(DataProperty(") );
   }

   public OWLEntity getEntityFromDeclaration(OWLAxiom axiom){
	   return ((OWLDeclarationAxiom)axiom).getEntity();
   }


/////////////////////////////////////////////////////////////////////////////
// a simple functional parser
///////////////////////////////////////////////////////////////////////////// 

   // returns true if object belongs to this namespace
   public boolean hasDefaultPrefix(HasIRI in){
      if (in.getIRI().toString().startsWith(defaultPrefix)) return true;
      return false;
   }
   public boolean hasDefaultPrefix(IRI in){
      if (in.toString().startsWith(defaultPrefix)) return true;
      return false;
   }

   public static String getShortIRI(HasIRI src){
     return src.getIRI().getShortForm();
   }

   public static String getShortIRI(IRI src){
     return src.getShortForm();
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

   public static ArrayList<OWLNamedIndividual> individualsToList(Stream<OWLNamedIndividual> stream){
      ArrayList<OWLNamedIndividual> out = new ArrayList<OWLNamedIndividual>();
      for (Iterator<OWLNamedIndividual> iterator = stream.iterator(); iterator.hasNext(); ){
          OWLNamedIndividual item = (OWLNamedIndividual)iterator.next();
          out.add(item); 
      }
      return out;
   }

   public static String safeIRI(String in){
      String in1 = in;
      in1 = in1.replace(" ","_");
      in1 = in1.replace(":","_");
      in1 = in1.replace("(","_");
      in1 = in1.replace(")","_");
      in1 = in1.replace("/","_");
      in1 = in1.replace("\\","_");
      in1 = in1.replace("^","_");
      in1 = in1.replace("+","plus");
      in1 = in1.replace("#","sharp");
      in1 = in1.replace("$","dollar");
      in1 = in1.replace("@","at");
      in1 = in1.replace(".","dot");
      in1 = in1.replace("-","dash");
      in1 = in1.replace("'","_");
      in1 = in1.replace("&","and");
      in1 = in1.replace("!","_");
      in1 = in1.replace("?","_");
      in1 = in1.replace(",","_");
      in1 = in1.replace(">","_");
      in1 = in1.replace("<","_");
      in1 = in1.replace("\"","_");
      in1 = in1.replace("*","star");
      in1 = in1.replace("%","_");
      in1 = in1.replace("[","_");
      in1 = in1.replace("]","_");
      in1 = in1.replace("|","_");
      if (Character.isDigit(in1.charAt(0))) in1 = "x" + in1;
      return in1;
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

   public OWLAxiom getIndividualAnnotation(IRI indIRI, String label, String lang){
      OWLAnnotation labelAnno = df.getOWLAnnotation(df.getRDFSLabel(), df.getOWLLiteral(label, lang));
      OWLAxiom ax1 = df.getOWLAnnotationAssertionAxiom(indIRI, labelAnno);
      return ax1;
   }

   public OWLAxiom getIndividualComment(IRI indIRI, String label, String lang){
      OWLAnnotation commentAnno = df.getOWLAnnotation(df.getRDFSComment(), df.getOWLLiteral(label, lang));
      OWLAxiom ax1 = df.getOWLAnnotationAssertionAxiom(indIRI, commentAnno);
      return ax1;
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

   // create data property assertion axiom
   public OWLAxiom getIndividualDataProperty(IRI indName, IRI propertyName, String value){
      OWLNamedIndividual ind = df.getOWLNamedIndividual(indName);
      OWLDataProperty property = df.getOWLDataProperty(propertyName);      
      return getIndividualDataProperty(ind,property,value);
   }
   public OWLAxiom getIndividualDataProperty(OWLNamedIndividual ind, OWLDataProperty property, String value){
      OWLDataPropertyAssertionAxiom ax = df.getOWLDataPropertyAssertionAxiom(property,ind,df.getOWLLiteral(value, "en"));
      return ax;
   }

   // create defined class axiom like <cls> equivalents to <prop> value <val>
   // see http://owlcs.github.io/owlapi/apidocs_5/org/semanticweb/owlapi/model/axiomproviders/EquivalentAxiomProvider.html
   public OWLAxiom getDefinedClassValue (OWLClass cls, OWLObjectProperty prop, OWLNamedIndividual val){
      Set<OWLClassExpression> arguments=new HashSet<OWLClassExpression>();
      arguments.add (cls);
      arguments.add(df.getOWLObjectHasValue(prop, val));
      return df.getOWLEquivalentClassesAxiom(arguments);
   }
   public OWLAxiom getDefinedClassValue(IRI clsName,IRI propName, IRI valName){
      return getDefinedClassValue(df.getOWLClass(clsName),df.getOWLObjectProperty(propName),df.getOWLNamedIndividual(valName));
   }  
 
   // create defined class axiom like <cls> equivalents to <prop> some <val>
   // see http://owlcs.github.io/owlapi/apidocs_5/org/semanticweb/owlapi/model/axiomproviders/EquivalentAxiomProvider.html
   public OWLAxiom getDefinedClassSome (OWLClass cls, OWLObjectProperty prop, OWLClass val){
      Set<OWLClassExpression> arguments=new HashSet<OWLClassExpression>();
      arguments.add (cls);
      arguments.add(df.getOWLObjectSomeValuesFrom(prop, val));
      return df.getOWLEquivalentClassesAxiom(arguments);
   }
   public OWLAxiom getDefinedClassSome(IRI clsName,IRI propName, IRI valName){
      return getDefinedClassSome(df.getOWLClass(clsName),df.getOWLObjectProperty(propName),df.getOWLClass(valName));
   }  
 
   // create defined class axiom like <cls> equivalents to <class> and <class>
   // see http://owlcs.github.io/owlapi/apidocs_5/org/semanticweb/owlapi/model/axiomproviders/EquivalentAxiomProvider.html
   public OWLAxiom getDefinedClassAnd (OWLClass cls, OWLClass cls1, OWLClass cls2){
      Set<OWLClassExpression> arguments=new HashSet<OWLClassExpression>();
      arguments.add (cls);
      arguments.add( df.getOWLObjectIntersectionOf(cls1, cls2));
      return df.getOWLEquivalentClassesAxiom(arguments);
   }
   public OWLAxiom getDefinedClassAnd(IRI clsName,IRI cls1Name, IRI cls2Name){
      return getDefinedClassAnd(df.getOWLClass(clsName),df.getOWLClass(cls1Name),df.getOWLClass(cls2Name));
   }  

   // create sub class axiom like "<cls> subclass of <prop> value <val>"
   public OWLAxiom getSubClassValue (OWLClass cls, OWLObjectProperty prop, OWLNamedIndividual val){
      return df.getOWLSubClassOfAxiom(cls, df.getOWLObjectHasValue(prop, val)); 
   }
   public OWLAxiom getSubClassValue(IRI clsName,IRI propName, IRI valName){
      return getSubClassValue(df.getOWLClass(clsName),df.getOWLObjectProperty(propName),df.getOWLNamedIndividual(valName));
   }  
 
   public OWLAxiom getSubClass(OWLClass cls, OWLClass parent){
      return df.getOWLSubClassOfAxiom(cls, parent);
   }
   public OWLAxiom getSubClass(IRI cls, IRI parent){
      return df.getOWLSubClassOfAxiom(df.getOWLClass(cls), df.getOWLClass(parent));
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

   // assumes that instance has only one property
   // for several it returns the first one
   public String getSearcherDataPropertyValue(OWLNamedIndividual ind, OWLDataProperty property){
      Iterator<OWLLiteral> iterator = EntitySearcher.getDataPropertyValues(ind, property, o).iterator();
      if (iterator.hasNext()) return ((OWLLiteral)iterator.next()).getLiteral();         
      return null;      
   }   
   public String getSearcherDataPropertyValue(IRI instanceName, IRI propertyName){
      return getSearcherDataPropertyValue(df.getOWLNamedIndividual(instanceName), df.getOWLDataProperty(propertyName));
   }   

  public String getSeacherLabel(OWLEntity e) {
     Stream<OWLAnnotation> labels = EntitySearcher.getAnnotations(e, o);
      
     for (Iterator<OWLAnnotation> iterator = labels.iterator(); iterator.hasNext(); ){
        OWLAnnotation an = iterator.next();
        if (an.getProperty().isLabel()) {
           OWLAnnotationValue val = an.getValue();
           if (val instanceof IRI) {
               return ((IRI) val).toString();
           } else if (val instanceof OWLLiteral) {
                OWLLiteral lit = (OWLLiteral) val;
                return lit.getLiteral();
           } else if (val instanceof OWLAnonymousIndividual) {
                OWLAnonymousIndividual ind = (OWLAnonymousIndividual) val;
                return ind.toStringID();
           }
        }
     }
     //return e.toString();
     return null;
  }

  public String getSeacherComment(OWLEntity e) {
     Stream<OWLAnnotation> labels = EntitySearcher.getAnnotations(e, o);

     for (Iterator<OWLAnnotation> iterator = labels.iterator(); iterator.hasNext(); ){
        OWLAnnotation an = iterator.next();
        if (an.getProperty().isComment()) {
           OWLAnnotationValue val = an.getValue();
           if (val instanceof IRI) {
               return ((IRI) val).toString();
           } else if (val instanceof OWLLiteral) {
                OWLLiteral lit = (OWLLiteral) val;
                return lit.getLiteral();
           } else if (val instanceof OWLAnonymousIndividual) {
                OWLAnonymousIndividual ind = (OWLAnonymousIndividual) val;
                return ind.toStringID();
           }
        }
     }
     //return e.toString();
     return null;
  }


  public Stream<OWLPropertyExpression> getSearcherSubProperties(String propertyName){
	 OWLObjectProperty e = df.getOWLObjectProperty(IRI.create(propertyName)); 
     return EntitySearcher.getSubProperties(e, o);     
  } 

  public Stream<OWLClassExpression> getSearcherObjectPropertyRanges(OWLObjectProperty prop){
	  return EntitySearcher.getRanges((OWLObjectPropertyExpression)prop, o);
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
     
   public OWLClass getReasonerDirectType(OWLNamedIndividual instance){
      Iterator<OWLClass> iterator = getReasonerDirectTypes(instance).iterator();
      if (iterator.hasNext()) return (OWLClass)iterator.next(); 
      LOGGER.severe("instance does not have type - " + instance.getIRI().toString());
      return null;
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
      LOGGER.severe("instance does not have this property - " + instance.getIRI().toString() + "  "+ property.getIRI().toString());
      return null;
   }
   public OWLNamedIndividual getObjectPropertyValue(IRI instanceName,IRI propertyName){
      return getObjectPropertyValue(df.getOWLNamedIndividual(instanceName),df.getOWLObjectProperty(propertyName));
   }   
   

   public OWLNamedIndividual getObjectPropertyValueFromOntology(OWLNamedIndividual instance,OWLObjectProperty property, IRI iri){
      for (Iterator<OWLNamedIndividual> iterator = getReasonerObjectPropertyValues(instance,property).iterator(); iterator.hasNext(); ){
          OWLNamedIndividual in = (OWLNamedIndividual)iterator.next();
          if (in.getIRI().toString().startsWith(iri.toString())) return in;
      }
      LOGGER.severe("instance does not have this property - " + instance.getIRI().toString() + "  "+ property.getIRI().toString());
      return null;
   }

   public OWLNamedIndividual getObjectPropertyValueFromOntology(IRI instanceIRI, IRI propertyIRI, IRI iri){
      return getObjectPropertyValueFromOntology(df.getOWLNamedIndividual(instanceIRI),df.getOWLObjectProperty(propertyIRI),iri);
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

   
   public Stream<OWLClass> getReasonerObjectPropertyRanges(OWLObjectProperty pe){
	  return reasoner.objectPropertyRanges(pe, false);   
   }

////////////////////////////////////////////////////////////////////////
// namespaces (i.e. prefixies)
// http://owlcs.github.io/owlapi/apidocs_5/org/semanticweb/owlapi/rdf/rdfxml/renderer/OWLOntologyXMLNamespaceManager.html
// to init:
// OWLDocumentFormat format = o.getFormat();
// // OWLDocumentFormat format = o.getOWLOntologyManager().getOntologyFormat(o); 
// OWLOntologyXMLNamespaceManager namespaces = new OWLOntologyXMLNamespaceManager(o, format);
// 
//  they call prefix thing like this "ns" 
//  and namespace is "http://www.grsu.by/net/SecurityPatternCatalogNaiveSchema#"
//  but here prefix is "http://www.grsu.by/net/SecurityPatternCatalogNaiveSchema#" 
//  and namespace is "ns"
// 
////////////////////////////////////////////////////////////////////////

   public ArrayList<String> getNamespaces(){
	   ArrayList<String> lst = new ArrayList<String>();
       for (String ns : namespaces.getNamespaces()) lst.add(ns);   
	   return lst;
   }

   public void showNamespaces(){	   
	  for (String prefix : namespaces.getPrefixes()) {
          System.out.println(prefix);
      }  
      for (String ns : namespaces.getNamespaces()) {
          System.out.println(ns);
      }
   }
		   
   public String getNamespaceForPrefix(String prefix){
	   // return namespaces.getNamespaceForPrefix(prefix);
	   return namespaces.getPrefixForNamespace(prefix);
   }
	  	
   public String getPrefixForNamespace(String namespace){
	   // return namespaces.getPrefixForNamespace(namespace);
	   return namespaces.getNamespaceForPrefix(namespace);
   }   

   public String getShortFromIRI(IRI iri){
	   return iri.getShortForm();
   }

   public String getPrefixFromIRI(IRI iri){
	   return iri.getNamespace();
   }

   public String getNamespaceFromIRI(IRI iri){
	   if (iri == null){
	      LOGGER.severe("null IRI");
	      return null;
	   }
	   String prefix = getPrefixFromIRI(iri);
	   String namespace = getNamespaceForPrefix(prefix);
	   if (namespace == null){
	      LOGGER.severe("namespace is not found for prefix "+prefix);
	      return null;		   
	   }
	   return namespace;
   }

   public String getShortLikeForm(OWLEntity ent){
	   IRI iri = ent.getIRI();
	   String prefix = getPrefixFromIRI(iri);
       String shortForm = getShortFromIRI(iri);
       String namespace = getNamespaceForPrefix(prefix);
       if (namespace == null) {
	      LOGGER.severe("namespace is not found for prefix "+prefix);
	      return null;		   
	   }
       return namespace+":"+shortForm; 
   }

   public IRI getIRIForm(String in){
	   String[] parts = in.split(":");
	   if (parts.length == 1) return null;
	   String prefix = getPrefixForNamespace(parts[0]);
	   String sn = parts[1];
       return IRI.create(prefix+sn);
   }


   public void addImportDeclaration(String iriname){
      IRI tmpiri = IRI.create(iriname);
      OWLOntologyManager man = o.getOWLOntologyManager();
      OWLImportsDeclaration importDeclaration=man.getOWLDataFactory().getOWLImportsDeclaration(tmpiri);
      man.applyChange(new AddImport(o, importDeclaration));
   }


   // don't work :(((
   public boolean copyNamespaces(O source){
	  ArrayList<String> srcList = source.getNamespaces();
	  ArrayList<String> dstList = getNamespaces();
	  
	  for (String prefix : srcList){
		  if (!dstList.contains(prefix)){
			  String ns = source.getNamespaceForPrefix(prefix);
			  if (ns != null) {		
				 addImportDeclaration(prefix.substring(0, prefix.length()-1) );   
				 namespaces.setPrefix(ns,prefix);
			  }
			  System.out.println(ns+ " ::: "+prefix+ " ::: "+IRI.create(prefix).getNamespace());
		  }
	  } 
	  return true;   
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

   public void showAxioms(Stream<OWLAxiom> lst){
       for (Iterator<OWLAxiom> iterator = lst.iterator(); iterator.hasNext(); ){
          OWLAxiom in = (OWLAxiom)iterator.next();
          System.out.println(in.getAxiomType()+"... "+in.toString());
          //System.out.println(getEntityFromDeclaration((OWLDeclarationAxiom)in));
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
