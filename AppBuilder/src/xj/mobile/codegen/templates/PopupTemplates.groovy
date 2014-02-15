package xj.mobile.codegen.templates

import xj.mobile.lang.WidgetMap

import static xj.translate.Logger.info 

class PopupTemplates {

  static templates = [
    ios :     new IOSPopupTemplates('ios'),
    android : new AndroidPopupTemplates('android')
  ]

  static getPopupTemplates(target) { 
    return templates[target]
  }

  String target

  PopupTemplates(String target) { 
    this.target = target
  }

  def getTemplate(String name) { 
	popupTemplates[name]
  }

  def getPopupTemplateByPlatformName(name) { 
    def temp = null
    if (name) { 
	  temp = popupTemplates[name]
    }
    return temp
  }

  static String actionTemplateName(boolean isMenu, boolean hasData) { 
	(isMenu ? 'actionMenu' : 'action') + (hasData ? 'Data' : '')
  }

  def getActionTemplate(String popupType, boolean isMenu, boolean hasData) { 
	def temp = popupTemplates[popupType]
	if (temp)
	  return temp[actionTemplateName(isMenu, hasData)]
	else 
	  return null
  }

} 