package xj.mobile.common

import xj.mobile.model.*
import xj.mobile.model.ui.*
import xj.mobile.model.sm.*

class Analyzer { 
  
  Application app
  AppInfo appInfo
  View rootView

  ViewHierarchyProcessor vhp

  Analyzer(ViewHierarchyProcessor vhp) { 
	this.vhp = vhp

	this.app = vhp.app
	this.appInfo = vhp.appInfo
	this.rootView = vhp.rootView
  }

  void analyzeView(View view) { }
  void analyzeWidget(View owner, Widget widget) { }

  void analyzeTansition(View owner, Transition tranistion) { }
  void analyzeState(View owner, State state) { }
  void analyzeAction(View owner, Action action) { }

  void analyze(View owner, ModelNode node) { }

}