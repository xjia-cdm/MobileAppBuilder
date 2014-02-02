package xj.translate.java

import java.util.ArrayList;

import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*

import static org.apache.commons.lang3.StringUtils.*

import xj.translate.*
import xj.translate.common.*
import xj.translate.typeinf.VariableScopeCategory
import xj.translate.typeinf.TypeInference
import xj.translate.typeinf.TypeCategory

import static xj.translate.common.ClassProcessor.*
import static xj.translate.typeinf.TypeUtil.*
import static xj.translate.typeinf.TypeInference.*
import static xj.translate.typeinf.VariableScopeCategory.*
import static xj.translate.Logger.* 

/**
 * An unparser of groovy AST. Output java source.
 * Handle statements and expressions 
 */

class UnparserJava extends Unparser { 

  String makeString(String str) { 
    return "\"${str}\""
  }

  String statementTerminator() { 
    return ';'
  }

  String statement(Statement stmt, int level = 1) {
    if (stmt) { 
      switch (stmt.class) { 
      case ReturnStatement:
	def exp = stmt.expression
	if (exp && exp instanceof MethodCallExpression) { 
	  def mtd = exp.methodAsString
	  if (mtd == "println" || mtd == 'print') { 
	    def tname = newName('__$s')
	    def args = exp.arguments.expressions
	    def argsStr = (args && !args.isEmpty()) ? ('\"\" + ' + args.collect{ e -> expression(e)}.join(' + ')) : ''
	    return "String ${tname} = ${argsStr}; System.out.${mtd}(${tname}); return ${tname};"
	  }       
	}
	return 'return' + (stmt.returningNullOrVoid ? '' : " ${expression(exp)}") + statementTerminator()       
	
      case ExpressionStatement:
	def expText = expression(stmt.expression) + statementTerminator()
	if (stmt.expression.class == BinaryExpression && 
	    !operators.isAssignmentOperator(stmt.expression.operation.text)) { 
	  def type1 = getExpressionType(stmt.expression.leftExpression)
	  def type2 = getExpressionType(stmt.expression.rightExpression)
	  if (ClassHelper.isNumberType(type1) && ClassHelper.isNumberType(type2)) { 
	    def type = commonType(type1, type2)
	    def tname = owner.typeName(type)
	    def vname = newName('__$n')
	    expText = tname + ' ' + vname + ' = ' + expText
	  }
	} 
	return expText
		
      case ForStatement:
	currentVariableScope = stmt.variableScope
	VariableScopeCategory.clearCurrentTypes(currentVariableScope)

	def cexp = stmt.collectionExpression
	if (cexp instanceof ClosureListExpression && 
	    cexp.expressions.size() == 3) { 
	  return """for (${expression(cexp.expressions[0])}; ${expression(cexp.expressions[1])}; ${expression(cexp.expressions[2])}) {
${statement(stmt.loopBlock)}
}"""
	} else { 
	  def var = stmt.variable.name
	  if (cexp instanceof RangeExpression) { 
	    //def type = getExpressionType(cexp.from)
	    def type = determineType(cexp.from)
	    def tname = owner.typeName(type)
	    Variable decl = currentVariableScope.getDeclaredVariable(var) 
	    if (decl) { 
	      decl.type = type
	    }
	    def op = cexp.inclusive ? '<=' : '<'
	    return """for (${tname} ${var} = ${expression(cexp.from)}; ${var} ${op} ${expression(cexp.to)}; ${var}++) {
${statement(stmt.loopBlock)}
}""" 
	  } else if (cexp instanceof ListExpression) { 
	    if (cexp.expressions.size > 0) { 
	      // enumerated list, To-Do: determine element type 
	      //def type = wrapSafely(getExpressionType(cexp.expressions[0]))
	      def type = wrapSafely(TypeInference.findCollectionType(cexp, currentVariableScope))
	      def tname = owner.typeName(type)
	      Variable decl = currentVariableScope.getDeclaredVariable(var) 
	      if (decl) { 
		decl.type = type
	      }
	      return unparseForLoopList(tname, var, cexp, stmt.loopBlock)
	    } else {  
	      // empty list 
	      
	    }
	  } else if (cexp instanceof MapExpression) { 
	    // enumerated map, To-Do: determine element (key value) type 
	    if (cexp.mapEntryExpressions.size > 0) { 
	      if (cexp.mapEntryExpressions[0] instanceof MapEntryExpression) {
		def keyType = wrapSafely(getExpressionType(cexp.mapEntryExpressions[0].keyExpression))
		def valueType = wrapSafely(getExpressionType(cexp.mapEntryExpressions[0].valueExpression))
		def ktname = owner.typeName(keyType)
		def vtname = owner.typeName(valueType)
		return unparseForLoopMap(ktname, vtname, var, cexp, stmt.loopBlock)
	      }
	    }
	  } else if (cexp instanceof ClassExpression) { 
	    if (cexp.type.isEnum()) { 
	      Variable decl = currentVariableScope.getDeclaredVariable(var) 
	      if (decl) { 
		decl.type = cexp.type
	      }
	      def tname = owner.typeName(cexp.type)
	      return unparseForLoopEnum(tname, var, cexp, stmt.loopBlock)
	    }
	  } else {

	  }  
	  return """for (${parameter(stmt.variable)} : ${expression(cexp)}) {
${statement(stmt.loopBlock)}
}""" 
	}

      case IfStatement:
	def pred = isTypePred(stmt.booleanExpression)
	if (pred && pred[2])
	  handleTypePredIfStatement(pred, stmt.ifBlock, currentVariableScope)
	def code = """if (${expression(stmt.booleanExpression)}) {
${statement(stmt.ifBlock)}
}""" 
	if (stmt.elseBlock) { 
	  if (pred && !pred[2])
	    handleTypePredIfStatement(pred, stmt.elseBlock, currentVariableScope)
	  if (stmt.elseBlock instanceof IfStatement) {  
	    code += " else ${statement(stmt.elseBlock)}" 
	  } else { 
	    code += """ else {
${statement(stmt.elseBlock)}
}""" 
	  }
	}
	return code 

      case SwitchStatement:
	return unparseSwitchStatement(stmt)

      case CaseStatement: 
	info "[UnparserJava] unparser case statement"
	return "case ${expression(stmt.expression)}: " + 
               (!stmt.code?.isEmpty() ? "\n${statement(stmt.code)}\n" : '')

      default:
	return super.statement(stmt, level)      
      }
    }
    return null
  }

