
package ab.base;

import java.util.logging.*;
import java.io.IOException;

// to add logging support for a class add: 
//   private static final Logger LOGGER = Logger.getLogger(LManager.class.getName());
// to enable global logging run: 
//   LManager.init();  
public class LManager {
   private static Handler consoleHandler;
   private static MyFormatter formatter;
   
   public static void init(){
	   init(Level.ALL);
   }
   
   public static void initSEVERE(){
	   init(Level.SEVERE);
	   
   }
   public static void init(Level level){
	  Logger LOGGER = Logger.getLogger(LManager.class.getName());
      LOGGER.setUseParentHandlers(false); 

      consoleHandler = new ConsoleHandler();
	  consoleHandler.setLevel(level);
	  formatter = new MyFormatter();
	  consoleHandler.setFormatter(formatter);
	  
	  LOGGER.addHandler(consoleHandler);
	  LOGGER.setLevel(Level.ALL);
	  LOGGER.info("starting as " + LOGGER.getName());
   }
}
