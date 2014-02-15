
package xj.mobile.codegen.objc

import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*

import xj.translate.objc.UnparserObjectiveC
import xj.translate.typeinf.TypeCategory

import xj.mobile.codegen.EntityUnparser
import xj.mobile.codegen.UnparserViewProperty
import xj.mobile.model.properties.PropertyType
import xj.mobile.model.impl.ClassModel
import xj.mobile.common.AppInfo

import xj.mobile.lang.ast.*

import static xj.mobile.model.PropertyInterpreter.interpretAttributeValue
import static xj.translate.typeinf.TypeInference.getActualType
import static xj.translate.typeinf.TypeUtil.isNumericalType
import static xj.translate.typeinf.TypeUtil.isBooleanType

import static xj.translate.Logger.info 

@Mixin(UnparserViewProperty) 
class UnparserIOS extends UnparserObjectiveC { 

  String target = 'ios'

  AppInfo appInfo
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

      case ListExpression:
		return "@[ ${exp.expressions.collect{ e -> toNSObject(e)}.join(', ')} ]"

      case MapExpression:
		return "@{ ${exp.mapEntryExpressions.collect{ e -> kvpair(e)}.join(', ')} }"

	  case VariableExpression:
		//def type = getActualType(exp, currentVariableScope)
		def type = null
		use (TypeCategory) { 
		  type = exp.getActualType()
		}
		if (type instanceof PropertyType) { 
		  def value = interpretAttributeValue(type.context, false, exp.name)
		  if (value != null) { 
			return value.toIOSString()
		  }
		}
      default:
		return super.expression(exp, expectedType)      
      }
    }
    return ''
  }

  String unparseMap(MapExpression exp, String var) { 
	"NSDictionary *${var} = ${expression(exp)};"
  }

  String unparseSwitchStatement(SwitchStatement stmt) { 
    def type = getActualType(stmt.expression, currentVariableScope)
    if (type instanceof PropertyType) {
	  stmt.caseStatements.each { cs -> 
		use (TypeCategory) { 
		  cs.expression.setActualType(type)
		}
	  }
	}
	super.unparseSwitchStatement(stmt)
  }

  // handling property type 
  String convertToString(Expression exp) { 
	info "[UnparserIOS] convertToString: exp=${exp}"
	if (exp instanceof VariableExpression) { 
	  info "[UnparserIOS] convertToString: exp.dynamicTyped=${exp.dynamicTyped}"
	}
	info "[UnparserIOS] convertToString: exp.type=${exp.type}"

    if (exp) { 
      def type = getActualType(exp, currentVariableScope)
	  info "[UnparserIOS] convertToString: type=${type}"
      if (ClassHelper.STRING_TYPE != type) { 
		if (isNumericalType(type)) { 
		  return "[${toNSNumber(exp)} stringValue]"
		} else if (type.isEnum()) { 
		  if (type instanceof PropertyType) { 
			def nameStrings = type.context.names.collect { n -> "@\"${n}\"" }.join(', ')
			return "[@[${nameStrings}] objectAtIndex:orientation]"
		  } else { 
			return "[${toNSNumber(exp)} stringValue]"
		  }
		} else if (isBooleanType(type)) { 
		  return "(${expression(exp)} ? @\"true\" : @\"false\")"
		} else { 
		  return "[${expression(exp)} description]"
		}
      }
      return expression(exp)
    }
    return ''
  }

  String getFloatFormat() { 
	def floatPrec = appInfo?.userConfig?.format?.floatingPoint?.precision
	//println "[UnparserIOS] getFloatFormat(): appname = ${appInfo?.appname}  floatPrec = ${floatPrec}"
	if (floatPrec) { 
	  int p = floatPrec as int
	  if (p > 0 && p < 6) 
		return "%.${p}f"
	}
	return '%f'
  }

}