
package org.javax; 

import java.util.List;
import java.util.ArrayList;

public class Ranges {

  public static List<Integer> makeRange(int from, int to) { 
    return makeRange(from, to, true);
  }

  public static List<Integer> makeRange(int from, int to, boolean inclusive) { 
    List<Integer> r = new ArrayList<Integer>();
    if (from <= to) {
      for (int i = from; i <= to; i++) {
	if (inclusive || i < to)
	  r.add(i);
      }
    } else { 
      for (int i = from; i >= to; i--) {
	if (inclusive || i > to)
	  r.add(i);
      }
    }
    return r;
  } 

}