  String unparseForLoopList(String tname, String var, ListExpression exp, Statement block) { 
    // (List<? extends ${tname}>) 
    return  """for (${tname} ${var} : ${expression(exp)}) {
${statement(block)}
}""" 
  }

  String unparseForLoopMap(String keyTypeName, String valueTypeName, String var, MapExpression exp, Statement block) { 
    addImportFile('java.util.Arrays') 
    addImportFile('java.util.Map') 
    addImportFile('java.util.List') 
    def tname = "Map.Entry<${keyTypeName}, ${valueTypeName}>"
    return """for (${tname} ${var} : Arrays.asList(${exp.mapEntryExpressions.collect{ e -> expression(e)}.join(', ')})) {
${statement(block)}
}""" 
  }

  String unparseForLoopEnum(String tname, String var, ClassExpression exp, Statement block) { 
    return """for (${tname} ${var} : ${tname}.values()) {
${indent(statement(block), 1)}
}""" 
  }

  String caseLabel(CaseStatement stmt, boolean classExp) {       
    def e = expression(stmt.expression)
    classExp ? "${e}.class" : e
  }

  String unparseSwitchStatement(SwitchStatement stmt) { 
    info "[UnparserJava] unparse switch statement"
    def type = getActualType(stmt.expression, currentVariableScope)
    if (isIntegerType(type)) { 
      return "switch (${expression(stmt.expression)}) {\n" +
	stmt.caseStatements.collect{ s -> statement(s) }.join('') + 
	(stmt.defaultStatement ? "default:\n${statement(stmt.defaultStatement)}" : '') + "\n}"
    } else { 
      String varname = isTypeExp(stmt.expression)
      addImportFile('java.util.Arrays') 
      def labels = "Arrays.asList(${stmt.caseStatements.collect{ s -> caseLabel(s, varname != null) }.join(', ')})"
      def code = "switch (${labels}.indexOf(${expression(stmt.expression)})) {\n"
      stmt.caseStatements.eachWithIndex{ s, i -> 
	if (varname) handleTypePredCaseStatement(varname, s, currentVariableScope)
	code += "case ${i}: " + (!s.code?.isEmpty() ? "\n${statement(s.code)}\n" : '')
      }
      code += (stmt.defaultStatement ? "default:\n${statement(stmt.defaultStatement)}" : '') + "\n}"
      return code
    }
  }

