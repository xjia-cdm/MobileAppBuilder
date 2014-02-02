
package xj.translate

import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilationUnit.PrimaryClassNodeOperation
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.control.CompilationFailedException;

import xj.translate.common.*

import static xj.translate.common.ModuleProcessor.*
import static xj.translate.common.ClassProcessor.*

import static xj.translate.Logger.* 

enum Language { 
  Raw,    // unfiltered groovy, for debugging 
  Groovy,
  Java,
  Scala,
  ObjectiveC,
  Cpp,
}

class Main { 
  
  /*
   * Compile phases:
   *  
   * Initialization: source files are opened and environment configured
   * Parsing: the grammar is used to to produce tree of tokens representing the source code
   * Conversion: An abstract syntax tree (AST) is created from token trees.
   * Semantic Analysis: Performs consistency and validity checks that the grammar can't check for, 
   *                    and resolves classes.
   * Canonicalization: Complete building the AST
   * Instruction Selection: instruction set is chosen, for example java5 or pre java5
   * Class Generation: creates the binary output in memory
   * Output: write the binary output to the file system
   * Finalization: Perform any last cleanup
   */
  /*
   * Command-line options:
   *  -phase={init, parse, conv, sem, canon, inst, class, output, final}
   *  -target={raw,groovy,java,scala,objc,c++} 
   *  -output=dir
   *  -header=yes/no
   */
  static main(args) { 
    //def phase = Phases.CONVERSION;  
    //def phase = Phases.SEMANTIC_ANALYSIS;
    Config config = Config.instance;
    config.init()
    config.phase = Phases.CANONICALIZATION;
    //config.phase = Phases.SEMANTIC_ANALYSIS;
    //config.phase = Phases.CONVERSION;  
    String output = 'out'

    Unparser unparser = null; 
    Templates templates = null;
    
    ModuleProcessor.reInit()
    Unparser.reInit()
    
    if (args.length > 0) { 
      args.each { f -> 
	if (f[0] == '-') { 
	  // options 
	  config.addOption(f)

	  String opt = f.substring(f.indexOf('=') + 1)
	  if (!f.startsWith('-output=')) 
	    opt = opt.toLowerCase() 
	  println "opt=${opt}"
	  if (f.startsWith('-phase=')) { 
	    switch (opt) { 
	    case 'init':   config.phase = Phases.INITIALIZATION; break;
	    case 'parse':  config.phase = Phases.PARSING; break;
	    case 'conv':   config.phase = Phases.CONVERSION; break; 
	    case 'sem':    config.phase = Phases.SEMANTIC_ANALYSIS; break; 
	    case 'canon':  config.phase = Phases.CANONICALIZATION; break;  
	    case 'inst':   config.phase = Phases.INSTRUCTION_SELECTION; break;
	    case 'class':  config.phase = Phases.CLASS_GENERATION; break;
	    case 'output': config.phase = Phases.OUTPUT; break;
	    case 'final':  config.phase = Phases.FINALIZATION; break;
	    }
	  } else if (f.startsWith('-target=')) { 
	    switch (opt) { 
	    case 'raw':    config.target = Language.Raw; break;
	    case 'groovy': config.target = Language.Groovy; break;
	    case 'java':   config.target = Language.Java; break;
	    case 'scala':  config.target = Language.Scala; break;
	    case 'objc':   config.target = Language.ObjectiveC; break;
	    case 'c++':    config.target = Language.Cpp; break;
	    }
	  } else if (f.startsWith('-output=')) { 
	    output = opt
	  } else if (f.startsWith('-header=')) { 
	    config.setHeaderOption(opt)
	  } else if (f.startsWith('-log=')) { 
	    switch (opt) { 
	    case 'all'    : Logger.logLevel = ALL; break;
	    case 'fine'   : Logger.logLevel = FINE; break;
	    case 'info'   : Logger.logLevel = INFO; break;
	    case 'warning': Logger.logLevel = WARNING; break;
	    case 'error'  : Logger.logLevel = ERROR; break;
	    case 'none'   : Logger.logLevel = NONE; break;
	    } 
	  }
	} else { 
	  // file or dir 
	  println "Translate ${f}"

	  if (!unparser) { 
	    unparser = Unparser.unparserForLanguage(config.target)
	    templates = Templates.templatesForLanguage(config.target)
	  }

	  File file = new File(f)
	  if (file.isDirectory()) {  
	    config.outdir = f + File.separator + output
	    
	    ProjectLoader projLoader = new ProjectLoader()
	    projLoader.processFile = { path -> 
	      parseSourceFile(path, config.phase, template, unparser)
	    }
	    projLoader.loadProject(f)
	  } else if (file.exists()) {   
	    config.outdir = file.parent + File.separator + output
	    
	    parseSourceFile(f, config.phase, templates, unparser)
	  } else { 
	    println "${file} not found"
	  }

	  String appname = f
	  int i = appname.lastIndexOf(File.separator)
	  if (i >= 0) appname = appname.substring(i + 1)
	  i = appname.indexOf('.')
	  if (i > 0) appname = appname.substring(0, i)
	  ClassProcessor cp = classProcessorForLanguage(Config.instance.target, 
							templates, unparser) 
	  cp.generateBuildFile(appname, ModuleProcessor.mainClass ?: '')

	  ModuleProcessor mp = moduleProcessorForLanguage(Config.instance.target, null, null, 
							  templates, unparser)
	  mp.copyUtilFiles()
	}
      }
    } else { 
      println "No file"
    }

    //System.exit(0)
  } 


  /** 
   * Parse a Groovy file and add a class node to the class diagram  
   */
  static parseSourceFile(String infile, phase, 
			 Templates templates, 
			 Unparser unparser) {
    if (infile != null) { 
      println "Parse ${infile}"
      println '========================================================'
      println new File(infile).text
      println '=======================================================\n'

      CompilationUnit cu = new CompilationUnit() 
      cu.addSource(new File(infile));
      try {
	//cu.compile(Phases.CANONICALIZATION)
	//cu.compile(Phases.SEMANTIC_ANALYSIS)
	//cu.compile(Phases.CONVERSION)
	cu.compile(phase)

	CompileUnit astRoot = cu.ast

	astRoot.modules.each {  m -> 
	  ModuleProcessor mp = moduleProcessorForLanguage(Config.instance.target, m, infile, templates, unparser)
	  mp.process()
	}

	/*
	astRoot.classes.each {  c -> 
	  processGroovyClass(c, infile)
	} 
	*/
      } catch (CompilationFailedException cfe) {
	error cfe.toString()
	cfe.printStackTrace()
      } catch (Throwable t) {
	error t.toString()
	t.printStackTrace()
      }
    }
  }

}