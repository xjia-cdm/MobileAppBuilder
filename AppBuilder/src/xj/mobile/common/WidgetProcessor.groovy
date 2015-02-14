package xj.mobile.common

import xj.mobile.api.AttributeHandler
import xj.mobile.codegen.templates.WidgetTemplates
import xj.mobile.codegen.CodeGenerator

import xj.mobile.model.properties.ModalTransitionStyle
import xj.mobile.model.ui.Widget

import static xj.mobile.common.ViewUtils.getActionInfo
import static xj.mobile.common.ViewUtils.getTransitionInfo
import static xj.mobile.common.ViewUtils.isInsideNavigationView

class WidgetProcessor { 

  static DATA_VAR_NAME = 'data_'

  CodeGenerator generator

  ViewProcessor vp
  def engine

  @Delegate
  WidgetTemplates widgetTemplates

  AttributeHandler attributeHandler

  WidgetProcessor(ViewProcessor vp) { 
    this.vp = vp
    engine = vp.generator.engine							
	attributeHandler = vp.generator.attributeHandler
  }
  
  void declareWidget(Widget widget) { }
  void process(Widget widget) { }

  def genActionCode(Widget widget) { 
    String actionCode = null
    def srcInfo = getActionInfo(widget)
    if (srcInfo) { 
	  actionCode = generator.generateActionCode(vp, srcInfo, widget)
    } else if (widget.next || widget.menu) {
      def next = widget.next
      if (!next) next = widget.menu
	  boolean animated = true
	  ModalTransitionStyle transition = null
	  if (widget.animated != null) 
		animated = widget.animated as Boolean
	  if (widget.transition instanceof ModalTransitionStyle)
		transition = widget.transition
	  def (String nextState, data) = getTransitionInfo(next)
	  String dataStr = null
	  String setup = ''
	  if (nextState) { 
		if (data) {
		  if (data instanceof Map) { 
			dataStr = DATA_VAR_NAME 
			//if (widget['next.data.src']) { 
			if (hasNextDataUpdate(widget)) { 
			  setup = vp.generator.unparseMapExp(vp, widget['next.data.src'].code, DATA_VAR_NAME,  
												 widget)
			} else { 
			  setup = vp.generator.mapToCode(data, DATA_VAR_NAME) 
			}
			setup += '\n'
		  } else { 
			//if (widget['next.data.src']) { 
			if (hasNextDataUpdate(widget)) { 
			  dataStr = vp.generator.unparseUpdateExp(vp, widget['next.data.src'].code, widget)
			} else {			 
			  dataStr = vp.generator.valueToCode(vp.classModel, data)
			}
		  }
		}
		actionCode = vp.generateTransitionCode(nextState, isInsideNavigationView(widget), 
											   vp.view?.embedded as boolean, 
											   animated, transition, dataStr)
		if (actionCode)
		  actionCode = setup + actionCode
	  }
    }
    return actionCode
  }

  boolean hasNextDataUpdate(widget) { 
	if (widget['next.data.src']) { 
	  def useSet = widget['next.data.src'].useSet
	  if (useSet) { 
		for (name in useSet) { 
		  if (vp.getWidget(name)) 
			return true
		  if (vp.hasAttributeForWidget(widget, name))
			return true
		}
	  }
	}
	return false 
  } 

}