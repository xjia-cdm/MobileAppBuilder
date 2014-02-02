
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

class Translator { 

  Unparser unparser
  Templates templates
  Language lang

  public Translator(Language lang, Unparser unparser = null) { 
    if (lang) { 
      this.lang = lang
      Config.instance.target = lang
      Config.instance.phase = Phases.CANONICALIZATION
      if (unparser) { 
	this.unparser = unparser
      } else { 
	this.unparser = Unparser.unparserForLanguage(lang)
      }
      templates = Templates.templatesForLanguage(lang)
    }
  }

  public void load(String infile) { 
    if (infile) { 
      CompilationUnit cu = new CompilationUnit() 
      cu.addSource(new File(infile));
      try {
	cu.compile(Config.instance.phase)
	//cu.compile(Phases.CANONICALIZATION)
	//cu.compile(Phases.SEMANTIC_ANALYSIS)
	//cu.compile(Phases.CONVERSION)

	CompileUnit astRoot = cu.ast

	astRoot.modules.each {  m -> 
	  ModuleProcessor mp = moduleProcessorForLanguage(lang, m, infile, templates, unparser)
	  mp.process(false)
	}
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