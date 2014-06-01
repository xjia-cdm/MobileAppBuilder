
package xj.mobile

import org.codehaus.groovy.control.*
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.control.customizers.*

import xj.mobile.builder.*
import xj.mobile.model.Application

import xj.translate.Logger

import static xj.mobile.util.CommonUtils.*
import static xj.translate.Logger.info 
				    
/**
 * Entry point of Mobile App Builder
 * Arguments:
 *   madl-script user-config        (complete)
 * or 
 *   app-dir                        (to-do) 
 */

class Main { 

  static TESTING = false

  static PROG_NAME = 'Mobile App Builder'
  static PROG_VERSION = 'ver. 1.0.0 (2014/03)'

  static COMMAND_LINE_ARGS = null

  static userConfigFile = null
  static userConfigName = null
  static userConfig = null
  static String scriptFile = null
  static String sourceDir = null 
  static String resDir = null 
  static String destDir = null

  static String APP_BUILDER_HOME = null
  static String CONF_DIR = null

  static final String WORK_DIR = 'work' 
  static final String SCRIPT_FILENAME = WORK_DIR + File.separator + 'app.groovy'  


  static String getAppBuilderHome() { 
	if (APP_BUILDER_HOME == null) { 
	  String env = System.getenv('APP_BUILDER_HOME')
	  if (env) { 
		if (env.endsWith(File.separator)) 
		  APP_BUILDER_HOME = env
		else 
		  APP_BUILDER_HOME = env + File.separator
	  } else { 
		APP_BUILDER_HOME = ''
	  }
	} 
	return APP_BUILDER_HOME
  }

  static String getConfDir() { 
	if (CONF_DIR == null) { 
	  CONF_DIR = getAppBuilderHome() + 'conf'
	}
	return CONF_DIR
  }

  static String getImageDir() {
	String prefix = ''
	if (resDir) 
	  return resDir 
	else if (sourceDir) 
	  prefix = sourceDir + File.separator 
	return prefix + 'images'
    //sourceDir ? "${sourceDir}/images" : "${new File('.').absolutePath}/images"
  }


  static systemConfig = new ConfigSlurper().parse(new File(confDir + '/system.conf').toURL())

  static boolean nodate = false
  static boolean nocode = false // compilation only
  static boolean quiet = false
  static boolean graphviz = false // generate diagram in GraphViz 
  static boolean inIDE = false

  static boolean success = true 
  static ErrorMessages errors = new ErrorMessages()

  public static void main(String[] args) {
	//systemConfig = new ConfigSlurper().parse(new File(confDir + '/system.conf').toURL())
	//info "[Main] args: ${args}"
    reInit()

	COMMAND_LINE_ARGS = args
    if (args?.length > 0) { 
	  boolean expectDest = false 
	  boolean expectHome = false 
	  boolean expectRes = false 
      args.each { f -> 
		if (f[0] == '-') { 
		  // options 
		  if ('-nodate' == f) { 
			nodate = true
		  } else if ('-ide' == f) { 
			inIDE = true
			AppBuilder.verbose = false
			AppBuilder.WRITE_ATTRIBUTES = false
		  } else if ('-nocode' == f) { 
			nocode = true
		  } else if ('-quiet' == f) { 
			quiet = true
			Logger.logLevel = Logger.NONE
		  } else if ('-graphviz' == f) { 
			graphviz = true
		  } else if ('-d' == f) { 
			// destination dir 
			expectDest = true
		  } else if ('-res' == f) { 
			// images dir 
			expectRes = true
		  } else if ('-h' == f) { 
			// app builder home dir 
			expectHome = true
		  }
		} else {  
		  if (expectDest) { 
			destDir = f
			expectDest = false 
		  } else if (expectRes) {
			resDir = f
			expectRes = false
		  } else if (expectHome) { 
			APP_BUILDER_HOME = f
			expectHome = false 
		  } else if (!scriptFile) { 
			scriptFile = f
			sourceDir = new File(scriptFile).parent
		  } else {  
			File file = new File(f)
			userConfigFile = f
			userConfigName = getFileName(file.name) //filename[0 ..< filename.indexOf('.')]
			userConfig = new ConfigSlurper().parse(file.toURL())
		  }
		}
      }

      if (scriptFile) { 
		if (!quiet) { 
		  println "${PROG_NAME} ${PROG_VERSION}"
		  println "Processing ${scriptFile} ${userConfigFile}"
		}
		success = new Main().runScript(scriptFile)
	  }
    } else {  
	  if (!quiet) println "[Usage] appbuilder <options> <madl file> <user config file>"
    }
  }

  public static reInit() { 
	success = true 
	errors.clear();

    userConfig = null;
    scriptFile = null;
    sourceDir = null; 
    resDir = null; 
	destDir = null;
    nodate = false;
	nocode = false; 
	quiet = false;
	graphviz = false;
	inIDE = false;

	APP_BUILDER_HOME = null
	COMMAND_LINE_ARGS = null

	Logger.logLevel = Logger.INFO
  }

