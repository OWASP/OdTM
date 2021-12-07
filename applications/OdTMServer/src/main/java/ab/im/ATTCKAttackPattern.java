package ab.im;

import java.io.*;
import java.util.logging.*;
import java.util.*;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.core.type.*;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import ab.base.*;

public class ATTCKAttackPattern extends ATTCKAbstractRecord{
   
   private static final Logger LOGGER = Logger.getLogger(LManager.class.getName());

   // format description: https://github.com/mitre-attack/attack-stix-data/blob/master/USAGE.md
   
   // id : string
   // name : string
   // type : string
   // x_mitre_is_subtechnique : boolean
   // x_mitre_remote_support : boolean
   // external_references : list
   // x_mitre_platforms : list
   // x_mitre_permissions_required : list
   // x_mitre_effective_permissions : list
   // x_mitre_impact_type : list
   // x_mitre_defense_bypassed : list

   private boolean x_mitre_is_subtechnique;
   private boolean x_mitre_remote_support;
   
   private List<String> xMitrePlatforms;
   private List<String> xMitreDataSources;
   private List<String> xMitrePermissionsRequired;
   private List<String> xMitreEffectivePermissions;
   private List<String> xMitreImpactType;
   private List<String> xMitreDefenseBypassed;

   private ArrayList<ATTCKDataSourceComponent> dataSourceComponents;
   private List<ATTCKKillChainPhase> killChainPhases;


   public void setKillChainPhases(List<ATTCKKillChainPhase> killChainPhases){
      this.killChainPhases = killChainPhases;
   }

   public List<ATTCKKillChainPhase> getKillChainPhases(){
      return killChainPhases;
   }

   public void setDataSourceComponents(){
      if (xMitreDataSources!=null) {
          dataSourceComponents = new ArrayList<ATTCKDataSourceComponent>();
          for (int i=0;i<xMitreDataSources.size();i++) {
             ATTCKDataSourceComponent dc = ATTCKDataSourceComponent.getDataSourceComponent(xMitreDataSources.get(i));
             if (dc != null) dataSourceComponents.add(dc);
             else LOGGER.severe("failed to parse data source: " + xMitreDataSources.get(i));
          }
      }
        
   }

   public List<ATTCKDataSourceComponent> getDataSourceComponents(){
      return dataSourceComponents;
   }


   public void setXMitreImpactType(List<String> xMitreImpactType){
      this.xMitreImpactType = xMitreImpactType;
   }

   public List<String> getXMitreImpactType(){
      return xMitreImpactType;
   }

   public void setXMitreDefenseBypassed(List<String> xMitreDefenseBypassed){
      this.xMitreDefenseBypassed = xMitreDefenseBypassed;
   }

   public List<String> getXMitreDefenseBypassed(){
      return xMitreDefenseBypassed;
   }

   public void setXMitrePermissionsRequired(List<String> xMitrePermissionsRequired){
      this.xMitrePermissionsRequired = xMitrePermissionsRequired;
   }

   public List<String> getXMitrePermissionsRequired(){
      return xMitrePermissionsRequired;
   }

   public void setXMitreEffectivePermissions(List<String> xMitreEffectivePermissions){
      this.xMitreEffectivePermissions = xMitreEffectivePermissions;
   }

   public List<String> getXMitreEffectivePermissions(){
      return xMitreEffectivePermissions;
   }


   public void setXMitrePlatforms(List<String> xMitrePlatforms){
      this.xMitrePlatforms = xMitrePlatforms;
   }

   public List<String> getXMitrePlatforms(){
      return xMitrePlatforms;
   }

   public void setXMitreDataSources(List<String> xMitreDataSources){
      this.xMitreDataSources = xMitreDataSources;
   }

   public List<String> getXMitreDataSources(){
      return xMitreDataSources;
   }
 
   public boolean getX_mitre_is_subtechnique(){
      return x_mitre_is_subtechnique;
   }

   public boolean getX_mitre_remote_support(){
      return x_mitre_remote_support;
   }

   


