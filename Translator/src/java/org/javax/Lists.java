
package org.javax; 

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

public class Lists {
  
  public static List plus(List left, Object right) {
    if (right instanceof Collection) { 
      left.addAll((Collection) right);
    } else { 
      left.add(right);
    }
    return left; 
  }

  public static List minus(List left, Object right) {
    if (right instanceof Collection) { 
      left.removeAll((Collection) right);
    } else { 
      left.remove(right);
    }
    return left; 
  }

  public static List multiply(List left, Number factor) {
    int size = factor.intValue();
    if (size == 0)
      return new ArrayList();
    else if (size < 0) {
      throw new IllegalArgumentException("multiply() should be called with a number of 0 or greater not: " + size);
    }

    List copy = new ArrayList();
    copy.addAll(left);
    for (int i = 1; i < size; i++) {
      left.addAll(copy);
    }
    return left;
  }

}