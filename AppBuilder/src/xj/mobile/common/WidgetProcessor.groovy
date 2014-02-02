package xj.mobile.common

import xj.mobile.api.AttributeHandler
import xj.mobile.codegen.templates.WidgetTemplates
import xj.mobile.codegen.CodeGenerator

import xj.mobile.model.properties.ModalTransitionStyle

import static xj.mobile.common.ViewUtils.getActionInfo
import static xj.mobile.common.ViewUtils.getTransitionInfo
import static xj.mobile.common.ViewUtils.isInsideNavigationView

class WidgetProcessor { 

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
  
  def genActionCode(widget) { 
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
	  if (nextState) { 
		actionCode = vp.generateTransitionCode(nextState, isInsideNavigationView(widget), 
											   vp.view?.embedded as boolean, 
											   animated, transition, data)
	  }
    }
    return actionCode
  }

}