  String expression(Expression exp, ClassNode expectedType = null) { 
    if (exp) { 
      switch (exp.class) { 
      case DeclarationExpression:
	def var = exp.variableExpression
	def type = var.originType
	def tname = owner.typeName(type)
	def init = ''
	def rtype = null
	if (exp.rightExpression instanceof ClosureExpression) { 
	  return unparseClosureDeclaration(var, exp.rightExpression)
	} else { 
	  if (var.dynamicTyped) { 
	    Variable decl = currentVariableScope.getDeclaredVariable(var.name) 
	    if (decl) { 
	      tname = owner.typeName(decl.type)
	      type = decl.type
	    }
	  }      
	  if (exp.rightExpression?.class != EmptyExpression) { 
	    init = " = " + unparserInitExpression(tname, exp.leftExpression.type, exp.rightExpression)
	  }
	}

	if (var.dynamicTyped) { 
	  if (exp.rightExpression && 
	      exp.rightExpression.class != EmptyExpression && 
	      exp.rightExpression.type &&
	      !isNullExpression(exp.rightExpression)) {  
	    rtype = getActualType(exp.rightExpression, currentVariableScope)
	    if (rtype != type) {
	      info "[UnparserJava] DeclarationExpression: var ${var.name} is now ${rtype}"
	      VariableScopeCategory.setCurrentVariableType(currentVariableScope, var.name, rtype)
	    }
	  }
	}

	/****
	if (var.dynamicTyped) { 
	  Variable decl = currentVariableScope.getDeclaredVariable(var.name) 
	  if (decl) { 
	    if (exp.rightExpression?.type &&
		!isNullExpression(exp.rightExpression)) {  
	      rtype = getActualType(exp.rightExpression, currentVariableScope)
	      if (rtype != type) {
		// update declaration in the current variable scope (local var only)
		decl.type = rtype
	      }
	    }
	  }
	}
	*/

	owner.useTypeSetLocal.add(type)
	info "[UnparserJava] owner.useTypeSetLocal = ${owner.useTypeSetLocal} type = ${type}"
	return tname + " ${expression(var)}" + init	
	
      case MethodCallExpression:
	def mtd = exp.methodAsString
	if (!mtd) mtd = exp.method.text
	Expression obj = exp.objectExpression
	if (obj && exp.implicitThis && obj.text == 'this')  obj = null 
	Expression args = exp.arguments
	return unparseMethodCall(obj, mtd, args, exp.isSpreadSafe())

      case ConstructorCallExpression:
	return unparseConstructorCall(exp)

      case BinaryExpression:
	if (operators.isAssignmentOperator(exp.operation.text)) { 
	  return unparseAssignmentExpression(exp.leftExpression, exp.operation.text, exp.rightExpression)
	} else { 
	  return unparseBinaryExpression(exp.leftExpression, exp.operation.text, exp.rightExpression)
	}
	
      case PostfixExpression:
	//def type = exp.expression.type
	//def type = getExpressionType(exp.expression)
	def type = getActualType(exp.expression, currentVariableScope)
	if (ClassHelper.isNumberType(type)) { 
	  def result = "${expression(exp.expression)}${exp.operation.text}"
	  if (onlyContainsConstants(exp)) {
	    result = "${Eval.me(result)}"
	  }
	  return result
	} else if (ClassHelper.STRING_TYPE == type) { 
	  return operators.convertStringPostfixExpression(expression(exp.expression),
							  exp.operation.text) 
	} else {  
	  return operators.convertPostfixExpression(expression(exp.expression), 
						    exp.operation.text)
	}

      case PrefixExpression:
	//def type = exp.expression.type
	//def type = getExpressionType(exp.expression)
	def type = getActualType(exp.expression, currentVariableScope)
	if (ClassHelper.isNumberType(type)) { 
	  def result = "${exp.operation.text}${expression(exp.expression)}"
	  if (onlyContainsConstants(exp)) {
	    result = "${Eval.me(result)}"
	  }
	  return result
	} else if (ClassHelper.STRING_TYPE == type) { 
	  return operators.convertStringPrefixExpression(expression(exp.expression), 
							 exp.operation.text) 
	} else {  
	  return operators.convertPrefixExpression(expression(exp.expression), 
						   exp.operation.text)
	}

      case UnaryMinusExpression:
	def result = unparseUnaryExpression(exp.expression, '-')
	if (onlyContainsConstants(exp)) {
	  return "${Eval.me(result)}"
	}
	return result
	
      case UnaryPlusExpression:
	def result = unparseUnaryExpression(exp.expression, '+')
	if (onlyContainsConstants(exp)) {
	  result = "${Eval.me(result)}"
	}
	return result
	
      case BitwiseNegationExpression:
	return unparseUnaryExpression(exp.expression, '~')
	
      case ElvisOperatorExpression:
	def e1 = expression(exp.booleanExpression)
	def e2 = expression(exp.falseExpression)
	
	ClassNode t = exp.booleanExpression.expression.type
	//getActualType(exp.booleanExpression)
	
	info '[UnparserJava] ElvisOperatorExpression ' + exp.booleanExpression.expression + 
	        ' : ' + t //exp.booleanExpression.expression.type
	
	if (isBooleanType(t)) { 
	  return "${e1} ? ${e1} : ${e2}"	
	} else {  
	  String val = nullLiteral
	  if (isNumericalType(t)) { 
	    val = '0'
	  } 
	  return "${e1} != ${val} ? ${e1} : ${e2}"	
	}
	//if (owner.typeName(exp.booleanExpression.expression.type) == 'Integer') val = '0'
	
      case AttributeExpression:
	def obj = exp.objectExpression
	return (obj && !exp.implicitThis ? "${expression(obj)}." : '') + "${expression(exp.property)}"
	
      case PropertyExpression:
	def obj = exp.objectExpression
	ClassNode c = obj.type
	def pname = owner.normalizeVariableName(exp.propertyAsString)
	//FieldNode f = getDeclaredField(c, pname)
	FieldNode f = findFieldDecl(exp, currentVariableScope)

	if (f)
	  info "[UnparserJava] Unparse Property: ${f.name} type=${f.type} static=${f.static}" 
	else 
	  info "[UnparserJava] Unparse Property: ${exp.propertyAsString} not found" 

	return unparsePropertyExpression(exp.implicitThis ? null : obj, pname, c, f,
					 exp.safe, exp.spreadSafe)

      case ListExpression:
	addImportFile('java.util.Arrays') 
	def type = getActualType(exp, currentVariableScope)
	GenericsType[] gen = type.getGenericsTypes() 
	def gentype = ''
	if (gen.size() > 0 && gen[0] != ClassHelper.OBJECT_TYPE) { 
	  //gentype = "<${owner.typeName(gen[0].type)}>"
	  gentype = "<${owner.typeName(gen[0].name, false)}>"
	} 
	return "Arrays.${gentype}asList(${exp.expressions.collect{ e -> expression(e)}.join(', ')})"

      case MapEntryExpression:
	addImportFile('java.util.AbstractMap') 
	return "new AbstractMap.SimpleEntry(${expression(exp.keyExpression)}, ${expression(exp.valueExpression)})"

      case MapExpression:
	owner.addUtilFile('Maps')
	addImportFile('org.javax.Maps')
	return "Maps.makeMap(${exp.mapEntryExpressions.collect{ e -> kvpair(e)}.join(', ')})"

      case RangeExpression: 
	owner.addUtilFile('Ranges')
	addImportFile('org.javax.Ranges')
	return "Ranges.makeRange(${expression(exp.from)}, ${expression(exp.to)}, ${exp.inclusive})"
 	
      default:
	return super.expression(exp, expectedType)
      }
    }
  }

