package ab.im;

import java.io.*;
import java.util.logging.*;
import java.util.*;

import ab.base.*;

public class ATTCKKillChainPhase {
   
   private static final Logger LOGGER = Logger.getLogger(LManager.class.getName());

   private String kill_chain_name;
   private String phase_name;

   public String getKill_chain_name(){
      return kill_chain_name;
   }

   public String getPhase_name(){
      return phase_name; 
   }

   public String asString(){
      return "   kill_chain_name: ["+kill_chain_name+"] phase_name: ["+phase_name+"]";
   }
   
}
