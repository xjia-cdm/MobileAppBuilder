package xj.translate.common

import java.util.ArrayList;
import java.util.Set;
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*

import xj.translate.*
import xj.translate.java.UnparserJava
import xj.translate.objc.UnparserObjectiveC
import xj.translate.typeinf.VariableScopeCategory

import static xj.translate.Logger.* 
import static xj.translate.typeinf.TypeInference.* 

/** An unparser of groovy AST. Output groovy source.
 *  Handle statements and expressions 
 */

class Unparser { 

  static Unparser unparserForLanguage(Language lang) { 
    if (lang == Language.Java) { 
      return new UnparserJava()
    } else if (lang == Language.ObjectiveC) { 
      return new UnparserObjectiveC()
    }
    return new Unparser()
  }

  boolean explicitToString = false
  Operators operators = new Operators();

  ClassProcessor getOwner() { 
    ModuleProcessor.currentClassProcessor
  } 

  VariableScope currentVariableScope = null; 

  String unparse(ASTNode node, int level = 0) { 
    if (node) { 
      switch (node.class) { 
      case Statement:
      String label = label(node.statementLabel)
      return (label ? (label + ': ') : '') + statement(node, level)

      case Expression:
      return expression(node)

      case Parameter:
      return parameter(node)

      default:
      return node.text
      }
    }
    return null
  }

  String statement(Statement stmt, int level = 1) {
    if (stmt) { 
      switch (stmt.class) { 
      case BlockStatement:
	//info "[Unparser] unparse block statement ${stmt.statements.size()}"
	currentVariableScope = stmt.variableScope
	VariableScopeCategory.clearCurrentTypes(currentVariableScope)

	if (stmt.isEmpty()) { 
	  return '' 
	} else { 
	  def code = ''
	  if (stmt.statements.size() == 1 && 
	      stmt.statements[0] instanceof BlockStatement) { 
	    code = statement(stmt.statements[0])
	  } else { 
	    code = stmt.statements.collect{ s -> 
	      if (s instanceof BlockStatement) { 
		"""{
${indent(statement(s), 1)}
}"""
	      } else {  
		statement(s)
	      } 
	    }.join('\n')
	    if (level > 0) code = indent(code, level)
	  }
	  return code
	}

      case EmptyStatement:
	return ''

      case AssertStatement: 
	def cond = stmt.booleanExpression
	def msg = stmt.messageExpression
	return "assert ${cond ? expression(cond) : 'false'}${msg ? (': ' + expression(msg)): ''}"

      case BreakStatement:
	info "[Unparser] unparse break statement"
	return 'break' + (stmt.label ? " ${stmt.label}" : '') + statementTerminator()

      case ContinueStatement:
	return 'continue' + (stmt.label ? " ${stmt.label}" : '') + statementTerminator()

      case ExpressionStatement:
	return expression(stmt.expression) + statementTerminator()

      case ReturnStatement:
	return 'return' + (stmt.returningNullOrVoid ? '' : " ${expression(stmt.expression)}") + 
	       statementTerminator()

      case ThrowStatement:
	return "throw ${expression(stmt.expression)}" + statementTerminator()

      case IfStatement:
	def code = """if (${expression(stmt.booleanExpression)}) {
${statement(stmt.ifBlock)}
}""" 
	if (stmt.elseBlock) { 
	  if (stmt.elseBlock instanceof IfStatement) {  
	    code += " else ${statement(stmt.elseBlock)}" 
	  } else { 
	    code += """ else {
${statement(stmt.elseBlock)}
}""" 
	  }
	}
	return code 
      
      case WhileStatement:
	return """while (${expression(stmt.booleanExpression)}) {
${statement(stmt.loopBlock)}
}""" 

      case DoWhileStatement:
	return """do {
${statement(stmt.loopBlock)}
} while (${expression(stmt.booleanExpression)})""" 

      case ForStatement:
	currentVariableScope = stmt.variableScope
	VariableScopeCategory.clearCurrentTypes(currentVariableScope)
	def cexp = stmt.collectionExpression
	def code = ''
	if (cexp instanceof ClosureListExpression && 
	    cexp.expressions.size() == 3) { 
	  code = """for (${expression(cexp.expressions[0])}; ${expression(cexp.expressions[1])}; ${expression(cexp.expressions[2])}) {
${statement(stmt.loopBlock)}
}"""
	} else {  
	  code = """for (${parameter(stmt.variable)} in ${expression(cexp)}) {
${statement(stmt.loopBlock)}
}""" 
	}

	return code

      case SwitchStatement:
	return """switch (${expression(stmt.expression)}) {
${stmt.caseStatements.collect{ s -> statement(s) }.join('')}""" + 
(stmt.defaultStatement ? "default:\n${statement(stmt.defaultStatement)}" : '') + "\n}"

      case CaseStatement:
	return "case ${expression(stmt.expression)}: " + 
               (!stmt.code?.isEmpty() ? "\n${statement(stmt.code)}\n" : '')

      case TryCatchStatement:
       return """try {
${statement(stmt.tryStatement)}
} ${stmt.catchStatements.collect{ s -> statement(s) }.join(' ')}""" + 
(stmt.finallyStatement ? """ finally {
${statement(stmt.finallyStatement)}
}""" : '')

      case CatchStatement:
	return """catch (${parameter(stmt.variable)}) {
${statement(stmt.code)}
}"""

      case SynchronizedStatement:
	return """synchronized (${expression(stmt.expression)}) {
${statement(stmt.code)}
}"""

      case org.codehaus.groovy.classgen.BytecodeSequence:
	return "// ${stmt.text}"

      default:
	return stmt.text
      }
    }
    return null
  }