  def kvpair(Expression e) { 
    "${expression(e.keyExpression)}, ${expression(e.valueExpression)}"
  }

  String unparseMethodCall(Expression obj, String mtd, Expression args, boolean spread) { 
    def objStr = obj ? expression(obj) : null
    if (objStr == 'this')
      objStr = null
    def type = null;
    Variable decl = null
    if (objStr && hasNameProperty(obj)) {
      type = VariableScopeCategory.getCurrentVariableType(currentVariableScope, obj.name)
      //decl = currentVariableScope.getDeclaredVariable(obj.name) 
      decl = getVariableDecl(obj.name, currentVariableScope)
      //******
      //if (decl) type = decl.type

      if (decl && decl.dynamicTyped && decl.type != type) { 
	objStr = "((${type}) ${objStr})" 	    
      }
    }
    
    if (objStr == null) { 
      if (mtd == "println" || mtd == 'print')
	mtd = "System.out." + mtd
    }    
    objStr = (objStr ? objStr + '.' : '') 
    def argtext = ''
    if (args && !args.expressions.isEmpty()) { 
      MethodInfo minfo = findMethodDef(obj, mtd, args.expressions, false, currentVariableScope); 
      MethodNode m = minfo?.method
      if (m) { 
	argtext = unparseArguments(m.parameters, minfo.overloaded, args)
      } else { 
	argtext = expression(args)
      }
    }
    if (!spread) { 
      return "${objStr}${mtd}(${argtext})"
    } else { 
      return "${objStr}collect { c -> c.${mtd}(${expression(args)}) }"
    }
  }

