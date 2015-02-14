package xj.mobile.common

import xj.mobile.*
import xj.mobile.model.*
import xj.mobile.model.impl.*
import xj.mobile.model.ui.*
import xj.mobile.model.sm.*
import xj.mobile.lang.*

import static org.codehaus.groovy.ast.ClassHelper.*

import static xj.mobile.common.ViewUtils.simpleType
import static xj.mobile.common.ViewUtils.typeOf
import static xj.mobile.common.ViewUtils.getTransitionInfo
import static xj.mobile.common.ViewUtils.getDataVarTypeForWidget
import static xj.mobile.common.ViewUtils.setDataVarTypeForWidget
import static xj.mobile.common.ViewUtils.addDataVarValuesForWidget

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
	  def type = typeOf(data)
	  if (owner.widgetType == 'ListView') { 
		owner.viewProcessor.listItemDataVarType = simpleType(type) 
	  }

	  // to-do: need to handle: top, previous  
	  Widget nextView = app.getChild(next, true)
	  if (nextView && (Language.isPopup(nextView.nodeType) ||
					   Language.isTopView(nextView.nodeType))) { 
		def nextType = getDataVarTypeForWidget(nextView)
		if (nextType) { 
		  if (nextType != type) { 
			setDataVarTypeForWidget(nextView, OBJECT_TYPE)
		  }
		} else { 
		  setDataVarTypeForWidget(nextView, type)
		}

		addDataVarValuesForWidget(nextView, data)
		info "[TransitionAnalyzer] handleTransitionData() addDataVarValuesForWidget: ${nextView.id} => ${data}"
		info "[TransitionAnalyzer] handleTransitionData() update dataVarType: type=${type} dataVarType=${getDataVarTypeForWidget(nextView)}"
	  }
	}
  }

}