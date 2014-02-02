package xj.translate.common

import org.codehaus.groovy.ast.*

import xj.translate.*
import xj.translate.java.JavaModuleProcessor
import xj.translate.objc.ObjectiveCModuleProcessor

import static xj.translate.common.ClassProcessor.*
import static xj.translate.typeinf.TypeInference.*
import static xj.translate.Logger.* 

class ModuleProcessor { 

  static ModuleProcessor moduleProcessorForLanguage(Language lang,
						    ModuleNode moduleNode,
						    String infile,
						    Templates templates, 
						    Unparser unparser) { 
    if (lang == Language.Java) { 
      return new JavaModuleProcessor(moduleNode, infile, templates, unparser)
    } else if (lang == Language.ObjectiveC) { 
      return new ObjectiveCModuleProcessor(moduleNode, infile, templates, unparser)
    }

    return new ModuleProcessor(moduleNode, infile, templates, unparser) 
  }

  ModuleNode moduleNode
  String infile
  Templates templates
  Unparser unparser

  ModuleProcessor(ModuleNode moduleNode,
		  String infile,
		  Templates templates, 
		  Unparser unparser) { 
    this.moduleNode = moduleNode
    this.infile = infile
    this.templates = templates
    this.unparser = unparser

    moduleNode?.classes?.each { c -> 
      if (!ClassProcessor.isInnerClass(c)) { 
	classes << c.nameWithoutPackage
      }
    }

    info "[ModuleProcessor] classes: ${classes}"
    //info "ModuleProcessor.classMap: ${classMap}"
  }
  
  void process(boolean generateCode = true) { 
    info "[ModuleProcessor] Process module: ${moduleNode?.packageName} generateCode: ${generateCode}" 

    def name = infile
    def i = name.lastIndexOf(File.separator);
    def j = name.lastIndexOf('.') 
    if (i < 0) i = 0
    if (j < 0) j = name.length()
    name = name.substring(i, j)

    Config config = Config.getInstance()
    moduleNode?.classes?.each { c -> 
      moduleMap[c.nameWithoutPackage] = moduleNode 
      //if (!ClassProcessor.isInnerClass(c)) { 
      def cp = classProcessorForLanguage(Config.instance.target, 
					 templates, unparser,
					 moduleNode, c, infile)
      classMap[c.nameWithoutPackage] = cp      
    }

    if (generateCode) { 
      File outfile = config.getOutputFile(moduleNode.packageName, name)
      addOutputFile(config.getPackagePath(moduleNode.packageName) + outfile.name)
      println "Write source file to ${outfile}"
      println '======================================================='    
      
      boolean top = true
      classMap.each { cname, cp ->  
	if (!ClassProcessor.isInnerClass(cp.classNode)) { 
	  currentClassProcessor = cp //To be used for local calls
	  if (top) { 
	    outfile.write(cp.generatePreamble(outfile.name))
	    top = false	  
	  }
	  
	  def code = cp.generateCode() + '\n'
	  println code
	  outfile.append(code)
	}      
      }
      println '=======================================================\n'
      
    }
  }

  String getUtilDir() { 
    ''
  }

  static void reInit() { 
    moduleMap = [:]
    outputFiles = [] 
    utilFiles = [] 
    mainClass = null

    classes = [] as Set   
    classMap = [:] 
    headerFileMap = [:]
    currentClassProcessor = null;
  }

  static void addOutputFile(String outfile) { 
    outputFiles << outfile
  }

  static void addUtilFile(String utilfile) { 
    if (utilfile && !utilFiles.contains(utilfile)) { 
      utilFiles << utilfile
      outputFiles << utilfile
    }
  }

  static isEnum(String name) { 
    def cp = classMap[name]
    if (cp) 
      return cp.classNode.isEnum()
    return false
  }

  def copyUtilFiles() { 
    utilFiles.each { f -> 
      def src = utilDir + f
      println "copy util file ${src} to ${f}"
      def srcfile = new File(src)
      Config.instance.getOutputFile(f).write(srcfile.text)
    }
  }

  static Map<String, ModuleNode> moduleMap = [:] // c.nameWithoutPackage --> moduleNode
  static List<String> outputFiles = [] 
  static List<String> utilFiles = [] 
  static String mainClass = null

  static Set<String> classes = [] as Set   // c.nameWithoutPackage
  static Map<String, ClassProcessor> classMap = [:] // c.nameWithoutPackage --> classProcessor
  static Map headerFileMap = [:] // c.nameWithoutPackage -> header file name

  static ClassProcessor currentClassProcessor = null;

  static ClassProcessor getClassProcessor(String name) { 
    classMap[name]
  }

}