  String expression(Expression exp, ClassNode expectedType = null) { 
    if (exp) { 
      switch (exp.class) { 
      case EmptyExpression:
	return ''

      case ConstantExpression:
	if (exp.isEmptyStringExpression()) 
	  return makeString('')
	else if (exp.isTrueExpression()) 
          return trueLiteral
	else if (exp.isFalseExpression())
          return falseLiteral
	else if (exp.isNullExpression()) 
          return nullLiteral
	else if (exp.value instanceof String) 
          return makeString("${exp.value}")
	else { 
	  //info "[Unparser] Const ${exp.value} type=${exp.type} primitive=${ClassHelper.isPrimitiveType(exp.type)} number=${ClassHelper.isNumberType(exp.type)} unwrapper=${ClassHelper.getUnwrapper(exp.type)}"
	  String val = (exp.value?.toString() ?: '')
	  /*
	  if (exp.type == ClassHelper.BigDecimal_TYPE) { 
	    def t = reduceBigDecimal(exp)
	    if (t == ClassHelper.float_TYPE) { 
	      if (!val.endsWith('f') || !val.endsWith('F')) { 
		val += 'f'
	      }
	    }
	  } 
	  */
	  return val	  
	}

      case VariableExpression:
	if (exp.name) 
	  return owner.normalizeVariableName(exp.name)
	else 
	  return ''

      case MethodCallExpression:
	def obj = exp.objectExpression
	def objStr = obj ? "${expression(obj)}" : null
	if (objStr == 'this' && exp.implicitThis) objStr = null
	def mtd = exp.methodAsString
	if (!mtd) mtd = exp.method.text
	def args = exp.arguments
	def op = exp.spreadSafe ? '*.' : '.'
	return (objStr ? "${objStr}${op}" : '') + mtd + "(${expression(args)})"
	
      case StaticMethodCallExpression:
	return "${exp.ownerType}.${exp.method}" + "(${expression(exp.arguments)})"

      case ArgumentListExpression:
      case TupleExpression:
	if (exp.expressions && !exp.expressions.isEmpty()) 
	  return exp.expressions.collect{ e -> expression(e)}.join(', ')
	else  
	  return ''
	
      case DeclarationExpression:
	info "[Unparser] DeclarationExpression: right=${exp.rightExpression}"
	def var = exp.variableExpression 
	def initExp = null
	if (exp.rightExpression?.class != EmptyExpression) { 
	  initExp = expression(exp.rightExpression)
	}
	return (var.dynamicTyped ? 'def' : owner.typeName(var.originType)) + " ${var.name}" + 
	       (initExp ? (" = ${initExp}") : '')

      case BinaryExpression:
	return unparseBinaryExpression(exp)

      case BooleanExpression:
	return "${expression(exp.expression)}"

      case PostfixExpression:
	return "${expression(exp.expression)}${exp.operation.text}"
	
      case PrefixExpression:
	return "${exp.operation.text}${expression(exp.expression)}"

      case UnaryMinusExpression:
	return "-${expression(exp.expression)}"

      case UnaryPlusExpression:
	return "+${expression(exp.expression)}"

      case BitwiseNegationExpression:
	return "~${expression(exp.expression)}"

      case NotExpression:
	return "!${expression(exp.expression)}"

      case ElvisOperatorExpression:
	return "${expression(exp.booleanExpression)} ?: ${expression(exp.falseExpression)}"

      case TernaryExpression:
	return "${expression(exp.booleanExpression)} ? ${expression(exp.trueExpression)} : ${expression(exp.falseExpression)}"

      case FieldExpression:
	return (exp.fieldName ?: '')

      case AttributeExpression:
	def obj = exp.objectExpression
	return (obj && !exp.implicitThis ? "${expression(obj)}." : '') + "@${expression(exp.property)}"

      case PropertyExpression:
	def obj = exp.objectExpression
	def op = exp.safe ? '?.' : exp.spreadSafe ? '*.' : '.'
	return (obj && !exp.implicitThis ? "${expression(obj)}${op}" : '') + exp.propertyAsString
	//"${expression(exp.property)}"

      case ListExpression:
	return "[${exp.expressions.collect{ e -> expression(e)}.join(', ')}]"

      case MapEntryExpression:
	return "${expression(exp.keyExpression)} : ${expression(exp.valueExpression)}"

      case NamedArgumentListExpression:
      case MapExpression:
	return "[${exp.mapEntryExpressions.collect{ e -> expression(e)}.join(', ')}]"

      case CastExpression:
	return "(${exp.type}) ${expression(exp.expression)}"

      case ArrayExpression:
	return "new ${exp.type}[${exp.sizeExpression ? expression(exp.sizeExpression) : ''}] " + 
               "{ ${exp.expressions.collect{ e -> expression(e)}.join(', ')} }"

      case ConstructorCallExpression:
	if (exp.isSpecialCall()) { 
	  return "${exp.isSuperCall() ? 'super' : 'this'}(${expression(exp.arguments)})"
	} else {  
	  return "new ${owner.typeName(exp.type)}(${expression(exp.arguments)})"
	}

      case RangeExpression:
	return "${expression(exp.from)} ..${exp.inclusive ? '' : '<'} ${expression(exp.to)}"

      case ClosureExpression:
	return unparseClosureExpression(exp)

      case ClassExpression:
	return owner.normalizeTypeName(exp.type)

      case AnnotationConstantExpression:

      case ClosureListExpression:
      case GStringExpression:
      case MethodPointerExpression:
      case SpreadExpression:
      case SpreadMapExpression:

      default:
	return exp.text
      } 
    }
    return null
  }

