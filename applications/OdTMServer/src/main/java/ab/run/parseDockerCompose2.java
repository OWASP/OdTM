
package ab.run;

import ab.base.*;
import ab.dc.*;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.*;
import org.semanticweb.owlapi.apibinding.*;
import org.semanticweb.owlapi.util.*;
import org.semanticweb.owlapi.formats.*;
import java.io.*;
import java.util.logging.*;
import java.util.Arrays;

class parseDockerCompose2 {
   private final static Logger LOGGER = Logger.getLogger(LManager.class.getName());

   public static void main(String args[]){
 
     if (args.length != 0) {
	     LManager.init();

        LOGGER.info("starting ...");
        PManager conf = new PManager();

        if (conf.init(args[0])) {
           
          // getting the service model
          LOGGER.info("trying to parse the service model");
          String serviceModelFile = conf.get("SERVICE_MODEL");
          if (serviceModelFile == null) {
             LOGGER.severe("apply the SERVICE_MODEL option to the properties");
             System.exit(1);
          }
          ServiceModelManager modelManager = new ServiceModelManager();
          if (!modelManager.parse(serviceModelFile)){
             LOGGER.severe("Could not parse the services model");
             System.exit(1);
          }
          
          LOGGER.info("creating fact manager");

          String factlogFile = conf.get("FACT_LOG");
          if (factlogFile == null) {
             LOGGER.severe("apply the FACT_LOG option to the properties");
             System.exit(1);
          }

          FactManager factManager = new FactManager();
          if (!factManager.init(modelManager)){
             LOGGER.severe("Could not init fact manager");
             System.exit(1);
          }

          // getting the results folder
          String resultsFolder = conf.get("REZ_FOLDER");

          if (resultsFolder == null) {
             LOGGER.severe("apply the REZ_FOLDER option to the properties");
             System.exit(1);
          }

          String clearFolder = conf.get("CLEAR_FOLDER");

          if (clearFolder == null) {
             LOGGER.severe("apply the CLEAR_FOLDER option to the properties");
             System.exit(1);
          }

          // getting the base model location
          String baseModelFile = conf.get("BASEMODEL");
          if (clearFolder == null) {
             LOGGER.severe("apply the BASEMODEL option to the properties");
             System.exit(1);
          }
          String models[] = conf.getAsArray("MODELS");
          if (models == null) {
             LOGGER.severe("apply the MODELS option to the properties");
             System.exit(1);             
          }

          String classModel = conf.get("CLASSMODELIRI");
          if (classModel == null) {
             LOGGER.severe("apply the CLASSMODELIRI option to the properties");
             System.exit(1);
          }

          // trying to parse docker compose files
          LOGGER.info("trying to parse docker compose files");
          String composeApplicationFolder = conf.get("COMPOSE_FOLDER");

          if (composeApplicationFolder == null) {
             LOGGER.severe("apply the COMPOSE_FOLDER option to the properties");
             System.exit(1);
          }
          
          File folder = new File(composeApplicationFolder);
          File[] appFiles = folder.listFiles();
          Arrays.sort(appFiles);
          for (int i = 0; i < appFiles.length; i++) {
             String appFileName = appFiles[i].getName();
             ComposeApplicationManager caManager = new ComposeApplicationManager();
             if (!caManager.parse(composeApplicationFolder+appFileName, appFileName)){
                LOGGER.severe("Could not parse "+appFileName);
                System.exit(1);
             }
             Diagram diagram = factManager.prepareDiagram(caManager.getApp());
             if (diagram != null){
                DiagramSaverAsYaml saver = new DiagramSaverAsYaml();
                saver.saveToFile(resultsFolder+appFileName,diagram);
                
                //DiagramSaverAsFlows saver1 = new DiagramSaverAsFlows();
                //saver1.saveToFile(resultsFolder+appFileName+".flows",diagram);

                DiagramSaverAsItems saver2 = new DiagramSaverAsItems();
                saver2.saveToFile(resultsFolder+appFileName+".items",diagram);

                
                // now private data inside
                diagram.makeClear();
                DiagramSaverAsYaml saverClear = new DiagramSaverAsYaml();
                saverClear.saveToFile(clearFolder+appFileName,diagram);
                
                //DiagramSaverAsFlows saver1Clear = new DiagramSaverAsFlows();
                //saver1Clear.saveToFile(clearFolder+appFileName+".flows",diagram);

                DiagramSaverAsItems saver2Clear = new DiagramSaverAsItems();
                saver2Clear.saveToFile(clearFolder+appFileName+".items",diagram);

                // init diagram saver as ontology
                DiagramSaverAsOntology2 saverOnt = new DiagramSaverAsOntology2();
                if (!saverOnt.init(baseModelFile, models, classModel)){
                   LOGGER.severe("could not init DiagramSaverAsOntology2");
                   System.exit(1);
                }
                saverOnt.saveToFile(clearFolder+appFileName,diagram);
                
                
             }else{
                LOGGER.severe("Could not create a diagram for "+appFileName);                
             }
          }
          if (!factManager.saveFactLog(factlogFile)){
              LOGGER.severe("failed to save fact log");
              System.exit(1);
          }

        } else { LOGGER.severe("the configuration file was not found"); }
     } else { LOGGER.severe("give a configuration file!"); }
   }
}
