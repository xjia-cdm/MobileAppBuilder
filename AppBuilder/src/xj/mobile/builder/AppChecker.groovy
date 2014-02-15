package xj.mobile.builder

import xj.mobile.Main
import xj.mobile.lang.*
import xj.mobile.model.*
import xj.mobile.model.ui.*

import static xj.translate.Logger.info 

class AppChecker {
  
  static verbose = true //false

  @Delegate
  ErrorMessages errors = new ErrorMessages()

  Application app = null
  
  AppChecker(app) { 
    this.app = app
    info "[AppChecker] verbose=${verbose}"
    //if (app?.mainView)
	//check(app.mainView)

    if (app.children) { 
      app.children.each { w -> check(w) }
    }
  }

  def check(ModelNode node) { 
    if (verbose) info "[AppChecker] check ${node.nodeType}"
	String nodeType = node.nodeType

	if (node.parent == app && 
		!Language.isTopView(nodeType)) { 
	  errors << new ErrorMessage(file: Main.scriptFile, line: node.'#line', 
								 message: "${nodeType} is not a top-level view. It is not allowed here.")
    
	}
	
	if (Language.isTransition(nodeType)) { 

	} else if (Language.isState(nodeType)) { 

	} else if (Language.isAction(nodeType)) { 

	} else if (Language.isUI(nodeType)) { 

	} else if (Language.isGraphics(nodeType)) { 

	} else { 
	  errors << new ErrorMessage(file: Main.scriptFile, line: node.'#line', 
								 message: "Unknown element '${nodeType}'")
    }

    if (node.children) { 
      node.children.each { w -> check(w) }
    }
  }

} 
