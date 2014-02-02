package xj.mobile.common

import xj.mobile.*
import xj.mobile.model.*
import xj.mobile.model.impl.*
import xj.mobile.model.ui.*
import xj.mobile.model.sm.*
import xj.mobile.lang.*

import static org.codehaus.groovy.ast.ClassHelper.*
import static xj.mobile.common.ViewUtils.getTransitionInfo

import static xj.translate.Logger.info 

class TransitionAnalyzer extends Analyzer { 

  TransitionAnalyzer(ViewHierarchyProcessor vhp) { 
	super(vhp)
  }

  void analyzeView(View view) { 
	info "[TransitionAnalyzer] analyzeView() ${view.id} ${view.widgetType}"
  }

  void analyzeWidget(View owner, Widget widget) { 
	info "[TransitionAnalyzer] analyzeWidget() ${owner.id} ${widget.id} ${widget.widgetType}"
	if (widget.next || widget.menu) {
      def next = widget.next
      if (!next) next = widget.menu
	  def (String nextState, data) = getTransitionInfo(next)	
	  if (nextState) {
		handleTransitionData(owner, nextState, data)
	  }
	} 
  }

  void analyzeTansition(View owner, Transition transition) { 
	info "[TransitionAnalyzer] analyzeTransition() ${transition.id} ${transition.widgetType}"
	if (transition.next) {

	}
  }

  void handleTransitionData(View owner, String next, data) { 
	if (next && data) { 
	  // update dataVarType
	  def type = null
	  if (data instanceof String) type = STRING_TYPE
	  else if (data instanceof List) type = LIST_TYPE
	  else if (data instanceof Map) type = MAP_TYPE
	  else type = OBJECT_TYPE

	  if (owner.widgetType == 'ListView') { 
		owner.viewProcessor.listItemDataVarType = type 
	  }

	  ViewProcessor nextViewProcessor = vhp.findViewProcessor(next) 
	  // to-do: need to handle: top, previous  
	  if (nextViewProcessor) { 
		if (nextViewProcessor.dataVarType) { 
		  if (nextViewProcessor.dataVarType != type) nextViewProcessor.dataVarType = OBJECT_TYPE
		} else { 
		  nextViewProcessor.dataVarType = type
		}

		if (nextViewProcessor.dataVarValues == null) { 
		  nextViewProcessor.dataVarValues = []
		}
		nextViewProcessor.dataVarValues << data
	  }

	  info "[TransitionAnalyzer] handleTransitionData() update dataVarType: type=${type} dataVarType=${nextViewProcessor.dataVarType}"
	}
  }

}