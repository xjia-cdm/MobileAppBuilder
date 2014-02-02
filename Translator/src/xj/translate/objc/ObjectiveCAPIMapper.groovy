package xj.translate.objc

import static xj.translate.Logger.* 

class ObjectiveCAPIMapper {  
  
  // if no mapping was found, return null
  public static String mapCall(String obj, String type, 
			       String method, boolean isStatic, 
			       List<String> args) { 
    //info "mapCall: obj=${obj} type=${type} method=${method} isStatic=${isStatic}"
    if (obj && type && method) { 
      if ('java.lang.Integer' == type) { 
	if (isStatic) {
	  if ('parseInt' == method) { 
	    if (args?.size() == 1) {  
	      return "[${args[0]} intValue]"
	    }
	  }
	}
      }

      if ('java.lang.String' == type) { 
	if (!isStatic) { 
	  if ('substring' == method) { 
	    switch (args.size()) { 
	    case 1:
	      return "[${obj} substringFromIndex: ${args[0]}]"
	    case 2:
	      return "[${obj} substringWithRange: NSMakeRange(${args[0]}, (${args[1]}) - (${args[0]}))]"	      
	    }
	  }
	}
      }
    }
    return null 
  }

}