//
//  File:    Utils.java
//  Project: ___PROJECTNAME___
//
//  Created by ___FULLUSERNAME___ on ___DATE___.
//  Copyright ___YEAR___ ___ORGANIZATIONNAME___. All rights reserved.
//

package ___PACKAGE___;

import android.os.Bundle;

public class Utils {

  public static Bundle makeBundle(String... obj) {
	Bundle bundle = null;
    if (obj != null && obj.length > 0) {
	  bundle = new Bundle();
      for (int i = 0; i + 1 < obj.length; i += 2) { 
		bundle.putString(obj[i], obj[i+1]);
      }
    }
    return bundle; 
  }

}