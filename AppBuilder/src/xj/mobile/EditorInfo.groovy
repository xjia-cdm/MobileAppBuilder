package xj.mobile

import xj.mobile.model.Application
import xj.mobile.builder.AppBuilder
import xj.mobile.builder.ErrorMessages

import xj.mobile.lang.Language

//
// This class provides static methods to be used in the IDE
//
class EditorInfo { 

  //
  // Compilation of madl file
  // return app if successful, null otherwise 
  // if compile fails, use getErrors() to retrieve the error messages 
  //
  static Application compile(String file, String config) { 
	String[] args = ([ '-nocode', '-quiet', file, config ]) as String[]
	Main.main(args)
	if (Main.success) 
	  return AppBuilder.app
	else 
	  return null
  } 

  static boolean isCompileSuccess() { 
	Main.success
  }

  static ErrorMessages getErrors() { 
	Main.errors
  }

  //
  // MADL language info
  //

  static final keywords = [  
	'as', 'assert', 'break', 'case', 'catch', 'class', 'continue', 'def', 'default', 'do', 
	'else', 'extends', 'finally', 'if', 'in', 'implements', 'import', 'instanceof', 'interface', 
	'new', 'package', 'property', 'return', 'switch', 'throw', 'throws', 'try', 'while', 'public',
	'protected', 'private', 'static'
  ];

  static final types = [  
	'void', 'boolean', 'byte', 'char', 'short', 'int', 'long', 'float', 'double', 
	'String', 'Object', 'Date', 'List', 'Map', 'Set'
  ];

  static final constants = [
	'null', 'true', 'false'
  ];

  static getWidgetNames() {  
	Language.definitions.keySet().findAll { Language.isWidget(it) || Language.isPopup(it) }
  }

  static getViewContainerNames() { 
	Language.definitions.keySet().findAll { !Language.isWidget(it) && !Language.isPopup(it) }
  }

  static getTransitionNames() { 
	Language.getTransitions()
  }

  static getShapeNames() { 
	Language.Shapes + [ 'Path' ]
  }

  static getPathNames() { 
	Language.PathElements
  }

  static getActionNames() {
	Language.Actions
  } 

}