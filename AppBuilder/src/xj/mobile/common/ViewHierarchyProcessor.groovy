package xj.mobile.common

import xj.mobile.*
import xj.mobile.model.*
import xj.mobile.model.impl.*
import xj.mobile.model.ui.*
import xj.mobile.model.sm.*
import xj.mobile.lang.*

import static xj.translate.Logger.info 

// process the entire view hierarchy, containing children that are top views
class ViewHierarchyProcessor { 

  Application app
  View rootView

  AppInfo appInfo

  @Delegate
  Project project 

  Map viewProcessorMap
  List analyzers

  public ViewHierarchyProcessor(Application app, AppInfo appInfo) { 
	this.app = app
	this.appInfo = appInfo

    rootView = app.mainView
    viewProcessorMap = [:]
	
	analyzers = [ new TransitionAnalyzer(this) ]

	project = new Project() 
	project.packageName = appInfo.packageName
    project.rootViewName = toViewName(rootView?.id)

    if (rootView) { 
      setup(rootView)
    }
	
	app.children.each { view -> 
	  if (view != rootView) { 
		setup(view)
	  }
	}
	
  }

  void setup(View view) { 
    info "[ViewHierarchyProcessor] setup() ${view.id}"

    if (Language.isTopView(view.widgetType)) {  
      String viewName = toViewName(view.id)
      def vp = ViewProcessorFactory.getViewProcessor(view, viewName)
      if (vp) { 
		vp.init(appInfo)
		viewProcessorMap[view.id] = vp
		if (vp.classModel) { 
		  if (!view.embedded || vp.platform != 'Android') { 
			project.classes << vp.classModel
			vp.classModel.packageName = packageName
			if (vp.classModel.isMainView) { 
			  project.mainViewClass = vp.classModel
			}
		  }
		}

		vp.vhp = this
		view.viewProcessor = vp

		if (view == rootView) { 
		  vp.processRootView()
		}

		vp.processWidgetTable()
		info "[ViewHierarchyProcessor] WidgetTable ${view.id}: ${vp.widgetTable.keySet()}"
      

		view.children.each { widget -> 
		  if (widget instanceof Widget &&
			  Language.isTopView(widget.widgetType)) {  
			info "[ViewHierarchyProcessor] setup() ${view.id}: process widget ${widget.id} ${widget.widgetType}"

			String name = widget.id
			vp.topViews << name
	    
			setup(widget)
		  }
		}
      }
    }
  }

  void analyze() { 
	analyzers.each { analyzer ->
	  if (rootView) { 
		analyze(rootView, analyzer)
	  }
	
	  app.children.each { view -> 
		if (view != rootView) { 
		  analyze(view, analyzer)
		}
	  }
	}
  }

  void process() { 
    if (rootView) { 
      process(rootView)
    }
	
	app.children.each { view -> 
	  if (view != rootView) { 
		process(view)
	  }
	}
	
  }

  //
  // analyze views bottom up, post-order
  //
  void analyze(View view, Analyzer analyzer) { 
    info "[ViewHierarchyProcessor] analyze() ${view.id}"
	if (Language.isTopView(view.widgetType)) { 
      view.children.each { widget -> 
		analyze(view, widget, analyzer)
      }
	  analyzer.analyzeView(view)
	}      
  }

  void analyze(View view, ModelNode widget, Analyzer analyzer) { 
    info "[ViewHierarchyProcessor] analyze() ${view.id} ${widget.id}"
	switch (widget) { 
	case Widget: 
	  if (Language.isTopView(widget.widgetType)) {  
		analyze(widget, analyzer)
	  } else { 
		if (Language.isContainer(widget.widgetType) ||
			Language.isMenu(widget.widgetType)) { 
		  widget.children.each { w -> 
			analyze(view, w, analyzer)
		  }   
		}   
		analyzer.analyzeWidget(view, widget)
	  }
	  break;
	case Transition: 
	  analyzer.analyzeTansition(view, widget)
	  break;
	case State: 
	  analyzer.analyzeState(view, widget)
	  break;
	case Action: 
	  analyzer.analyzeAction(view, widget)
	  break;
	default: 
	  analyzer.analyze(view, widget)
	}
  }


  //
  // process views bottom up
  //
  void process(View view) { 
    info "[ViewHierarchyProcessor] process() ${view.id}"

    if (Language.isTopView(view.widgetType)) {  

	  view.viewProcessor?.preProcess()

      view.children.each { widget -> 
		if (widget instanceof Widget &&
			Language.isTopView(widget.widgetType) && 
			!widget.embedded) {  
		  // non-embedded top view 
		  info "[ViewHierarchyProcessor] process() ${view.id}: process widget ${widget.id} ${widget.widgetType}"
		  process(widget)
		}
      }
      
      info "[ViewHierarchyProcessor] process() ${view.id}: vp.process() ${view.id} ${view.widgetType}"
      view.viewProcessor?.process()

      view.children.each { widget -> 
		if (widget instanceof Widget &&
			Language.isTopView(widget.widgetType) && 
			widget.embedded) {  
		  // embedded top view 
		  info "[ViewHierarchyProcessor] process() ${view.id}: process widget ${widget.id} ${widget.widgetType}"
		  process(widget)
		}
      }
    }
  }

  ViewProcessor findViewProcessor(id) { 
    viewProcessorMap[id]
  }

  def getViewProcessors() { 
    def result = []
    viewProcessorMap.each { key, vp ->
      result << vp
    }
    return result
  }

  static String toViewName(String name) { 
    if (name) { 
      //return name[0].toUpperCase() + name[1 .. -1]
	  return toJavaClassName(name)
    }
    return name
  }

  public static String toJavaClassName(String name) { 
	StringBuilder nameBuilder = new StringBuilder(name.length());    
	boolean capitalizeNextChar = true;

	for (char c: name.toCharArray()) {
	  if (c == '-' || c == '_') {
		capitalizeNextChar = true;
		continue;
	  }
	  if (capitalizeNextChar) {
		nameBuilder.append(Character.toUpperCase(c));
	  } else {
		nameBuilder.append(c);
	  }
	  capitalizeNextChar = false;
	}
	return nameBuilder.toString();
  }

}
