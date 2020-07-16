
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
import org.semanticweb.owlapi.util.mansyntax.*;
import org.semanticweb.owlapi.expression.*;
import java.io.*;
import java.util.*;
import java.util.stream.*;
import java.time.Duration;
import java.time.Instant;
import java.util.logging.*;

// a simple OWL Manager for anything you want
//  i.e. to create, load, reason, save, copy, remove ontologies
// it has a OWLOntologyManager instance inside
public class OManager{

   private static final Logger LOGGER = Logger.getLogger(LManager.class.getName());

   // the main thing here
   // ontologies here are reasoned together
   protected OWLOntologyManager man;
   
   public OManager(){
      man = OWLManager.createOWLOntologyManager();
   }

   // create ontology with given iri
   public OWLOntology create(String _iri){
      OWLOntology o;
      try {
         o = man.createOntology(IRI.create(_iri));
      } catch (Exception e){
         LOGGER.severe("unable to create ontology with " + _iri);         
         e.printStackTrace();
         return null;
      }
      return o;
   }

   // load ontology from file by file name
   public OWLOntology loadFromFile (String fileName){
      File file;
      try {
         file = new File(fileName);
      } catch (Exception e){
         LOGGER.severe("could not open file " +fileName);
         e.printStackTrace();
         return null;
      }
      return loadFromFile(file);
   }

   // load ontology from file by file descriptor
   public OWLOntology loadFromFile (File file){
      OWLOntology o;
      try {
         o = man.loadOntologyFromOntologyDocument(file);
      } catch (Exception e) {
         LOGGER.severe("failed to load" +file.getAbsolutePath());
         e.printStackTrace();
         return null;
      }
      return o;
   }

   // copy ontology to this manager
   public OWLOntology copyOntology(OWLOntology _o){
      OWLOntology o;
      try {
        o = man.copyOntology(_o,OntologyCopy.DEEP);
      } catch (Exception e) {
         LOGGER.severe("failed to copy ontology" +getIRI(_o));
         e.printStackTrace();
         return null;
      }
      return o;
   }

   // merge all the ontologies with new iri
   public OWLOntology merge(String iriName){
      OWLOntology merged;
      try {
         OWLOntologyMerger merger = new OWLOntologyMerger(man);
         merged = merger.createMergedOntology(man,IRI.create(iriName));
      } catch (OWLOntologyCreationException e) {
         LOGGER.severe("failed to merge ontologies");
         e.printStackTrace();
         return null;
      }
      return merged;
   }

   // !!! deprecated, use O
   // reasoner is here because it is 'manager-depended' feature:
   //     to reason an ontology it needs all the imported ontologies
   public void reason(OWLOntology o){
      OWLReasoner reasoner = new Reasoner.ReasonerFactory().createReasoner(o);
      String reasonerName = reasoner.getReasonerName()+" "+reasoner.getReasonerVersion().toString();
      LOGGER.info("using " +reasonerName);
      LOGGER.info("trying to process "+getIRI(o).toString());

      InferredOntologyGenerator generator = new InferredOntologyGenerator(reasoner);
      OWLDataFactory df = o.getOWLOntologyManager().getOWLDataFactory();

      long startTime = System.nanoTime();
      reasoner.flush();
      generator.fillOntology(df, o);
      long stopTime = System.nanoTime();
      LOGGER.info("done; time (ms): "+ getms(startTime,stopTime));
   }

   public IRI getIRI(OWLOntology _o){
      return _o.getOntologyID().getOntologyIRI().get();
   }


   public void showStat(OWLOntology o){
      LOGGER.info("-----------------------");

      int i1=0;
      Set<AxiomType<?>> s = AxiomType.TBoxAxiomTypes;
      for (AxiomType a : s){
         i1=i1+o.getAxiomCount(a);
      }
      LOGGER.info("showStat: tbox axioms "+ i1 );

      int i2=0;
      s = AxiomType.ABoxAxiomTypes;
      for (AxiomType a : s){
          i2=i2+ o.getAxiomCount(a);
      }
      LOGGER.info("showStat: abox axioms "+ i2 );

      int i3=0;
      s = AxiomType.RBoxAxiomTypes;
      for (AxiomType a : s){
         i3=i3+ o.getAxiomCount(a);
      }
      LOGGER.info("showStat: rbox axioms "+ i3 );

      int i = i1+i2+i3;
      // the differnce is annotations & declarations etc.
      LOGGER.info("showStat: abox+rbox+tbox "+ i);
      LOGGER.info("showStat: total axioms "+ o.getAxiomCount());
      LOGGER.info("-----------------------");

   }

   // save ontology as OWL file in functional syntax
   public boolean saveToFile(OWLOntology o, String filepath){
      try {
         File fileout = new File(filepath);
         long startTime = System.nanoTime();
         man.saveOntology(o, new FunctionalSyntaxDocumentFormat(), new FileOutputStream(fileout));
         long stopTime = System.nanoTime();
      } catch (Exception e){
         LOGGER.severe("failed to save "+ filepath);
         e.printStackTrace();
         return false;
      }
      return true;
   } 

  // save an ontology to a file in the Turtle format
   public boolean saveToFileTTL(OWLOntology o, String filepath){
      try {
         File fileout = new File(filepath);
         long startTime = System.nanoTime();
         man.saveOntology(o, new TurtleDocumentFormat(), new FileOutputStream(fileout));
         long stopTime = System.nanoTime();
      } catch (Exception e){
         e.printStackTrace();
         return false;
      }
      return true;
   } 


   protected String getms(long start, long stop){
      long diff = (stop-start)/1000000;
      return Long.toString(diff) + " ms";
   }

     // read lines from file and add to arraylist
     protected ArrayList<String> readFileToArrayList(String fileName){
      try {  
         Scanner sc = new Scanner(new File(fileName));
         ArrayList<String> lines = new ArrayList<String>();
         while (sc.hasNextLine()) {
            String tmp = sc.nextLine().trim();
            if (!tmp.startsWith("#") && !tmp.isEmpty()) lines.add(tmp);
         }
         sc.close();
         return lines;
      } catch (Exception e) {
         LOGGER.severe("failed to read "+ fileName);
         e.printStackTrace();
         return null;
      }      
   }

   // add import to ontology
   public void addImportDeclaration(OWLOntology o,String iriname){
      IRI tmpiri = IRI.create(iriname);
      OWLImportsDeclaration importDeclaration=man.getOWLDataFactory().getOWLImportsDeclaration(tmpiri);
      man.applyChange(new AddImport(o, importDeclaration));
   }
 

}


