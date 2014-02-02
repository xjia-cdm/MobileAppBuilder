package xj.translate.java

import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilationUnit.PrimaryClassNodeOperation
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.control.CompilationFailedException

import groovy.text.GStringTemplateEngine

import xj.translate.*
import xj.translate.common.*

import static org.apache.commons.lang3.StringUtils.*

import static xj.translate.common.Unparser.*
import static xj.translate.typeinf.TypeUtil.*
import static xj.translate.typeinf.TypeInference.*

import static xj.translate.typeinf.MethodHelper.transformMathod

class JavaClassProcessor extends ClassProcessor { 

  JavaClassProcessor(ModuleNode moduleNode,
		     ClassNode classNode,
		     String infile,
		     Templates templates, 
		     Unparser unparser) { 
    super(moduleNode, classNode, infile, templates, unparser)
  }

  String defaultClassVisibility() { 
    'public'
  }

  String fieldVisibility(FieldNode f) { 
    if (f) { 
      if (f.public) return 'public'
      int mod = f.modifiers
      if (java.lang.reflect.Modifier.isProtected(mod)) return 'protected'
      if (java.lang.reflect.Modifier.isPrivate(mod)) return 'private'
      //if (java.lang.reflect.Modifier.isPublic(mod)) return 'public'
      //return 'protected'
    }
    return ''
  }

  boolean getterNeeded(FieldNode f) { 
    if (f) { 
      if (f.static) 
	return !(staticMethodMap[getterName(f.name)]?.size() > 0) && !f.final
      else 
	return !(methodMap[getterName(f.name)]?.size() > 0)
    } else { 
      return false
    }
  }

  boolean setterNeeded(FieldNode f) { 
    if (f) { 
      if (f.static) 
	return !(staticMethodMap[setterName(f.name)]?.size() > 0) && !f.final
      else 
	return !(methodMap[setterName(f.name)]?.size() > 0)
    } else {  
      return false
    }
  }

  void handleScript() { 
    if (isScript) {  
      def binding = [ 'name' : name ] 
      def template = engine.createTemplate(templates.scriptMain).make(binding)
      methodDefScrap += template.toString()
    }
  }

  void generateBuildFile(appname, mainclass) {   
    File buildfile = Config.instance.getBuildFile()
    def files = ModuleProcessor.outputFiles.findAll { f -> f.endsWith('.java') }.join(',')
    def binding = [ 'appname'   : appname,
		    'mainclass' : mainclass,
		    'filelist'  : files
		  ] 
    def template = engine.createTemplate(templates.antFileJava).make(binding)
    def code = template.toString()

    println "Write build file to ${buildfile}"
    println '=========================================================='
    println code
    println '==========================================================\n'

    buildfile.write(code)
  }

  String mapTypeName(String tname) { 
    if (tname.startsWith('groovy.')) { 
      if (typeMap.containsKey(tname)) 
	tname = typeMap[tname]
    }

    if (tname.startsWith('java.')) { 
      int k = tname.lastIndexOf('.')
      String impname = tname;
      int j = impname.indexOf('<')
      if (j > 0) { 
	impname = impname.substring(0, j)
	k = impname.lastIndexOf('.')
      }
      addImportFile(impname)

      if (k > 0)
	tname = tname.substring(k + 1)
    }

    return tname
  }

  static typeMap = [
    'groovy.lang.Range' : 'java.util.List'
  ]


  void addUtilFile(file) { 
    if (file && !utilFiles.contains(file)) { 
      def pkg = utilFilePkgMap[file]
      if (pkg) { 
	utilFiles << file
	addImportFile("${pkg}.${file}")
	ModuleProcessor.addUtilFile(Config.instance.getPackagePath(pkg) + file + '.java')
      }
    } 
  }

  static Map utilFilePkgMap = [
    'StringUtil' : 'org.javax',
    'Ranges'     : 'org.javax',
    'Maps'       : 'org.javax',
    'Lists'      : 'org.javax',
  ]

  Set<String> utilFiles = [] as Set
  
}

