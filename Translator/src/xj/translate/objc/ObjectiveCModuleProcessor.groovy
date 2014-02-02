package xj.translate.objc

import org.codehaus.groovy.ast.*

import xj.translate.*
import xj.translate.common.*
import xj.translate.typeinf.TypeInference

import static xj.translate.typeinf.ClassDependency.* 
import static xj.translate.common.ClassProcessor.* 
import static xj.translate.typeinf.TypeInference.*
import static xj.translate.Logger.* 

class ObjectiveCModuleProcessor extends ModuleProcessor { 

  ObjectiveCModuleProcessor(ModuleNode moduleNode,
			    String infile,
			    Templates templates, 
			    Unparser unparser) { 
    super(moduleNode, infile, templates, unparser)
  }

  void process(boolean generateCode = true) { 
    info "[ObjectiveCModuleProcessor] Process module: ${moduleNode?.packageName} generateCode: ${generateCode}" 

    Config config = Config.getInstance()
    Map<String, DependencyNode> allNodes = [:]

    moduleNode?.classes?.each { c -> 
      def tempNode = new DependencyNode()
      tempNode.name = c.nameWithoutPackage
      allNodes.put(c.nameWithoutPackage, tempNode)

      moduleMap[c.nameWithoutPackage] = moduleNode 
      //if (!ClassProcessor.isInnerClass(c)) { 
      def cp = classProcessorForLanguage(Config.instance.target, 
					 templates, unparser,
					 moduleNode, c, infile)
      classMap[c.nameWithoutPackage] = cp
      
      if (!ClassProcessor.isInnerClass(cp.classNode)) { 
	def header = config.getSourceFilename(c.packageName, c.nameWithoutPackage, true, true)
	headerFileMap[c.nameWithoutPackage] = header 
	c.innerClasses?.each { ic -> 
	  headerFileMap[ic.nameWithoutPackage] = header 
	}
      }
    }

    TypeInference.inferTypes(classMap, allNodes)

    if (generateCode) { 
      classMap.each { cname, cp ->  
	if (!ClassProcessor.isInnerClass(cp.classNode)) { 
	  currentClassProcessor = cp //To be used for local calls
	  def c = cp.classNode
	
	  File headerFile = config.getOutputFile(c.packageName, c.nameWithoutPackage, true)
	  File bodyFile = config.getOutputFile(c.packageName, c.nameWithoutPackage, false)
	  
	  String headerFilename = headerFile.name
	  int i = headerFilename.lastIndexOf(File.separator)
	  if (i >= 0) headerFilename = headerFilename.substring(i + 1)
	  
	  cp.buildScraps()
	  if (!c.isInterface() && !c.isEnum()) { 
	    // generate body 
	    def body = cp.generateCode()
	    def code = cp.generatePreamble(bodyFile.name, headerFilename, 
					   "Class definition of ${c.nameWithoutPackage}") + 
		       body

	    println "Write source file to ${bodyFile}"
	    println '=========================================================='
	    println code
	    println '==========================================================\n'
	    
	    bodyFile.write(code)
	    addOutputFile(bodyFile.name)
	  }
	  
	  // generate header
	  def hbody = cp.generateHeaderCode()
	  def hcode = cp.generateHeaderPreamble(headerFile.name, 
						"Class interface of ${c.nameWithoutPackage}") + 
		      hbody

	  println "Write source file to ${headerFile}"
	  println '=========================================================='
	  println hcode
	  println '==========================================================\n'
	  
	  headerFile.write(hcode)
	  addOutputFile(headerFile.name)
	}
      }
    }
  }

  String getUtilDir() { 
    'src/objc/'
  }

}
