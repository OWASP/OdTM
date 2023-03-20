package ab.dc;

import java.io.*;
import java.util.logging.*;
import java.util.*;

import ab.base.*;

public class FactManager{

   private static final Logger LOGGER = Logger.getLogger(LManager.class.getName());
   
   private ServiceModelManager modelManager;
   private List<String> factlog;

   public boolean init(ServiceModelManager modelManager){
      if (modelManager == null){
         LOGGER.severe("null instead of model manager");
         return false;
      }
      this.modelManager = modelManager; 
      factlog = new ArrayList<String>();
      
      return true;
   }
   
   public Diagram prepareDiagram(ComposeApplication app){
      LOGGER.info("app: "+app.getName());
      Diagram diagram = new Diagram(app.getName());
      boolean rez = true;
      // get list of services
      List<ComposeService> services = app.getServices();
      
      // first iteration over the services
      //    - to find service models 
      //    - to create diagram items 
      //    - to do everything that relates to a single service
      for (int i=0;i<services.size();i++){
         // get a service
         ComposeService service = services.get(i);
         // prepare the service
         service.prepare();
         LOGGER.info ("... service: "+service.getName());
         // for logger
         String servicePath = app.getName()+"|"+service.getName()+"|";
         
         // this will be found service models
         ServiceModel foundServiceModel = null;
         // looking for a service model by image 
         if (service.cImage != null) {
            foundServiceModel = modelManager.findByImage(service.cImage);
            if (foundServiceModel != null) {
               factlog.add(servicePath+ "FACT|MODEL "+foundServiceModel.getName()+"|image: "+service.getImage());
            }
            else factlog.add(servicePath+ "INFO|NO_MODEL_BY_IMAGE|"+service.getImage());
         } else factlog.add(servicePath+"INFO|NO_IMAGE");
       
         // if no model, add the GenericService model that is empty
         if (foundServiceModel == null) {
             foundServiceModel = modelManager.getGenericModel();
             factlog.add(servicePath+ "FACT|MODEL "+foundServiceModel.getName()+"|defaut model");
         }
         
         DFDPoint processPoint = diagram.addProcess(service.getName(),foundServiceModel);
 
            // processing ports
            // todo: duplicated ports because of TCP and UDP
         if (service.cPorts != null){
               // iterating over cPorts
               for (int j=0;j<service.cPorts.size();j++){
                  // this is a port
                  ComposePort port = service.cPorts.get(j);
                  if (port.isOpenSocket){
                     // it will be a model where port is
                     String protocolFlow = null;

                     ServicePort servicePort = modelManager.findPort(port.localPort);
                     if (servicePort !=null){
                        protocolFlow = servicePort.getName()+"Flow";
                        if (servicePort.getLabel() != null) {
                           diagram.addLabelToProcess(processPoint,servicePort.getLabel());
                           factlog.add(servicePath+ "FACT|LABEL_BY_PORT "+servicePort.getValue()+"|"+servicePort.getLabel());
                        }
                     }else{
                        protocolFlow = diagram.GenericProtocol+"Flow";
                     }

                     factlog.add(servicePath+foundServiceModel.getName()+"|FACT|PUB_PORT "+port.pubPort+"|"+port.toString());
                     // get a point of the source (user)
                     DFDPoint source = diagram.findRemoteUser();
                     // get a point of the target (the current process)
                     DFDPoint target = processPoint;
                     // hidden mapping
                     String mapping = port.pubPort+":"+port.localPort;
                     // adding flow from the user to the server (this flow represents possible attacks from the Internet)
                     diagram.addFlow(source,target,diagram.NetworkFlow,new ArrayList<String>(Arrays.asList(protocolFlow)),mapping,null);
                  }
                  // end if
               }
               // end for
         }
         // end if 
            
            // processing volumes
         if (service.cVolumes !=null){
               // iterating over volumes;
               for (int j=0;j<service.cVolumes.size();j++){
                  // current volume
                  ComposeVolume volume = service.cVolumes.get(j);                  
                  String func = modelManager.findVolumeFunction(volume.localPath);
                  
                  if (func != null){
                     // found. the 'func' & 'model' variables can be used
                     factlog.add(servicePath+foundServiceModel.getName()+"|FACT|VOLUME "+func+"|"+volume.toString());
                  }else{
                     // the volume is not found in models 
                     // check if it is a docker socket
                     if (modelManager.isVolumeDockerSocker(volume.hostPath)){
                        // it's the docker socket
                        func = ServiceModel.DockerSocket;
                        factlog.add(servicePath+foundServiceModel.getName()+"|FACT|VOLUME "+func+"|"+volume.toString());
                     } else {
                        // function will be GenericStorage
                        func = ServiceModel.GenericStorage;
                        factlog.add(servicePath+foundServiceModel.getName()+"|INFO|NO_VOLUME_FUNCTION|"+volume.toString()); 
                     }
                  }   
                  DFDPoint source = diagram.findProcess(service.getName(),foundServiceModel.getName());                  
                  DFDPoint storage = diagram.addStorage(volume,func,service.getName());
                  String realStorageMapping = volume.name+":"+volume.hostPath+":"+volume.localPath;
                  String flowType = diagram.ReadWriteFlow;
                  if (volume.isReadOnly) flowType = diagram.ReadOnlyFlow;
                  List<String> props = new ArrayList<String>();
                  props.add(flowType);
                  if (modelManager.isVolumeSensitivePath(volume.hostPath)) props.add(Diagram.SensitiveHostPath);
                  diagram.addFlow(source,storage,diagram.makeFlowModel(func),props,null,realStorageMapping);
                                 
               } 
               // end for              
            }
            // end if
            
      }

      // the second iteration is to process relations between services
      for (int i=0;i<services.size();i++){
         ComposeService service = services.get(i);

         // processing dependencies
         if (service.getDepends_on() != null){
            DFDPoint source = diagram.findFirstProcess(service.getName());
            for (int j=0;j<service.getDepends_on().size();j++){
               String dep = service.getDepends_on().get(j);
               DFDPoint target = diagram.findFirstProcess(dep);
               // to create a flow from the first service process to its dependency (first process too)
               diagram.addFlow(source,target,diagram.DependFlow,null,null, null);
            }
         }

         // processing links
         if (service.getLinks() != null){
            DFDPoint source = diagram.findFirstProcess(service.getName());
            for (int j=0;j<service.getLinks().size();j++){
               String dep = service.getLinks().get(j);
               DFDPoint target = diagram.findFirstProcess(dep);
               // to create a flow from the first service process to its link (the first process)
               diagram.addFlow(source,target,diagram.LinkFlow,null,null,null);
            }
         }
         
         //todo: volumes_from
/*         if (service.getVolumes_from() != null){
            DFDPoint source = diagram.findFirstProcess(service.getName());
            for (int j=0;j<service.getVolumes_from().size();j++){
               String vol = service.getVolumes_from().get(j);
               LOGGER.info(" >>> "+vol+" <<< ");               
            }
         }*/

         
      }
      
      if (rez == true) return diagram;
      return null;
   }
 
   public boolean saveFactLog (String name){
      try (
        FileWriter fw = new FileWriter(name)) {
        for (int i=0;i<factlog.size();i++) fw.write(factlog.get(i)+'\n');
        fw.close();
      } catch (Exception e) {
        e.printStackTrace();
        return false;
      }
      return true;
    }
 
   
}