   public static ATTCKAttackPattern getATTCKAttackPatternFromNode(ObjectMapper mapper,JsonNode node){
     try {
        ATTCKAttackPattern rec = mapper.treeToValue(node, ATTCKAttackPattern.class);         
        if (node.has("x_mitre_platforms")) rec.setXMitrePlatforms (mapper.readValue(node.path("x_mitre_platforms").toString(), TypeFactory.defaultInstance().constructCollectionType(List.class, String.class)));         
        if (node.has("x_mitre_data_sources")) {
           rec.setXMitreDataSources (mapper.readValue(node.path("x_mitre_data_sources").toString(), TypeFactory.defaultInstance().constructCollectionType(List.class, String.class)));
           rec.setDataSourceComponents();
        }
        if (node.has("x_mitre_permissions_required")) rec.setXMitrePermissionsRequired (mapper.readValue(node.path("x_mitre_permissions_required").toString(), TypeFactory.defaultInstance().constructCollectionType(List.class, String.class)));         
        if (node.has("x_mitre_effective_permissions")) rec.setXMitreEffectivePermissions (mapper.readValue(node.path("x_mitre_effective_permissions").toString(), TypeFactory.defaultInstance().constructCollectionType(List.class, String.class)));         
        if (node.has("x_mitre_impact_type")) rec.setXMitreImpactType (mapper.readValue(node.path("x_mitre_impact_type").toString(), TypeFactory.defaultInstance().constructCollectionType(List.class, String.class)));         
        if (node.has("x_mitre_defense_bypassed")) rec.setXMitreDefenseBypassed (mapper.readValue(node.path("x_mitre_defense_bypassed").toString(), TypeFactory.defaultInstance().constructCollectionType(List.class, String.class)));
        
        if (node.has("external_references")) {
            rec.setExternalReferences (mapper.readValue(node.path("external_references").toString(), new TypeReference<List<ATTCKExternalReference>>(){}) );             
            rec.setExtID("mitre-attack"); // find ID in external references
        }
        
        if (node.has("kill_chain_phases")){
            rec.setKillChainPhases(mapper.readValue(node.path("kill_chain_phases").toString(), new TypeReference<List<ATTCKKillChainPhase>>(){}) );             
        }
        
        return rec;
     } catch (Exception e) {
        e.printStackTrace();
        LOGGER.severe("failed to read JSON node "+ node.toString());
        return null;
     }
   }


 
   public void debug(){
      System.out.println("--------------------------");
      System.out.println("extID: ["+extID+"]  extURL: ["+extUrl+"]");
      System.out.println("type: ["+type+"] id: ["+id+ "] name: ["+name+"]");
      System.out.println("x_mitre_is_subtechnique: ["+x_mitre_is_subtechnique+"]");
      System.out.println("x_mitre_remote_support: ["+x_mitre_remote_support+"]");
      if (xMitrePlatforms!=null) {
          System.out.print("x_mitre_platforms: " );
          for (int i=0;i<xMitrePlatforms.size();i++) System.out.print(" ["+xMitrePlatforms.get(i)+"] ");
          System.out.println("");
      }

      if (xMitreDataSources!=null) {
          System.out.print("x_mitre_data_sources: " );
          for (int i=0;i<xMitreDataSources.size();i++) System.out.print(" ["+xMitreDataSources.get(i)+"] ");
          System.out.println("");

          System.out.print("sources & components: " );          
          for (int i=0;i<dataSourceComponents.size();i++) System.out.print(" ["+dataSourceComponents.get(i).getSource()+"<<>>"+dataSourceComponents.get(i).getComponent()+"] ");
          System.out.println("");
          
      }

      if (xMitrePermissionsRequired!=null) {
          System.out.print("x_mitre_permissions_required: " );
          for (int i=0;i<xMitrePermissionsRequired.size();i++) System.out.print(" ["+xMitrePermissionsRequired.get(i)+"] ");          
          System.out.println("");
      }

      if (xMitreEffectivePermissions!=null) {
          System.out.print("x_mitre_effective_permissions: " );
          for (int i=0;i<xMitreEffectivePermissions.size();i++) System.out.print(" ["+xMitreEffectivePermissions.get(i)+"] ");          
          System.out.println("");
      }

      if (xMitreImpactType!=null) {
          System.out.print("x_mitre_impact_type: " );
          for (int i=0;i<xMitreImpactType.size();i++) System.out.print(" ["+xMitreImpactType.get(i)+"] ");          
          System.out.println("");
      }

      if (xMitreDefenseBypassed!=null) {
          System.out.print("x_mitre_defense_bypassed: " );
          for (int i=0;i<xMitreDefenseBypassed.size();i++) System.out.print(" ["+xMitreDefenseBypassed.get(i)+"] ");          
          System.out.println("");
      }

      if (externalReferences !=null){
          System.out.println("external_references: " );
          for (int i=0;i<externalReferences.size();i++) System.out.println(externalReferences.get(i).asString());          
      }

   }
   
}
