
package ab.im;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import java.io.*;
import java.util.logging.*;

import ab.base.*;

// eats CAPEC in XML
// taken from the old project
public class CWEParser extends XMLAbstractParser{

   private static final Logger LOGGER = Logger.getLogger(LManager.class.getName());

   public NodeList patterns;

   public CWEParser (){
      super();
      theMainListName = "Weakness";
   }


   public CWEVulnerability getCWEVulnerabilityByNode(Node node){

      CWEVulnerability vuln = new CWEVulnerability();

      // get all attributes
      NamedNodeMap nodemap = node.getAttributes();
      if (nodemap == null ) {
         LOGGER.severe("node has no attributes");
         return null;
      }
      // iterate all attributes
      for (int i=0; i< nodemap.getLength();i++) {
         Attr attr = (Attr)nodemap.item(i);
         if (attr.getName().equals("Name")) vuln.Name = attr.getValue();
         if (attr.getName().equals("ID")) vuln.ID = attr.getValue();
         if (attr.getName().equals("Abstraction")) vuln.Abstraction = attr.getValue();
      }
      if ( vuln.ID ==null ) {
         LOGGER.severe("node has no ID");
         return null;
      }


      String ID = vuln.ID;

      NodeList nodelist = node.getChildNodes();
      // iterate all child nodes
      for (int m = 0; m<nodelist.getLength(); m++){
          Node tmpnode = nodelist.item(m);
          String tmpname = tmpnode.getNodeName();
          
          if (tmpname.equals("#text")) continue;
         
          if (tmpname.equals("Description")) {
             Node tmpnode1 = getFirstChildNodeByName(tmpnode,"#text");
             if (tmpnode1 != null){
                vuln.Description = tmpnode1.getNodeValue();
             } else {
                LOGGER.severe("could not read Description (ID="+ID+")");
             }
             continue;
          }

          if (tmpname.equals("Likelihood_Of_Exploit")) {
             Node tmpnode1 = getFirstChildNodeByName(tmpnode,"#text");
             if (tmpnode1 != null){
                vuln.ExploitLikehood = Normalizer.setDegree(tmpnode1.getNodeValue());

             } else {
                LOGGER.severe("could not read ExploitLikehood (ID="+ID+")");
             }
             continue;
          }


          // get scope & impact
          if (tmpname.equals("Common_Consequences")){
             NodeList tmpnodelist = tmpnode.getChildNodes();
             for (int i=0;i<tmpnodelist.getLength();i++){
                 Node tmpnode1 = tmpnodelist.item(i);
                 if (tmpnode1.getNodeName().equals("Consequence")){
                     CAPECScope tmpScope = new CAPECScope();
                     NodeList tmpnodelist1 = tmpnode1.getChildNodes();
                     
                     for (int k=0;k<tmpnodelist1.getLength();k++){
                         Node tmpnode2 = tmpnodelist1.item(k);
                         String tmpnode2name = tmpnode2.getNodeName();
                         if (tmpnode2name.equals("#text")) continue;

                         Node tmpnode3 = getFirstChildNodeByName(tmpnode2,"#text");
                         if (tmpnode3 !=null){
                             if (tmpnode2name.equals("Scope")){
                                tmpScope.Scopes.add(Normalizer.setScope3(tmpnode3.getNodeValue()));
                             }
                             if (tmpnode2name.equals("Impact")){
                                tmpScope.Impacts.add(Normalizer.setTechnicalImpact3(tmpnode3.getNodeValue()));
                             }
                         } else{
                             LOGGER.severe("could not read scope (ID="+ID+")");
                         }
                      }
                      vuln.Scopes.add(tmpScope);
                   } 
               }
               continue;
            }

          // get Mode of introduction
          if (tmpname.equals("Modes_Of_Introduction")){
             NodeList tmpnodelist = tmpnode.getChildNodes();
             for (int i=0;i<tmpnodelist.getLength();i++){
                Node tmpnode1 = tmpnodelist.item(i);
                if (tmpnode1.getNodeName().equals("Introduction")){
                   Node tmpnode2 = getFirstChildNodeByName(getFirstChildNodeByName(tmpnode1,"Phase"), "#text");
                   vuln.AppearancePhases.add(Normalizer.setPhase(tmpnode2.getNodeValue()));
                }
             }
             continue;
          }
         
          if (tmpname.equals("Detection_Methods")){
             NodeList tmpnodelist = tmpnode.getChildNodes();
             for (int i=0;i<tmpnodelist.getLength();i++){
                Node tmpnode1 = tmpnodelist.item(i);
                if (tmpnode1.getNodeName().equals("Detection_Method")){
                   Node tmpnode2 = getFirstChildNodeByName(getFirstChildNodeByName(tmpnode1,"Method"),"#text");
                   vuln.DetectionMethods.add(Normalizer.setCourseOfAction(tmpnode2.getNodeValue()));
                }

             }
             continue;
          }


          // get mitigation methods
          if (tmpname.equals("Potential_Mitigations")){
             NodeList tmpnodelist = tmpnode.getChildNodes();
             for (int i=0;i<tmpnodelist.getLength();i++){
                Node tmpnode1 = tmpnodelist.item(i);
                if (tmpnode1.getNodeName().equals("Mitigation")){
                   Node tmpnode2 = getFirstChildNodeByName(getFirstChildNodeByName(tmpnode1,"Strategy"), "#text");
                   if (tmpnode2 != null){
                      vuln.MitigationMethods.add(Normalizer.setCourseOfAction(tmpnode2.getNodeValue()));
                   }
                }
             }
             continue;
          }


          // get CAPECs
          if (tmpname.equals("Related_Attack_Patterns")){
             NodeList tmpnodelist = tmpnode.getChildNodes();
             for (int i=0;i<tmpnodelist.getLength();i++){
                Node tmpnode1 = tmpnodelist.item(i);
                if (tmpnode1.getNodeName().equals("Related_Attack_Pattern")){
                   String tmpID = null;
                   // get all attributes
                   NamedNodeMap nodemap1 = tmpnode1.getAttributes();
                   if (nodemap1 != null ) {
                      // iterate all attributes
                      for (int i4=0; i4< nodemap1.getLength();i4++) {
                         Attr attr = (Attr)nodemap1.item(i4);
                         if (attr.getName().equals("CAPEC_ID")) tmpID = attr.getValue();
                      }
                      if (tmpID !=null) vuln.CAPECs.add("CAPEC-"+tmpID);
                   }
                }
             }
             continue;
          }
          
          if (tmpname.equals("Observed_Examples")){
             NodeList tmpnodelist = tmpnode.getChildNodes();
             for (int i=0;i<tmpnodelist.getLength();i++){
                Node tmpnode1 = tmpnodelist.item(i);
                if (tmpnode1.getNodeName().equals("Observed_Example")){
                   String cveName = getFirstChildNodeByName(getFirstChildNodeByName(tmpnode1,"Reference"),"#text").getNodeValue();
                   if (cveName.startsWith("CVE")){
                      String linkName = getFirstChildNodeByName(getFirstChildNodeByName(tmpnode1,"Link"),"#text").getNodeValue();
                      vuln.CVEs.add(cveName);
                   }
                }

             }
             continue;
          }




      }

      return vuln;

   }


   public boolean process(IMOntologyGenerator generator){
      try{
        for (int i=0; i<theMainList.getLength();i++){
          Node tmpnode = theMainList.item(i);
          CWEVulnerability vuln = getCWEVulnerabilityByNode(tmpnode);
          if (vuln != null){
             generator.processCWEVulnerability(vuln);
          } else {
             LOGGER.severe("Could not process node"+tmpnode.toString());
          }
        }
   
      } catch (Exception e) {
          e.printStackTrace();
          LOGGER.severe("failed :(((");
          return false;
      }
      return true;
   }


   // remove
   public void testCWE(){
      for (int i=0; i<theMainList.getLength();i++){
         Node tmpnode = theMainList.item(i);
         CWEVulnerability vuln = getCWEVulnerabilityByNode(tmpnode);
         vuln.debugShowVuln();
      }
   }


}


