package ab.dc;

import java.io.*;
import java.util.logging.*;
import java.util.*;
import java.util.UUID;

import ab.base.*;

public class Diagram {

   private boolean isClear = false;

   private static final Logger LOGGER = Logger.getLogger(LManager.class.getName());
   private String name;
   private UUID id;
   private List<DFDProcess> processes;
   private List<DFDExternalEntity> externals;
   private List<DFDFlow> flows;
   private List<DFDStorage> storages;

   public static final String RemoteUserName = "user";
   public static final String RemoteUserModel = "RemoteUser"; 
   
   public static final String HostStorageName = "hostStorage";
   public static final String HostStorageModel = "HostStorage";
    
   public static final String DockerSocketName = "dockerSocket";
   public static final String DockerSocketModel = ServiceModel.DockerSocket;

   public static final String DockerVolumeModel = ComposeVolume.DockerVolume;
   public static final String DockerAnonymousVolumeModel = ComposeVolume.DockerAnonymousVolume;
   

   public static final String DataFlow = "DataFlow"; 

   public static final String NetworkFlow = "NetworkFlow";
   public static final String TargetServerFlow = "TargetServerFlow";
   public static final String SourceServerFlow = "SourceServerFlow";
   
   public static final String LinkFlow = "LinkFlow"; 
   
   public static final String DependFlow = "DependFlow"; 

   public static final String StorageFlow = "StorageFlow"; 

   public static final String SensitiveHostPath = "SensitiveHostPath"; 


   public static final String LocalFlow = "LocalFlow"; 
   
   public static final String ReadWriteFlow = "ReadWriteFlow";
   public static final String ReadOnlyFlow = "ReadOnlyFlow";
   
   public static final String GenericProtocol = "GenericProtocol";


   private int scounter = 0;

   public void makeClear(){
      isClear = true;
      
      for (int i=0;i<processes.size();i++) processes.get(i).makeClear();
      for (int i=0;i<storages.size();i++) storages.get(i).makeClear();
      for (int i=0;i<flows.size();i++) flows.get(i).makeClear();
            
   }

   public Diagram(String name){
      id = UUID.randomUUID();
      this.name = name;
      processes = new ArrayList<DFDProcess>();
      externals = new ArrayList<DFDExternalEntity>();
      externals.add(new DFDExternalEntity(RemoteUserName, RemoteUserModel));
      flows = new ArrayList<DFDFlow>();
      storages = new ArrayList<DFDStorage>();
   }

   public String getName(){
      return name;
   }

   public String getId(){
      return id.toString();
   }

   public List<DFDProcess> getProcesses(){
      return processes;
   }

   public List<DFDExternalEntity> getExternals(){
      return externals;
   }

   public List<DFDFlow> getFlows(){
      return flows;
   }

   public List<DFDStorage> getStorages(){
      return storages;
   }


   public DFDItem findItem(String id){
      for (int i=0;i<processes.size();i++){
         DFDProcess ex = processes.get(i);
         if (ex.getId().equals(id)) return ex;
      }      

      for (int i=0;i<externals.size();i++){
         DFDExternalEntity ex = externals.get(i);
         if (ex.getId().equals(id)) return ex;
      }      

      for (int i=0;i<storages.size();i++){
         DFDStorage ex = storages.get(i);
         if (ex.getId().equals(id)) return ex;
      }      

      for (int i=0;i<flows.size();i++){
         DFDFlow ex = flows.get(i);
         if (ex.getId().equals(id)) return ex;
      }      

      
      return null;
   }

   public String makeFlowModel(String func){
      if (func != null) return func+"Flow";
      return null;
   }

   public boolean isStorageModel(String model){
      if (model.equals(makeFlowModel(ServiceModel.GenericStorage))) return true;
      if (model.equals(makeFlowModel(ServiceModel.CertStorage))) return true;
      if (model.equals(makeFlowModel(ServiceModel.ConfigStorage))) return true;
      if (model.equals(makeFlowModel(ServiceModel.LogStorage))) return true;
      if (model.equals(makeFlowModel(ServiceModel.DataStorage))) return true;
      
      return false;
   }

   public DFDFlow findStorageFlowByAttributes(DFDPoint source, DFDPoint target, String model){
      if (model != null){
         if (isStorageModel(model)){
            for (int i=0;i<flows.size();i++){
               DFDFlow flow = flows.get(i);
               if (flow.getSource().getName().equals(source.getName()) && flow.getTarget().getName().equals(target.getName()) && flow.getModel().equals(model) ){
                  return flow;
               }
            }
            
         }
      }
      return null;
   }


