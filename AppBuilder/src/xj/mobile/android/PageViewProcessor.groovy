package xj.mobile.android

import xj.mobile.*
import xj.mobile.model.ui.*
import xj.mobile.lang.*

import xj.mobile.common.ViewProcessor

class PageViewProcessor extends DefaultViewProcessor { 

  public PageViewProcessor(View view, String viewName = null) { 
    super(view, viewName)
  }

  void process() { 
	currentViewProcessor = this
  }

} 