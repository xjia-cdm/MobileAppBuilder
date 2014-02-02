package xj.mobile.android

import xj.mobile.*
import xj.mobile.model.ui.*
import xj.mobile.lang.*

import xj.mobile.common.ViewProcessor

class NavigationViewProcessor extends DefaultViewProcessor { 

  public NavigationViewProcessor(View view, String viewName = null) { 
    super(view, viewName)
  }

  void process() { 
	currentViewProcessor = this
    generateViewLayout()
  }

} 