   public DFDPoint findDockerSocket(){
      return findStorage(DockerSocketName);
   }

   public DFDPoint findHostStorage(){
      return findStorage(HostStorageName);
   }

   public DFDPoint findStorage(String name){
      for (int i=0;i<storages.size();i++){
         DFDStorage ex = storages.get(i);
         if (ex.getName().equals(name)) return new DFDPoint(ex.getName(),ex.getId().toString());
      }
      return null;
   }

   public DFDPoint findStorageByRealName(String name){
      for (int i=0;i<storages.size();i++){
         DFDStorage ex = storages.get(i);
         if (ex.getRealName() !=null){
            if (ex.getRealName().equals(name)) return new DFDPoint(ex.getName(),ex.getId().toString());
         }
      }
      return null;
   }


   public DFDPoint findRemoteUser(){
      return findExternalEntity(RemoteUserName);
   }

   public DFDPoint findExternalEntity(String name){
      for (int i=0;i<externals.size();i++){
         DFDExternalEntity ex = externals.get(i);
         if (ex.getName().equals(name)) return new DFDPoint(ex.getName(),ex.getId().toString());
      }
      return null;
   }

   public DFDPoint findProcess(String realName, String model){
      for (int i=0;i<processes.size();i++){
         DFDProcess ex = processes.get(i);
         if (ex.getRealName().equals(realName) && ex.getModel().equals(model)) return new DFDPoint(ex.getName(),ex.getId().toString());
      }
      return null;
   }

   public DFDPoint findFirstProcess(String realName){
      for (int i=0;i<processes.size();i++){
         DFDProcess ex = processes.get(i);
         if (ex.getRealName().equals(realName)) return (new DFDPoint(ex.getName(),ex.getId().toString()));
      }
      return null;
   }


   public DFDPoint addProcess(String realName, ServiceModel model){
      DFDProcess process = new DFDProcess(realName,model.getName(), model.getLabels(),processes.size());
      processes.add(process);
      return process.asDFDPoint();
   }

//      LOGGER.info(volume.type+" === "+volume.name+" === "+volume.hostPath+" === "+func+ " === "+model.getName());
   public DFDPoint addStorage(ComposeVolume volume, String func,String realService){
      // intercept Docker Socket
      if (func.equals(ServiceModel.DockerSocket)){
         DFDPoint pt = findDockerSocket();
         if (pt == null){
            DFDStorage storage = new DFDStorage(DockerSocketName,DockerSocketModel);
            storages.add(storage);
            pt = storage.asDFDPoint(); 
         }
         return pt;
      }
      
      if (volume.type.equals(volume.HostFolder)){
         DFDPoint pt = findHostStorage();
         if (pt == null){
            DFDStorage storage = new DFDStorage(HostStorageName,HostStorageModel);
            storages.add(storage);
            pt = storage.asDFDPoint(); 
         }
         return pt;
      }
      
      if (volume.type.equals(volume.DockerVolume)){
         DFDPoint pt = findStorageByRealName(volume.name);
         if (pt == null){
            DFDStorage storage = new DFDStorage("storage"+scounter,DockerVolumeModel);
            storage.setRealService(realService);
            storage.setRealName(volume.name);
            storages.add(storage);
            scounter++;
            pt = storage.asDFDPoint(); 
         }
         return pt;
      }

      if (volume.type.equals(volume.DockerAnonymousVolume)){
         DFDStorage storage = new DFDStorage("storage"+scounter,DockerAnonymousVolumeModel);
         storage.setRealService(realService);
         storages.add(storage);
         scounter++;
         return storage.asDFDPoint();
      }
      
      return null;  
   }


   public void addLabelToProcess(DFDPoint point, String label){
      for (int i=0;i<processes.size();i++){
         DFDProcess ex = processes.get(i);
         if (ex.getId().equals(point.getId())) ex.addLabel(label);
      }      
   }

   public DFDPoint addFlow(DFDPoint source, DFDPoint target, String model, List<String> labels, String portMapping, String storageMapping){
      
      DFDFlow flow = findStorageFlowByAttributes(source,target,model);
      if (flow != null){
         if (storageMapping !=null) flow.getRealStorageMappings().add(storageMapping);
         return flow.asDFDPoint();
      }
      
      flow = new DFDFlow(source,target,model,flows.size(),labels,portMapping,storageMapping);
      flows.add(flow);
      return flow.asDFDPoint();
   }

}
