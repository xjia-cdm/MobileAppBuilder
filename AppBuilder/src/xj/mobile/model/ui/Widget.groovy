
package xj.mobile.model.ui

import xj.mobile.model.ModelNode
import xj.mobile.lang.Language

class Widget extends ModelNode { 

  View parent  

  Language.ViewType getViewType() { 
    Lanaguge.getViewType(widgetType)
  }

  String setPlatformWidgetName(String platform, String platformName) { 
	if (platform && platformName) { 
	  platform = platform.toLowerCase()
	  if (this.'#platformWidget' == null) 
		this.'#platformWidget'= [:]
	  this.'#platformWidget'[platform] = platformName
	}
  }

  String getPlatformWidgetName(String platform) { 
	if (platform) { 
	  platform = platform.toLowerCase()
	  if (this.'#platformWidget') 
		return this.'#platformWidget'[platform]
	}
	return null
  }

}