
package ab.im;

import java.util.*;

// CAPEC attack as a object
public class CAPECScope{
   public ArrayList<String> Scopes;
   public ArrayList<String> Impacts;

   public CAPECScope(){
      Scopes = new ArrayList<String>();
      Impacts = new ArrayList<String>();
   }

   public String toString(){
      return Scopes.toString()+Impacts.toString();
   }


}