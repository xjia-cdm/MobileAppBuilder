
package xj.translate

class Logger {  
  public static final int ALL     = 5;
  public static final int FINE    = 4;
  public static final int INFO    = 3;
  public static final int WARNING = 2;
  public static final int ERROR   = 1;
  public static final int NONE    = 0;
    
  static int logLevel = INFO

  static void log(String msg) { 
    println msg
  }

  static void info(String msg) { 
    if (logLevel >= INFO)
      println msg
  }

  static void warning(String msg) { 
    if (logLevel >= WARNING)
      println msg
  }

  static void error(String msg) { 
    if (logLevel >= ERROR)
      println msg
  }  

}