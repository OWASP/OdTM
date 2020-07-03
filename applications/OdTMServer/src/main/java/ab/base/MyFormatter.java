package ab.base;

import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

// !!! https://docs.oracle.com/javase/7/docs/api/java/util/logging/LogRecord.html
public final class MyFormatter extends Formatter {
   public String format(LogRecord record) {
      String str = "[" +Long.toString(record.getMillis()) + "] " + record.getLevel() + " (" + record.getSourceClassName()+ ":" + record.getSourceMethodName()+ ") - " + record.getMessage() + "\r\n";
      return str;
      //return new java.util.Date() + " " + record.getLevel() + " " + record.getMessage() + "\r\n"; 
      //String str = "[" + new Date(record.getMillis()) + "] " + record.getLevel() + " from " + record.getSourceClassName()+ "." + record.getSourceMethodName()+ " :: " + record.getMessage() + "\r\n";
   }
}
