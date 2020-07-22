package ab.base;

public class MyAxiom{
   public String type;
   public String[] args;
   
   public MyAxiom(String _type, String[] _args){
      type = _type;
      args = _args;
   }
   
   public void print(){
      System.out.println("type="+type);
      for (int i=0; i<args.length; i++){
         System.out.println("args["+i+"]="+args[i]);
      }
   }
}