  String unparseConstructorCall(ConstructorCallExpression exp) {  
    def args = exp.arguments 
    if (exp.isSpecialCall()) { 
      return "${exp.isSuperCall() ? 'super' : 'this'}(${expression(exp.arguments)})"
    } else {  
      def cname = owner.typeName(exp.type)
      boolean isInnerClass = owner.hasInnerClass(exp.type)

      def arguments = args?.expressions
      if (isInnerClass && 
	  arguments != null && arguments.size() > 0 && expression(arguments[0]) == 'this') { 
	arguments = arguments.tail()
      }
      def argtext = ''
      if (arguments) { 
	MethodInfo minfo = findConstructorDef(exp.type, args)
	ConstructorNode c = minfo?.method
	if (c) { 
	  argtext = unparseArguments(c.parameters, minfo.overloaded, args)
	} else { 
	  argtext = arguments.collect{ e -> expression(e)}.join(', ')
	}
      }
      return "new ${cname}(${argtext})"
    }	
  }

  // used in method call
  String unparseArguments(Parameter[] params, boolean overloaded, Expression args) { 
    def argstext = ''
    def len = args.expressions.size()
    for (int i = 0; i < len; i++) { 
      def exp = args.expressions[i]
      if (i > 0) { 
	argstext += ', '
      }
      def cast = ''
      if (i < params.length) { 
	def param = params[i]
	def type = determineType(exp, currentVariableScope)
	if (param.type != type && 
	    !isSuperType(param.type, type)) {
	  cast = "(${owner.typeName(param.type)}) "
	}
      }      
      argstext += (cast + expression(exp))
    }
    return argstext
  }

  String unparseClosureDeclaration(VariableExpression var, ClosureExpression exp) { 
    if (var && exp) { 
      def vscope = null, v = null
      (vscope, v) = findDeclaredVariableScope(var.name, currentVariableScope)
      if (vscope) { 
	ClosureInfo cinfo = VariableScopeCategory.getClosureInfo(vscope, var.name)
	//info "unparseClosureDeclaration ${cinfo?.myCopies.size()}"
	if (cinfo &&
	    cinfo.closure.is(exp) &&
	    cinfo.myCopies.size() == 1) {
	  exp = cinfo.myCopies[0]
	}
      }

      def tname = newName('__$Closure')
      def type = ClassHelper.make(tname)
      tname = owner.typeName(type)
      return "${tname} ${var.name} = ${unparseClosureExpression(exp, tname)}"
    }
    return null
  }

  String unparserInitExpression(String tname, ClassNode type, Expression exp) { 
    if (exp) { 
      if (exp.class == ConstantExpression && 
	  exp.isNullExpression() && 
	  isNumericalType(type)) {
	if (tname == "java.math.BigDecimal")
	  return "null"
	else
	  return "0"
      } else if (tname == "java.math.BigDecimal" && 
		 exp.class == BinaryExpression && 
		 onlyContainsConstants(exp)) {
	//Use Eval in case the result is something like "2.3 + 32.12"
	addImportFile('java.math.BigDecimal') 
	return "new BigDecimal(\"${Eval.me(expression(exp))}\")"
      } else { 
	return "${expression(exp)}"
      }
    }
    return null
  }

