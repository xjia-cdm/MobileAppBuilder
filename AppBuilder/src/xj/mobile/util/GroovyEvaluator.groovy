package xj.mobile.util

import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.control.*
import org.codehaus.groovy.tools.GroovyClass
import org.codehaus.groovy.GroovyBugError

public class GroovyEvaluator {

  public static Object evaluate(ASTNode node, Map binding = null) {
	CompilationUnit cu = new CompilationUnit();
	SourceUnit dummy = SourceUnit.create("dummy", "");

	cu.addSource(dummy);
	cu.compile(Phases.CONVERSION);

	ClassNode classNode = dummy.getAST().getClasses().get(0);
	MethodNode run = classNode.getMethods("run").get(0);
	Statement code = null 
	if (node instanceof Statement) {  
	  code = node
	} else if (node instanceof Expression) { 
	  code = new ExpressionStatement(node);
	}

	if (code) { 
	  run.setCode(code);
	  cu.compile(Phases.CLASS_GENERATION);

	  def clazz = cu.classes[0]
	  def aClass = cu.classLoader.defineClass(clazz.name, clazz.bytes)
 
	  try {
		def object = aClass.newInstance()
		if (binding) { 
		  binding.each { name, value -> object.setProperty(name, value) }
		}
		return object.invokeMethod("run", null)
	  } catch (InstantiationException | IllegalAccessException e) {
		throw new GroovyBugError(e)
	  }
	}
  }

}