
package org.javax; 

import java.util.Map;
import java.util.HashMap;

public class Maps {

  public static Map makeMap(Object... obj) {
    Map map = new HashMap();
    if (obj != null && obj.length > 0) {
      int i = 0;
      while (i + 1 < obj.length) { 
	map.put(obj[i], obj[i+1]);
	i += 2;
      }
    }
    return map; 
  }

}