  String unparsePropertyExpression(Expression obj, String pname, 
				   ClassNode type, FieldNode f,
				   boolean safe, boolean spread) { 
    def op = safe ? '?.' : spread ? '*.' : '.'
    def objStr = (obj ? "${expression(obj)}${op}" : '') 
    if (pname == 'this') {  // || pname == 'class') { 
      return objStr + pname
    } else if (f && f.static && f.final) { 
      return objStr + f.name
    } else { 
      return objStr + "get${capitalize(pname)}()"
    }
  }

  String unparseAssignmentExpression(Expression left, String op, Expression right) { 
    if (operators.isAssignmentOperator(op) &&
	left instanceof PropertyExpression) { 
      def obj = left.objectExpression
      def op1 = left.safe ? '?.' : left.spreadSafe ? '*.' : '.'
      if (op == '=') { 
	return (obj && !left.implicitThis ? "${expression(obj)}${op1}" : '') + 
	       "set${capitalize(left.propertyAsString)}(${expression(right)})"
      } else {  
	def op2 = op.substring(0, op.length() - 1)
	return (obj && !left.implicitThis ? "${expression(obj)}${op1}" : '') + 
	       "set${capitalize(left.propertyAsString)}(${expression(left)} ${op2} ${expression(right)})" 
      }
    } else {
      if (op == "=" && 
	  left.hasProperty('name')) {
	Variable decl = currentVariableScope.getDeclaredVariable(left.name) 
	if (decl) {
	  info "[UnparserJava] var ${left.name} : ${decl.type}  -- dynamictyped ${decl.dynamicTyped}"
	  def ltype = decl.dynamicTyped ? decl.type : decl.originType
	  def rtype = getActualType(right, currentVariableScope)
	  if (decl.dynamicTyped && 
	      !isNumericAssignableType(ltype, rtype)) { 
	      //!isDirectlyAssignableFrom(ltype, rtype)) {
	     
	    ltype = getActualType(right, currentVariableScope)
	    info "[UnparserJava] AssignmentExpression: var ${left.name} is now ${decl.type}"
	    VariableScopeCategory.setCurrentVariableType(currentVariableScope, left.name, ltype)
	    //********
	    //ltype = decl.type = getActualType(right, currentVariableScope)
	  } 
	  if (ltype == rtype || 
	      isSuperType(ltype, rtype)) {
	    return "${expression(left)} = ${expression(right)}"
	  } else {  
	    return "${expression(left)} = (${owner.typeName(ltype)}) (${expression(right)})"
	  }
	}
      }
      return unparseBinaryExpression(left, op, right)
    }
  }

  boolean isNumericBinaryExpression(Expression exp) { 
    if (exp && exp.class == BinaryExpression) { 
      def type1 = getExpressionType(exp.leftExpression)
      def type2 = getExpressionType(exp.rightExpression)
      return (ClassHelper.isNumberType(type1) && ClassHelper.isNumberType(type2)) 
    }
    return false 
  }

  boolean isConstant(Expression exp) { 
    return exp && exp.class == ConstantExpression
  }

  String unparseListBinaryExpression(Expression left, String op, Expression right,
				    boolean lp, boolean rp) { 
    operators.convertListBinaryExpression(expression(left), op, expression(right), lp, rp)
  }

  String unparseMapBinaryExpression(Expression left, String op, Expression right,
				    boolean lp, boolean rp) { 
    operators.convertMapBinaryExpression(expression(left), op, expression(right), lp, rp)
  }

