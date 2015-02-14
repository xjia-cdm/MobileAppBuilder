package xj.mobile.codegen

import xj.mobile.common.AppGenerator
import xj.mobile.codegen.templates.IOSTemplates
import xj.mobile.model.impl.ClassModel
import xj.mobile.model.properties.ModalTransitionStyle

import static xj.mobile.common.ViewHierarchyProcessor.toViewName
import static xj.mobile.model.impl.ViewControllerClass.getViewControllerName
import static xj.mobile.codegen.IOSUtils.valueToCode
import static xj.mobile.codegen.IOSCodeGenOptions.*

class IOSCodeGenerator extends CodeGenerator { 

  IOSCodeGenerator() { 
	templates = IOSTemplates.getInstance()
	actionHandler = new xj.mobile.codegen.IOSActionHandler()
	attributeHandler = new xj.mobile.api.IOSAttributeHandler()
	init()
  }

  void init() { 
	AppGenerator appgen = AppGenerator.getAppGenerator('ios')
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
	String target = isEmbedded ? 'self.owner' : 'self'
    if (nextView != null && nextView != '' && nextView[0] != '#') { 
	  String nextViewControllerName = getViewControllerName(toViewName(nextView))
	  //classModel.addImport(nextViewControllerName)
	  String nextViewVarName = classModel.getIVarName(nextView)
	  String setData = ''
	  if (data) { 
		//String value = valueToCode(data)
		setData = "${nextViewVarName}.data = ${data};\n"
	  }
	  return """if (${nextViewVarName} == nil) ${nextViewVarName} = [[${nextViewControllerName} alloc] init];
${setData}[${target}.navigationController pushViewController:${nextViewVarName} animated:YES];"""	
	} else if (nextView == '#Previous') {  
	  return "[${target}.navigationController popViewControllerAnimated:YES];"
	} else if (nextView == '#Top') {
	  return "[${target}.navigationController popToRootViewControllerAnimated:YES];"
	}
  }

  String generateModalTransitionCode(ClassModel classModel,
									 String curView, 
									 String nextView, 
									 boolean isEmbedded = false, 
									 boolean animated = true,
									 ModalTransitionStyle style = null,
									 String data = null) { 
	String ani = (animated || style != null) ? 'YES' : 'NO'
	String target = isEmbedded ? 'self.owner' : 'self'
    if (nextView != null && nextView != '' && nextView[0] != '#') { 
	  String nextViewControllerName = getViewControllerName(toViewName(nextView))
	  classModel.addImport(nextViewControllerName)
	  String nextViewVarName = classModel.getIVarName(nextView)
	  String setStyle = ''
	  String setData = ''
	  if (style) { 
		setStyle = "\n${nextViewVarName}.modalTransitionStyle = ${style.toIOSString()};"
	  }
	  if (data) { 
		//String value = valueToCode(data)
		setData = "\n${nextViewVarName}.data = ${data};"
	  }
	  return """if (${nextViewVarName} == nil) ${nextViewVarName} = [[${nextViewControllerName} alloc] init];${setStyle}${setData}
[${target} presentViewController:${nextViewVarName} animated:${ani} completion: NULL];"""
	} else if (nextView == '#Previous') {  
	  return "[${target} dismissViewControllerAnimated:${ani} completion:NULL];"
	} else if (nextView == '#Top') {
	  return """UIViewController* top = ${target};
while (top.presentingViewController != nil) top = top.presentingViewController;
[top dismissViewControllerAnimated:${ani} completion:NULL];"""
	}
  }

  String valueToCode(ClassModel classModel, value) { 
	IOSUtils.valueToCode(value)
  }

  String mapToCode(Map value, String var) { 
	if (value) { 
	  "NSDictionary *${var} = ${IOSUtils.valueToCode(value)};"
	} else { 
	  null
	} 
  }

  public String getWidgetIVarName(String name) { 
	GENERATE_PROPERTY_SYNTHESIZER ? name : '_' + name  
  }

}