  boolean runScript(madlScript) { 
	boolean okay = true
    try { 
      File infile = new File(madlScript)
      String filename = getFileName(infile.name)
      //filename = filename[0 ..< filename.indexOf('.')]
	  
	  String plat_suffix = [ 'ios', 'android' ].findAll{ userConfig.platform[it] }.join('-')
	  String uconfig = userConfigName
	  int i = userConfigName.indexOf(File.separator)
	  Logger.setLogFile("logs${File.separator}${filename}-${plat_suffix}+${userConfigName}.log")

      String srcdir = infile.parent 
	  info "[Main] command-line args: ${COMMAND_LINE_ARGS?.join(' ')}"
      info "[Main] infile: ${infile}   fileanme: ${filename}  srcdir: ${srcdir}   resDir: ${resDir}"
	  info "[Main] APP_BUILDER_HOME: ${APP_BUILDER_HOME}"

      def script = SCRIPT_HEADER + infile.text
	  def wdir = new File(WORK_DIR)
      if (!wdir.exists()) wdir.mkdir()
	  def script1 = new File(SCRIPT_FILENAME)
      script1.write(script)

      def builder = new AppBuilder(filename, userConfig)
      okay = checkScript(script1, madlScript, builder, errors)

      if (okay) { 
		def binding = new Binding([builder : builder])
		List<String> classpath = [ 'lib' + File.separator + 'madl.jar' ]
		CompilerConfiguration cc = new CompilerConfiguration();
		//cc.setScriptBaseClass( Main.class.getName() );
		cc.setClasspathList(classpath);
		// inject default imports
		//ImportCustomizer ic = new ImportCustomizer();
		//ic.addStarImports('xj.mobile.lang.madl')
		//cc.addCompilationCustomizers(ic);

		ClassLoader classloader = this.class.getClassLoader();
		GroovyShell shell = new GroovyShell(classloader, binding, cc)

		boolean first = true
		[ 'iOS', 'Android' ].each { plat ->	  
		  if (okay &&
			  userConfig.platform[plat.toLowerCase()] && 
			  (first || !nocode)) { // if compile-only, process only the first platform 
			info "===== Start ${plat} ====="
			Preprocessor.reset()
			builder.init(plat)
			okay &= shell.evaluate(script1)
			info "===== End ${plat} ====="
			first = false 
		  }
		}
      } else { 
		if (!quiet) { 
		  println "[Error] There are errors in the input."
		  errors.printMessages()
		}
		okay = false 
      }
    } catch (FileNotFoundException e) { 
	  println "[Error] ${e.class.simpleName} caused by\n\t${e.message}.\nFail to process ${madlScript}" 
      //println "[Error] File not found: ${madlScript}" 
	  okay = false 
    } catch (Exception e) { 
	  if (TESTING) throw e

	  println "[Error] ${e.class.simpleName} caused by\n\t${e.message}.\nFail to process ${madlScript}" 
	  e.printStackTrace()
	  okay = false 
	}

	  /*
	// comment out this exception for regression test, include for release 
    } catch (Exception e) { 
	  println "[Error] ${e.class.simpleName} caused by\n\t${e.message}.\nFail to process ${madlScript}" 
	  e.printStackTrace()
    }
	  */

	if (inIDE) { 
	  new File(SCRIPT_FILENAME).delete()	  
	  new File(WORK_DIR).delete()	  
	}
	return okay
  }

  boolean checkScript(File script, String name, AppBuilder builder, def errors = null) {
    boolean verbose = true 
    boolean okay = true

    CompilationUnit cu = new CompilationUnit() 
    //cu.addSource(filename, script);
    cu.addSource(script);
    try {
      cu.compile(Phases.CANONICALIZATION)
      //cu.compile(Phases.SEMANTIC_ANALYSIS)
      //cu.compile(Phases.CONVERSION)
      
      CompileUnit astRoot = cu.ast
      
      astRoot.modules.each {  module -> 
		module.classes.each { c -> 
		  if (verbose) info "[CheckScript] class ${c.name}"

		  if (c.name != 'app') { 
			builder.classes[c.name] = c
		  }

		  c.fields.each { f -> 
			if (verbose) info "[CheckScript] field: ${f.name}"
		  }
		  c.methods.each { m -> 
			if (verbose) info "[CheckScript] method: ${m.name}"

			if (m.name == 'run') { 
			  okay &= Preprocessor.checkViewBuilder(m.code, name, errors)
			}
		  }
		}
      }
    } catch (CompilationFailedException cfe) {
	  def err = ErrorMessage.parseMessage(cfe)
	  if (err)
		errors << err

	  //cfe.printStackTrace()
    } catch (Throwable t) {
      t.printStackTrace()
    }

    return okay && errors.okay
  }

  static final SCRIPT_HEADER = '''import xj.mobile.lang.madl.*
def app = { args, closure -> builder.app(args, closure) }
'''

static final SCRIPT_HEADER_LINE = 2

}

