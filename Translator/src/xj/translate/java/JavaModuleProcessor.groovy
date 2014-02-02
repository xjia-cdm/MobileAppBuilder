package xj.translate.java

import java.util.ArrayList;
import java.util.LinkedList;

import org.codehaus.groovy.ast.*

import xj.translate.*
import xj.translate.common.*
import xj.translate.typeinf.*

import static xj.translate.typeinf.ClassDependency.* 
import static xj.translate.common.ClassProcessor.* 
import static xj.translate.typeinf.TypeInference.* 
import static xj.translate.Logger.* 

class JavaModuleProcessor extends ModuleProcessor { 

  JavaModuleProcessor(ModuleNode moduleNode,
		      String infile,
		      Templates templates, 
		      Unparser unparser) { 
    super(moduleNode, infile, templates, unparser)
  }
  
  void process(boolean generateCode = true) { 
    info "Process module: ${moduleNode?.packageName} generateCode: ${generateCode}" 

    Map<String, ClassDependency.DependencyNode> allNodes = [:]

    moduleNode?.classes?.each { c -> 
      def tempNode = new ClassDependency.DependencyNode()
      tempNode.name = c.nameWithoutPackage
      allNodes.put(c.nameWithoutPackage, tempNode)

      moduleMap[c.nameWithoutPackage] = moduleNode 
      //if (!ClassProcessor.isInnerClass(c)) { 
      def cp = classProcessorForLanguage(Config.instance.target, 
					 templates, unparser,
					 moduleNode, c, infile)
      classMap[c.nameWithoutPackage] = cp
    }

    TypeInference.inferTypes(classMap, allNodes)

    if (generateCode) { 
      //Now translate into the code
      classMap.each { cname, cp ->  
	if (!ClassProcessor.isInnerClass(cp.classNode)) { 
	  currentClassProcessor = cp //To be used for local calls
	  def c = cp.classNode

	  Config config = Config.getInstance()
	  File outfile = config.getOutputFile(c.packageName, c.nameWithoutPackage)
	  addOutputFile(config.getPackagePath(c.packageName) + outfile.name)
	  def body = cp.generateCode()
	  def code = cp.generatePreamble(outfile.name, 
					 "Class definition of ${c.nameWithoutPackage}") + 
		     body

	  println "Write source file to ${outfile}"
	  println '========================================================'
	  println code      
	  println '=======================================================\n'
	  
	  outfile.write(code)
	}
      }
    }
  }

  String getUtilDir() { 
    'src/java/'
  }

}

