
package xj.mobile.ios

import xj.mobile.*
import xj.mobile.common.*
import xj.mobile.model.*
import xj.mobile.model.ui.*
import xj.mobile.codegen.CodeGenerator
import xj.mobile.codegen.objc.UnparserIOS
import xj.mobile.lang.WidgetMap 

import xj.translate.Language
import xj.translate.Translator
import xj.translate.objc.ObjectiveCClassProcessor
import xj.translate.common.*

import static xj.translate.Logger.info 

class IOSAppGenerator extends AppGenerator {  
  
  String getTarget() { 'iOS' }

  void setUp(AppInfo appInfo) { 
    ViewProcessorFactory.setFactory('iOS')

    def unparser = new UnparserIOS()
    translator = new Translator(Language.ObjectiveC, unparser)
    translator.load('conf/View.groovy')

    unparser.setUnparseOptions([ UseNSInteger: true ])
	unparser.appInfo = appInfo

	CodeGenerator generator = CodeGenerator.getCodeGenerator('ios')
	generator.unparser = unparser

	//println "[IOSAppGenerator] setup() ${appInfo?.appname} ${appInfo?.userConfig?.format?.floatingPoint?.precision}"

	def typeMap = [:]
	WidgetMap.allWidgetNames.each { name -> 
	  String tname = WidgetMap.getNativeWidgetName(name, 'ios')
	  typeMap[name] = tname
	  typeMap['xj.mobile.lang.madl.' + name] = tname 
	}
  	ObjectiveCClassProcessor.ObjectiveCTypeMap = typeMap

    ModuleProcessor.currentClassProcessor = ModuleProcessor.classMap.get('View')
    Unparser.tab = '\t'
  }

  void cleanUp() { 
    translator = null
  }

}

