
package xj.mobile.codegen.java

import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*

import xj.translate.java.UnparserJava
import static xj.translate.typeinf.TypeInference.getActualType

import xj.mobile.model.impl.ClassModel
import xj.mobile.codegen.EntityUnparser
import xj.mobile.codegen.UnparserViewProperty
import xj.mobile.lang.ast.*

@Mixin(xj.mobile.codegen.UnparserViewProperty)
class UnparserAndroid extends UnparserJava { 

  String target = 'android'
  
  ClassModel classModel

  EntityUnparser entityUnparser 

  @Delegate
  UnparserDummyProperty dummyUnparser = new UnparserDummyProperty(this)

  String expression(Expression exp, ClassNode expectedType = null) { 
    if (exp) { 
      switch (exp.class) { 
      case SetViewPropertyExpression:
		return unparseSetViewProperty(exp)
      case GetViewPropertyExpression:
		return unparseGetViewProperty(exp)
	  case SetDummyPropertyExpression:
		return unparseSetDummyProperty(exp)
	  case GetDummyPropertyExpression:
		return unparseGetDummyProperty(exp)
	  case EntityMethodCallExpression:
		def eh = exp.vp.contentHandler?.findSectionHandler(exp.entity)
		if (eh) { 
		  return eh.unparseEntityMethodCallExpression(exp)
		}
		return super.expression(exp, expectedType)  
	  
	  case PropertyExpression:
		def text = entityUnparser?.unparsePropertyExpression(exp)
		return text ?: super.expression(exp, expectedType)  
	  case MethodCallExpression:
		def text = entityUnparser?.unparseMethodCallExpression(exp)
		return text ?: super.expression(exp, expectedType)  

      case MapExpression:
		return unparseBundle_aux(exp)

	  case ListExpression:
		return unparseListToArray(exp)

      default:
		return super.expression(exp, expectedType)      
      }
    }
    return ''
  }

  String unparsePropertyOfMap(String obj, String prop, ClassNode type) { 
	"${obj}.getCharSequence(\"${prop}\")"
  }

  String unparseMap(MapExpression exp, String var) { 
	def code = [ "Bundle ${var} = new Bundle();" ]
	exp.mapEntryExpressions.each { e -> 
	  code <<  "${var}.putCharSequence(${expression(e.keyExpression)}, ${expression(e.valueExpression)});" 
	}
	return code.join('\n')
  }

  String unparseBundle_aux(MapExpression exp) { 
	classModel.auxiliaryClasses << 'Utils'
	def kvlist = []
	exp.mapEntryExpressions.each { e -> 
	  kvlist << expression(e.keyExpression) << expression(e.valueExpression)
	}
	return 'Utils.makeBundle(' + kvlist.join(', ') + ')'
  }

  String unparseListToArray(ListExpression exp) { 
	//addImportFile('java.util.Arrays') 
	def type = getActualType(exp, currentVariableScope)
	GenericsType[] gen = type.getGenericsTypes() 
	def gentype = 'Object'
	if (gen.size() > 0 && gen[0] != ClassHelper.OBJECT_TYPE) { 
	  gentype = "${owner.typeName(gen[0].name, false)}"
	} 
	return "new ${gentype}[] { ${exp.expressions.collect{ e -> expression(e)}.join(', ')} }"
  }

  String unparseList(ListExpression exp) { 
	addImportFile('java.util.Arrays') 
	def type = getActualType(exp, currentVariableScope)
	GenericsType[] gen = type.getGenericsTypes() 
	def gentype = ''
	if (gen.size() > 0 && gen[0] != ClassHelper.OBJECT_TYPE) { 
	  gentype = "<${owner.typeName(gen[0].name, false)}>"
	} 
	return "Arrays.${gentype}asList(${exp.expressions.collect{ e -> expression(e)}.join(', ')})"
  }

  String unparsePropertyExpression(Expression obj, String pname, 
								   ClassNode type, FieldNode f,
								   boolean safe, boolean spread) { 
	def objStr = expression(obj)
	
	if (type.name == 'Bundle') { 
	  return "${objStr}.getString(\"${pname}\")"
	} else { 
	  return super.unparsePropertyExpression(obj, pname, type, f, safe, spread)
	}
  }

  def addImportFile(f) { 
    //owner.addImportFile(f) 
	classModel.addImport(f)
  }


}