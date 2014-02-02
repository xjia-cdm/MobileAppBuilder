package xj.mobile.codegen.templates

import static xj.mobile.lang.Language.*
import static xj.mobile.codegen.CodeGenerator.InjectionPoint.*

/*
 * Managing all templates
 *
 */
class Templates { 

  def templatesMap = null

  WidgetTemplates widgetTemplates
  PopupTemplates popupTemplates 

  def getTemplateByRef(String tempRef) { 
	if (tempRef) { 
	  def ref = tempRef.split(':')
	  if (ref.size() >= 2) { 
		if (templatesMap) { 
		  def t1 = templatesMap[ref[0]]
		  if (t1) {
			def t2 = t1[ref[1]]
			if (t2) return t2
		  }
		}
		def wtemp = getTemplateForWidget(ref[0])
		if (wtemp) { 
		  def code = widgetTemplates.getTemplate(wtemp, ref[1])
		  if (code) { 
			return [location: InjectionPointMap[ref[1]], code: code]
		  }
		}
	  }
	}
	return null
  }

  //
  //
  //

  static InjectionPointMap = [
	header: SystemImport,
	framework: Framework,
	delegate: DelegateDeclaration,
	create: LoadView, 
	autoCreate: LoadView, 
	autoLayout: LoadView, 
	autoResize: LoadView, 
	setFrame: LoadView,
	addSubview: LoadView,
	
  ]


  //
  //
  //

  def getTemplate(wtemp, String name) { 
	widgetTemplates.getTemplate(wtemp, name)
  }

  def getActionTemplate(String popupType, boolean isMenu) { 
	popupTemplates.getActionTemplate(popupType, isMenu)
  }

  def getTemplateForWidget(String platformName) { 
	def wtemp = widgetTemplates.getWidgetTemplateByPlatformName(platformName)
	if (wtemp == null) { 
	  wtemp = popupTemplates.getPopupTemplateByPlatformName(platformName)
	}
	return wtemp
  }

  String getWidgetNativeClass(String platformName) { 
	getTemplateForWidget(platformName)?.uiclass
  }

  //
  //
  //

  def getAttributeSetterTemplate(String widgetType, String platformName, String attr) { 
	if (isTopView(widgetType)) { 
	  def temp = templatesMap[widgetType]?.setAttribute?.code
	  if (temp)
		return temp
	  else 
		return templatesMap['Default']?.setAttribute?.code
	} else { 
	  def wtemp = getTemplateForWidget(platformName)
	  return widgetTemplates.getAttributeSetterTemplate(wtemp, attr)
	}
  }
  
  def getAttributeGetterTemplate(String platformName, String attr, boolean indexed = false) { 
	def wtemp = getTemplateForWidget(platformName)
	if (indexed) { 
	  widgetTemplates.getIndexedAttributeGetterTemplate(wtemp, attr)
	} else { 
	  widgetTemplates.getAttributeGetterTemplate(wtemp, attr)
	}
  }
  
  def getIndexedAttributeGetterTemplate(String platformName, String attr) { 
	def wtemp = getTemplateForWidget(platformName)
	widgetTemplates.getIndexedAttributeGetterTemplate(wtemp, attr)
  }



}