  String unparseBinaryExpression(Expression left, String op, Expression right) { 
    def type1a = getActualType(left, currentVariableScope)
    def type2a = getActualType(right, currentVariableScope)
    def type1 = determineType(left, currentVariableScope)
    def type2 = determineType(right, currentVariableScope)
 
    info "[UnparserJava] Unparse: ${left.text} ${op} ${right.text} : ${type1} ${type2}"
    if (type1 != type1a || type2 != type2a) { 
      info "[UnparserJava] !!!!!! actual type: ${left.text} ${op} ${right.text} : ${type1a} ${type2a}"
    }

    String lhs = expression(left)
    String rhs = expression(right)
    def (lp, rp) = OperatorPrecedence.needsParentheses(left, op, right)

    if (isListType(type1)) {
      return unparseListBinaryExpression(left, op, right, lp, rp)	
    } else if (isMapType(type1)) {
      return unparseMapBinaryExpression(left, op, right, lp, rp)	
    } else if (isNumericalType(type1) && isNumericalType(type2)) { 
      if (isBigNumber(type1) || isBigNumber(type2)) { 
	if (isBigDecimal(type1) || isBigDecimal(type2)) { 
	  if (isConstant(left) || !isBigDecimal(type1)) { 
	    lhs = newBigDecimal(lhs)
	  } 
	  if (isConstant(right) || !isBigDecimal(type2)) { 
	    rhs = newBigDecimal(rhs)
	  } 
	  return operators.convertBigDecimalBinaryExpression(lhs, op, rhs, lp, rp)
	} else { 
	  if (isConstant(left) || !isBigInteger(type1)) { 
	    lhs = newBigInteger(lhs)
	  } 
	  if (isConstant(right) || !isBigInteger(type2)) { 
	    rhs = newBigInteger(rhs)
	  } 
	  return operators.convertBigIntegerBinaryExpression(lhs, op, rhs, lp, rp)
	}
      } else { 
	return operators.convertNumericBinaryExpression(lhs, op, rhs, lp, rp)
      }
    } else if (op == '==' &&
	       left instanceof PropertyExpression &&
	       left.propertyAsString == 'class' &&
	       right instanceof ClassExpression) { 
      // obj.class == X ==> obj.getClass().getName().equals("X")
      return operators.convertTypePredicate(expression(left.objectExpression), lhs, rhs)
    } else if (ClassHelper.STRING_TYPE == type1 && 
	       ClassHelper.STRING_TYPE == type2) { 
      return operators.convertStringBinaryExpression(lhs, op, rhs, lp, rp)
    } else if (ClassHelper.STRING_TYPE == type1) { 
      if (op != '*') { 
	rhs = explicitToString ? convertToString(right) : rhs
      }
      return operators.convertStringBinaryExpression(lhs, op, rhs, lp, rp)						     
    } else if (ClassHelper.STRING_TYPE == type2) {
      return operators.convertStringBinaryExpression(convertToString(left), op, rhs, lp, rp)
    } else {  
      /*
      if (op == '=' && 
	  !isAssignableFrom(type1, type2)) { 
	return "${expression(left)} = (${owner.typeName(type1)}) (${expression(right)})"
      }	
      */
      return operators.convertBinaryExpression(lhs, op, rhs, lp, rp)
    }
  }

  String newBigDecimal(val) { 
    addImportFile('java.math.BigDecimal') 
    return "new BigDecimal(${val})"
  }

  String newBigInteger(val) { 
    addImportFile('java.math.BigInteger') 
    return "new BigInteger(${val})"
  }

  String convertToString(Expression exp) { 
    if (exp) { 
       def type = getExpressionType(exp)
       def text = expression(exp)
       if (ClassHelper.STRING_TYPE != type) { 
	 if (ClassHelper.isPrimitiveType(type)) { 
	   def t = ClassHelper.getWrapper(type)
	   def tname = owner.typeName(t)
	   return "${tname}.toString(${text})"
	 } else { 
	   return text + '.toString()'
	 }
       }
       return text
    }
    return ''
  }

  private String unparseUnaryExpression(Expression exp, String op) { 
    //def type = exp.type
    def type = getExpressionType(exp)
    if (ClassHelper.isNumberType(type) && ClassHelper.isPrimitiveType(type)) { 
      return "${op}${expression(exp.expression)}"
    } else { 
      return operators.convertPrefixExpression(expression(exp), op) 
    }
  }

  String unparseClosureExpression(ClosureExpression exp, String cname = null) { 
    if (!cname) cname = newName('__$Closure')
    def type = null
    use (TypeCategory) { 
      type = exp.getActualType()
    }
    def tname = type ? owner.typeName(type) : 'Object'
    def binding = [ 'name'   : cname,
		    'type'   : tname, 
		    'params' : parameters(exp.parameters),
		    'code'   : unparse(exp.code, 3)
		  ]
    def template = owner.engine.createTemplate(owner.templates.closureDef).make(binding)      
    owner.innerClassDefScrap += template.toString()

    return "new $cname()"
  }

  def addImportFile(f) { 
    //ModuleProcessor.currentClassProcessor.addImportFile(f) 
    owner.addImportFile(f) 
  }

}
