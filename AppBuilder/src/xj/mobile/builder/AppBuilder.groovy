
package xj.mobile.builder

import xj.mobile.common.AppGenerator
import xj.mobile.transform.AppTransformer
import xj.mobile.model.Application
import xj.mobile.model.ui.View
import xj.mobile.model.ui.Widget
import xj.mobile.Main
import xj.mobile.tool.GraphViz

import static xj.mobile.util.CommonUtils.encodeAttributeValue

import static xj.translate.Logger.info 

/*
 * Common driver for all platforms
 *
 * To-do: each invocation should handle only one platform, 
 *        reason: design and style transformation are applied to the model,
 *                each platform should start with a clean model
 */
class AppBuilder { 

  static ModelBuilder builder = new ModelBuilder()

  static boolean verbose = true //false
  static boolean WRITE_ATTRIBUTES = true

  String filename
  String target
  def userConfig 

  static Application app = null
  def classes = [:]

  AppBuilder(filename = null, userConfig = null) { 
    this.filename = filename
    this.userConfig = userConfig 
  }

  void init(String target) { 
	this.target = target
	builder.IOS = (target == 'iOS') 
	builder.ANDROID = (target == 'Android')

	info "[AppBuilder] init() target=${target} IOS=${builder.IOS} ANDROID=${builder.ANDROID}"
  }

  //
  // building app: called from script  
  //

  boolean app(args, closure = null) { 
    info '[AppBuilder] Start App Builder'
    info "[AppBuilder] classes: ${classes}"
	boolean okay = true 
    builder.reInit()
    app = new Application()
    if (classes) app.classes = classes 
	builder.handleChildren(app, [ args ], closure)
	if (builder.isOkay()) { 
	  if (app.mainView) okay = process()
	} else { 
	  if (!Main.quiet) { 
		println "=== There are errors in the app definition."
		builder.printMessages()
	  }
	  Main.errors.addAll(builder.errors)
	  builder.errors.clear()
	  okay = false 
	}
    info '[AppBuilder] End App Builder'
	return okay
  }

  //
  // Processing app 
  //

  boolean process() { 
    if (checkApp()) {
	  boolean success = true
	  if (!Main.nocode) { 
		if (verbose) printApp()
		if (WRITE_ATTRIBUTES) writeAttributes()

		if (Main.graphviz) GraphViz.generateDiagram(filename, app)

		processDesignOptions()
		transformApp()
		success = generateApp()
	  }
	  return success
    } else { 
	  return false
	}
  }

  // Write to a file, the attributes used in each element in this app 
  // For generating documentation only, not used for code generation  
  void writeAttributes() { 
    def attrMap = app.attributes() 
    new File('test/lang').mkdirs()
    def file = new File("test/lang/${filename}-attr.txt")
    file.delete()
    file = new File("test/lang/${filename}-attr.txt")
    attrMap.each{ key, value ->
	  value.each { v -> 
		//println "writeAttributes(): Type: ${v.value.class.name}  Value: ${v.value}"
		if (v.value instanceof String || v.value instanceof GString) { 
		  v.value = encodeAttributeValue(v.value)
		}
	  }
      def vstr = value.sort { it.name }.join('\n\t')
      file << "${key}\n\t${vstr}\n"
    }
  }

  void printApp() { 
    info '[AppBuilder] Application: node count = ' + app.count()
    info '[AppBuilder] Application:\n' + app.print()
  }

  boolean checkApp() { 
    AppChecker checker = new AppChecker(app)
    if (checker.isOkay()) { 
      return true
    } else {  
	  if (!Main.quiet) { 
		println "==== There are errors in the app definition."
		checker.printMessages()
	  }
	  Main.errors.addAll(checker.errors)
	  checker.errors.clear()
      return false
    }
  }

  void processDesignOptions() { 
	info '[AppBuilder] processDesignOptions()'
	if (userConfig.design) { 
	  info '[AppBuilder] processDesignOptions() userConfig.design: ' + userConfig.design
	  [ 'iOS', 'Android' ].each { plat ->	  
		String pname = plat.toLowerCase()
		def designOpt = userConfig.design[pname]
		if (designOpt && designOpt instanceof Map) { 
		  info "[AppBuilder] processDesignOptions() userConfig.design[${pname}]: " + userConfig.design[pname]

		  app.visit { node -> 
			if (node instanceof Widget) { 
			  if (designOpt[node.widgetType]) { 
				info "[AppBuilder] processDesignOptions() userConfig.design[${pname}] ${node.widgetType}"

				node["@Design:${plat}"] = designOpt[node.widgetType] 
			  }
			}
		  }
		}
	  }
	}
  }

  void transformApp() { 
	canonicalization()

    AppTransformer transformer = AppTransformer.getAppTransformer('common')
    transformer.transform(app)

    if (verbose)    
      info "[AppBuilder] After common transformation:\n" + app.print()
  }

  void canonicalization() { 
    // insert the root navigation view when necessary 
    if (app.mainView.widgetType != 'NavigationView') { 
	  if (app.views.size() > 1 && app.views.every { it.widgetType != 'NavigationView' } && app.navigationBar || 
		  app.views.any { it.widgetType == 'ListView' || it.widgetType == 'ExpandableListView' }) { 
		def top = new View()
		top.'#type' = 'NavigationView' 
		top.id = Preprocessor.getID('top')
		app.views.each { 
		  top.add(it)
		  it.parent = top
		}
		app.mainView = top
		app.children = [ top ]
	
		if (verbose) info "[AppBuilder] After canonicalization():\n" + app.print()
      } 
      
    }
    app.mainView.root = true
  }

  boolean generateApp() { 
	AppTransformer transformer = AppTransformer.getAppTransformer(target)
	transformer.transform(app)
		
	if (verbose)
	  info "[AppBuilder] After design transformation:\n" + app.print()
		
	AppGenerator generator = AppGenerator.getAppGenerator(target)
	generator.analyze(app, filename, userConfig)
	return generator.generate()
  }  

}