  String unparseBinaryExpression(exp) {
    String op = exp.operation.text
    String left = expression(exp.leftExpression)
    String right = expression(exp.rightExpression)
    if (op == '[') { 
      return "${left}[${right}]"
    } else { 
      def (lp, rp) = OperatorPrecedence.needsParentheses(exp)
      if (lp) left = "(${left})"
      if (rp) right = "(${right})"
      return "${left} ${op} ${right}"
    }
  }

  String unparseClosureExpression(exp) { 
    Parameter[] params = exp.parameters
    if (params && params.length > 0) { 
      return "{ ${parameters(params)} -> ${unparse(exp.code, 0)} }"
    } else { 
      return "{ ${unparse(exp.code, 0)} }"
    }
  }

  String parameters(Parameter[] params) { 
    def pstr = ''
    if (params && params.length > 0) { 
      for (int i = 0; i < params.length; i++) { 
	def param = parameter(params[i])
	pstr += ((i > 0 ? ', ' : '') + param) 
      }
    }
    return pstr
  }

  String parameter(Parameter param) { 
    if (param != null) { 
      def binding = [ 'type' : owner.typeName(param.type),
		      'name' : param.name
		    ]
      def template = owner.engine.createTemplate(owner.templates.methodParameter).make(binding)
      return template.toString()
    }
    return null
  }

  String label(String l) { 
    return l
  }

  ClassNode getExpressionType(Expression exp) { 
    def result = null
    if (exp) {
      result = (exp instanceof Variable) ? getVariableType(exp) : exp.type
      if (ClassHelper.BigDecimal_TYPE == result) {
	if (exp.class == ConstantExpression)
	  result = reduceBigDecimal(exp)
	else if (exp.class == BinaryExpression && onlyContainsConstants(exp))
	  result = reduceBigDecimal(Eval.me(exp))
      }
    }
    return result
  }

  ClassNode getVariableType(Variable var) { 
    if (var) { 
      if (currentVariableScope) { 
	 Variable decl = currentVariableScope.getDeclaredVariable(var.name) 
	 info "[Unparser] DeclaredVar ${var.name} ${var.type} ${var.originType} ${decl?.type} " + 
	      " primitive=${ClassHelper.isPrimitiveType(var.type)}" +  
	      " primitive=${ClassHelper.isPrimitiveType(var.originType)}"  
	 if (decl) return decl.type 
      }
      return var.type
    }
    return null
  }

  String getTrueLiteral() { 
    'true'
  }

  String getFalseLiteral() { 
    'false'
  }

  String getNullLiteral() { 
    'null'
  }

  String makeString(String str) { 
    "'${str}'"
  }

  String statementTerminator() { 
    ''
  }

  static boolean isNullExpression(Expression exp) { 
    exp?.class == ConstantExpression && exp.isNullExpression() 
  }

  static String space(int n) { 
    (n > 0) ? ' ' * n : ''
  }

  static String tab = '  '

  static String indent(String code, int level = 1) { 
    return indent(code, level, tab)
  }

  static String indent(String code, int level, String tab) { 
    if (code && level > 0) { 
      String pad = tab * level
      code = pad + code.replaceAll('\n', '\n' + pad)
    }
    return code
  }

  static final String TEMP_NAME_PREFIX = '__$v'
  static int tempNameId = 1

  static String newName(String prefix = null) { 
    if (!prefix) prefix = TEMP_NAME_PREFIX
    return sprintf("${prefix}%04d", tempNameId++)
  }

  static void reInit() { 
    tempNameId = 1
  }
  
}
