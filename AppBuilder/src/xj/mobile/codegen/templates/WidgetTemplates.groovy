package xj.mobile.codegen.templates

import xj.mobile.model.ui.Widget
import xj.mobile.lang.WidgetMap

class WidgetTemplates { 
  
  static templates = [
    ios :     new IOSWidgetTemplates('ios'),
    android : new AndroidWidgetTemplates('android')
  ]

  static getWidgetTemplates(target) { 
    return templates[target]
  }

  String target

  WidgetTemplates(String target) { 
    this.target = target
  }

  def getWidgetTemplate(Widget widget) { 
    def temp = null
	if (widget) { 
      temp = getWidgetTemplateByPlatformName(widget.getPlatformWidgetName(target))
	}
	return temp
  }

  def getWidgetTemplateByPlatformName(String name) { 
    def temp = null
    if (name) { 
	  temp = widgetTemplates[name]
    }
    return temp
  }

  def getTemplate(wtemp, name) { 
    if (wtemp && wtemp[name]) { 
      return wtemp[name]
    } else { 
      return CommonWidgetTemplate[name]
    }
  }

  // templates for setting attributes 
  def getAttributeSetterTemplate(wtemp, attr) { 
    if (wtemp) { 
      if (wtemp["set_${attr}"]) { 
		return wtemp["set_${attr}"]
      } else if (wtemp['setAttribute']) { 
		return wtemp['setAttribute']
      }
    }  
    if (CommonWidgetTemplate["set_${attr}"]) { 
      return CommonWidgetTemplate["set_${attr}"]
    } else {  
      return CommonWidgetTemplate['setAttribute']
    }
  }

  // templates for getting attributes 
  def getAttributeGetterTemplate(wtemp, attr) { 
    if (wtemp) { 
      if (wtemp["get_${attr}"]) { 
		return wtemp["get_${attr}"]
      } else if (wtemp['getAttribute']) { 
		return wtemp['getAttribute']
      }
    }  
    if (CommonWidgetTemplate["get_${attr}"]) { 
      return CommonWidgetTemplate["get_${attr}"]
    } else {  
      return CommonWidgetTemplate['getAttribute']
    }
  }

  def getIndexedAttributeGetterTemplate(wtemp, attr) { 
    if (wtemp) { 
      if (wtemp["get_${attr}_indexed"]) { 
		return wtemp["get_${attr}_indexed"]
      } else if (wtemp['getIndexedAttribute']) { 
		return wtemp['getIndexedAttribute']
      }
    }  
    if (CommonWidgetTemplate["get_${attr}_indexed"]) { 
      return CommonWidgetTemplate["get_${attr}_indexed"]
    } else {  
      return CommonWidgetTemplate['getIndexedAttribute']
    }
  }

  def getInitialAttributes(wtemp) { 
	wtemp?.initialAttributes
  }

}