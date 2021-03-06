package xj.mobile.codegen

import org.codehaus.groovy.ast.*

import xj.mobile.common.AppGenerator
import xj.mobile.common.ViewHierarchyProcessor 
import xj.mobile.codegen.templates.AndroidTemplates
import xj.mobile.model.impl.ClassModel
import xj.mobile.model.properties.ModalTransitionStyle

import xj.translate.java.JavaClassProcessor

class AndroidCodeGenerator extends CodeGenerator { 

  AndroidCodeGenerator() { 
	templates = AndroidTemplates.getInstance()
	actionHandler = new xj.mobile.codegen.AndroidActionHandler()
	attributeHandler = new xj.mobile.api.AndroidAttributeHandler()
	init()
  }

  void init() { 
	AppGenerator appgen = AppGenerator.getAppGenerator('android')
	unparser = appgen.translator.unparser
	engine = appgen.engine
  }

  //
  // generate transition code
  //

  String generatePushTransitionCode(ClassModel classModel,
									String curView, 
									String nextView, 
									boolean isEmbedded = false, 
									String data = null) { 
	def vhp = AppGenerator.getAppGenerator('android').vhp
    if (nextView != null && nextView != '' && nextView[0] != '#') { 
	  def activityClass = vhp.findViewProcessor(nextView)?.viewName
	  String intentCode = "new Intent(${curView}.this, ${activityClass}.class)"
	  if (data) { 
		//String dataCode = valueToCode(classModel, data)
		return """Intent intent = ${intentCode};
intent.putExtra(\"${nextView.toUpperCase()}_DATA\", ${data}); 
startActivity(intent);
"""
	  } else { 
		return "startActivity(${intentCode});"
	  }
	} else if (nextView == '#Previous') {  
	  return 'finish();'
	} else if (nextView == '#Top') {
	  //String rootViewName = vhp.project.rootViewName
	  def activityClass = vhp.findViewProcessor(vhp.rootView.id)?.viewName
	  return """Intent intent = new Intent(${curView}.this, ${activityClass}.class);
intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
startActivity(intent);"""
	}
  }

  String generateModalTransitionCode(ClassModel classModel,
									 String curView, 
									 String nextView, 
									 boolean isEmbedded = false, 
									 boolean animated = true,
									 ModalTransitionStyle style = null,
									 String data = null) { 
	String code = generatePushTransitionCode(classModel, curView, nextView, isEmbedded, data)
	if (!animated) { 
	  code += "\noverridePendingTransition(0, 0);"
	}
	return code
  }

  String typeName(ClassNode type, boolean mapType = true) { 
	if (mapType) { 
	  return unparser.typeName(type)
	} else { 
	  def tmap = JavaClassProcessor.JavaTypeMap
	  JavaClassProcessor.JavaTypeMap = null
	  String t = unparser.typeName(type)
	  JavaClassProcessor.JavaTypeMap = tmap
	  return t
	}
  }

  String valueToCode(ClassModel classModel, value) { 
	if (value instanceof Map) { 
	  classModel.auxiliaryClasses << 'Utils'
	  def kvlist = []
	  value.each { k, v -> kvlist << k << v}
	  return 'Utils.makeBundle(' + kvlist.collect{ "\"${it}\"" }.join(', ') + ')'
	} else { 
	  return "\"${value}\""
	}
  }

  String mapToCode(Map value, String var) { 
	if (value) { 
	  def code = [ "Bundle ${var} = new Bundle();" ]
	  value.each { k, v -> 
		code <<  "${var}.putCharSequence(\"${k}\", \"${v}\");" 
	  }
	  return code.join('\n')
	}
	return null
  }

}