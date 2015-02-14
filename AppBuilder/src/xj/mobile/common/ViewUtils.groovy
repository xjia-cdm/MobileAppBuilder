package xj.mobile.common

import xj.mobile.*
import xj.mobile.model.sm.*
import xj.mobile.model.ModelNode
import xj.mobile.model.Application
import xj.mobile.model.impl.Project
import xj.mobile.model.ui.*
import xj.mobile.lang.*

import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*

import static org.codehaus.groovy.ast.ClassHelper.*

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

//
// Utility methods for processing UI views and widgets 
//
class ViewUtils { 

  static ImageAttr = [
	Image: 'file',
	ImageShape: 'file',
	Button: 'image',
	ImageButton: 'image',
  ]

  static boolean allowImage(widget) { 
	widget && widget.widgetType in ImageAttr.keySet()
  }

  static getImageSizeForWidget(widget) { 
	if (widget && ImageAttr[widget.widgetType]) { 
	  return getImageSize(widget[ImageAttr[widget.widgetType]])
	} else { 
	  null
	}
  }

  //
  // Views
  //

  static boolean imageFileExists(imageFile) { 
    File img = new File("${Main.imageDir}/${imageFile}")
    return img.exists() 
  }

  static getImageSize(String imageFile) { 
    File img = new File("${Main.imageDir}/${imageFile}")
    if (img.exists()) { 
      BufferedImage bi = ImageIO.read(img)
      if (bi) { 
		return [ bi.width, bi.height ]
      }
    }
    return [0, 0]
  }

  static String getWidgetName(Widget widget) { 
    widget.id
  }

  static String getActionName(Widget widget) { 
	getActionName(widget.nodeType)
  }

  static String getActionName(String widgetType) { 
    if (widgetType) { 
      return widgetType[0].toLowerCase() + widgetType[1 .. -1] + 'Action'
    }
    return null
  }

  static String getActionName(String widgetType, String name) {
	if (widgetType && name) { 
	  if (name[0] != '_') name = '_' + name
	  return getActionName(widgetType) + name
	}
    return null 
  }

  static boolean hasWidgetTypes(ModelNode widget, wtypes) { 
    if (widget instanceof Widget) { 
      if (widget instanceof View) { 
		return widget.children.any { hasWidgetTypes(it, wtypes) }
      } else { 
		if (wtypes instanceof String) { 
		  return widget.widgetType == wtypes
		} else if (wtypes instanceof Collection) { 
		  return  widget.widgetType in wtypes
		} 
      }
    }
    return false 
  }
  
  static boolean isInsideNavigationView(Widget widget) { 
    if (widget) { 
      def w = widget?.parent
      while (w instanceof Widget) { 
		if (w.widgetType == 'NavigationView') return true
		w = w.parent
      }
    }
    return false
  }

  static View getEnclosingTopView(Widget widget) { 
    if (widget) { 
      def w = widget?.parent
      while (w instanceof Widget) { 
		if (Language.isTopView(w.widgetType)) return w
		w = w.parent
      }
    }
    return null
  }

  static String getPlatformWidgetName(Widget widget) { 
	widget?.getPlatformWidgetName(getEnclosingTopView(widget)?.viewProcessor?.platform) 
  }

  static getTransitionInfo(next) { 
	String nextState = null
	def data = null 
	if (next instanceof Map) {
	  nextState = next.to?.toString()
	  data = next.data
	} else {  
	  nextState = next?.toString()
	}
	return [ nextState, data ]
  } 

  static boolean isStatementVoidType(Statement stmt) { 
	if (stmt) { 
	  switch (stmt.class) { 
	  case ReturnStatement:
		return stmt.expression == null
	  case ExpressionStatement:
		Expression exp = stmt.expression 
		if (exp.operation.text == "=" &&
			exp.leftExpression.class == PropertyExpression) {
		  return true
		} else { 
		  return false
		}
	  case IfStatement: 
		return (isStatementVoidType(stmt.ifBlock) ||
				(stmt.elseBlock && isStatementVoidType(stmt.elseBlock)))
	  case BlockStatement:
		if (stmt.statements && stmt.statements.size() > 0) { 
		  return isStatementVoidType(stmt.statements[-1])
		} else { 
		  return true
		}
	  }
	}
	return false
  }

  //
  //
  //

  static varsDeclaredInView(view) { 
	if (view) { 
	  return view['#info']?.declarations?.keySet()
	}
	return null
  }

  // return the top view that contains the widget
  static findTopView(widget) { 
	while (widget && 
		   !Language.isTopView(widget.nodeType)) { 
	  widget = widget.parent 
	}
	return widget
  }


  //
  //
  //

  static def getDataVarTypeForWidget(Widget widget) { 
	widget?.'#info'?.dataVarType
  }
  
  static void setDataVarTypeForWidget(Widget widget, t) { 
	if (widget) {
	  if (widget.'#info' == null) widget.'#info' = [:]
	  widget.'#info'.dataVarType = t
	}
  }

  static def getDataVarValuesForWidget(Widget widget) { 
	widget?.'#info'?.dataVarValues
  }

  static void addDataVarValuesForWidget(Widget widget, v) { 
	if (widget) {
	  if (widget.'#info' == null) widget.'#info' = [:]
	  if (widget.'#info'.dataVarValues == null) widget.'#info'.dataVarValues = []
	  widget.'#info'.dataVarValues << v
	}
  }

  //
  // data var type 
  //
  
  static typeOf(data) { 
	def type = null
	if (data) { 
	  if (data instanceof String ||
		  data instanceof GString) {  
		type = STRING_TYPE
	  } else if (data instanceof List) { 
		type = LIST_TYPE
		//type = []
		//data.each { v -> type << typeOf(v) }
	  } else if (data instanceof Map) { 
		type = MAP_TYPE
		//type = [:]
		//data.each { k, v -> type[k] = typeOf(v) }
	  } else { 
		type = OBJECT_TYPE
	  }
	}
	return type 
  }

  static simpleType(type) { 
	if (type instanceof List) return LIST_TYPE
	else if (type instanceof Map) return MAP_TYPE
	else return type
  }

  //
  // Widget
  //

  static getActionInfo(widget) { 
    if (widget.action instanceof Closure) { 	
      return widget['action.src']
    } else if (widget.selection instanceof Closure) { 	
      return widget['selection.src']
    } else { 
	  return widget['action.src']
	}
    return null
  }

  static getWidgetAttributes(widget, exclude = null) { 
    if (widget) { 
      def attrs = []
      widget.properties.each { k, v -> 
		if (k[0] != '#' && 
			!k.endsWith('.src') &&
			!(k in ['class', 'id', 'parent', 'builder',
					'_frame', 'frame',
					'next', 'menu',
					'action', 'selection' ]) &&
			(exclude == null || !(k in exclude))) { 
		  attrs << k
		}
      }
      return attrs
    }
    return null
  }

  // !!! depends on widgetTable 
  static boolean isDependentAttribute(widget, attr) { 
	if (widget && attr) { 
	  def useSet = widget["${attr}.src"]?.useSet
	  if (useSet) { 
		def view = findTopView(widget)
		def vars = []
		if (view) { 
		  vars = varsDeclaredInView(view) ?: []
		  vars += view.viewProcessor?.widgetTable?.keySet()
		}
		return useSet.any { v -> v == 'data' || v in vars }		
	  }
	}
	return false
  }

}