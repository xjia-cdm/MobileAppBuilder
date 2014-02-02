package xj.translate.objc

import java.text.MessageFormat as mf

import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*

import static org.codehaus.groovy.ast.ClassHelper.*

import xj.translate.java.UnparserJava
import xj.translate.typeinf.VariableScopeCategory 
import xj.translate.typeinf.TypeCategory 
import xj.translate.common.OperatorPrecedence

import static xj.translate.common.ClassProcessor.*
import static xj.translate.objc.ObjectiveCClassProcessor.*
import static xj.translate.objc.ObjectiveCAPIMapper.*
import static xj.translate.Logger.* 
import static xj.translate.typeinf.TypeUtil.* 
import static xj.translate.typeinf.TypeInference.* 
import static xj.translate.typeinf.VariableScopeCategory.* 

/**
 * An unparser of groovy AST. Output Objective-C source.
 * Handle statements and expressions 
 */

class UnparserObjectiveC extends UnparserJava { 

  UnparserObjectiveC() { 
    operators = new OperatorsObjectiveC()
    explicitToString = true
  }

  String statement(Statement stmt, int level = 1) {
    if (stmt) { 
      switch (stmt.class) { 	
      case ThrowStatement:
	return "@throw ${expression(stmt.expression)}" + statementTerminator()

      case TryCatchStatement:
	return """@try {
${statement(stmt.tryStatement)}
} ${stmt.catchStatements.collect{ s -> statement(s) }.join(' ')}""" + 
(stmt.finallyStatement ? """ @finally {
${statement(stmt.finallyStatement)}
}""" : '')

      case CatchStatement:
	return """@catch (${owner.typeName(stmt.variable.type)} ${stmt.variable.name}) {
${statement(stmt.code)}
}"""

      case SynchronizedStatement:
	return """@synchronized (${expression(stmt.expression)}) {
${statement(stmt.code)}
}"""

      default:
	return super.statement(stmt, level)      
      }
    }
    return null
  }

  String unparseForLoopList(String tname, String var, ListExpression exp, Statement block) { 
    return """for (${tname} ${var} in ${expression(exp)}) {
${statement(block)}
}""" 
  }

  String unparseForLoopMap(String keyTypeName, String valueTypeName, String var, MapExpression exp, Statement block) { 
    return """NSDictionary *_dictionary = [NSDictionary dictionaryWithObjectsAndKeys: ${exp.mapEntryExpressions.collect{ e -> expression(e)}.join(', ')}, nil];
for (${keyTypeName} _key in _dictionary) {
  NSArray* ${var} = [NSArray arrayWithObjects: _key, [_dictionary objectForKey:_key], nil];
${statement(block)}
}""" 
  }

  String unparseForLoopEnum(String tname, String var, ClassExpression exp, Statement block) { 
    def ecp = ObjectiveCModuleProcessor.getClassProcessor(exp.type.nameWithoutPackage)
    def min = '', max = ''
    if (ecp) { 
      min = ecp.enumMinValue
      max = ecp.enumMaxValue
    }
    return """for (${tname} ${var} = ${min}; ${var} <= ${max}; ${var}++) {
${indent(statement(block), 1)}
}""" 
  }

  String caseLabel(CaseStatement stmt, boolean classExp) {       
    def e = expression(stmt.expression)
    classExp ? "[${e} class]" : e
  }

  String unparseSwitchStatement(SwitchStatement stmt) { 
    info "[UnparserObjectiveC] unparse switch statement"
    def type = getActualType(stmt.expression, currentVariableScope)
    if (isIntegerType(type)) { 
      return "switch (${expression(stmt.expression)}) {\n" +
	stmt.caseStatements.collect{ s -> statement(s) }.join('') + 
	(stmt.defaultStatement ? "default:\n${statement(stmt.defaultStatement)}" : '') + "\n}"
    } else { 
      String varname = isTypeExp(stmt.expression)
      def labels = "[NSArray arrayWithObjects: ${stmt.caseStatements.collect{ s -> caseLabel(s, varname != null) }.join(', ')}, nil]"
      def code = "switch ([${labels} indexOfObject: ${expression(stmt.expression)}]) {\n"
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
      case VariableExpression:
	info "[UnparserObjectiveC] unparse VariableExpression ${exp}"
	if (exp.name) { 
	  def varname = owner.normalizeVariableName(exp.name)
	  info "[UnparserObjectiveC] unparse VariableExpression ${exp} -> ${varname}"
	  def (vscope, var) = findDeclaredVariableScope(varname, currentVariableScope)
	  if (!var) { 
	    def c = owner.classNode
	    if (c?.getField(varname) == null && 
		c?.getOuterField(varname) != null) { 
	      varname = "[this\$0 ${varname}]"
	    }
	  }
	  return varname 
	} else { 
	  return ''
	}

      case ListExpression:
	return "(NSMutableArray*) [NSMutableArray arrayWithObjects: ${exp.expressions.collect{ e -> toNSObject(e)}.join(', ')}, nil ]"

      case MapEntryExpression:
	//addImportFile('java.util.AbstractMap') 
	return "${toNSObject(exp.keyExpression)}, ${toNSObject(exp.valueExpression)}"

      case MapExpression:
	return "[NSMutableDictionary dictionaryWithObjectsAndKeys: ${exp.mapEntryExpressions.collect{ e -> kvpair(e)}.join(', ')}, nil ]"

      case RangeExpression: 
	owner.addUtilFile('Range')
	return "rangeWithIntegers(${expression(exp.from)}, ${expression(exp.to)}, ${exp.inclusive})"

      case ElvisOperatorExpression:
	return super.expression(exp, expectedType)

      case TernaryExpression:
	def type = getActualType(exp, currentVariableScope)
	def trueType = getActualType(exp.trueExpression, currentVariableScope)
	def falseType = getActualType(exp.falseExpression, currentVariableScope)	

	String trueExp, falseExp
	if (!ClassHelper.isPrimitiveType(type) && 
	    ClassHelper.isPrimitiveType(trueType)) { 
	  trueExp = toNSObject(exp.trueExpression)
	} else { 
	  trueExp = expression(exp.trueExpression)
	}
	if (!ClassHelper.isPrimitiveType(type) && 
	    ClassHelper.isPrimitiveType(falseType)) { 
	  falseExp = toNSObject(exp.falseExpression)
	} else { 
	  falseExp = expression(exp.falseExpression)
	}	
	return "${expression(exp.booleanExpression)} ? ${trueExp} : ${falseExp}"

      default:
	return super.expression(exp, expectedType)
      }
    }
  }

  def kvpair(Expression e) { 
    "${toNSObject(e.keyExpression)}, ${toNSObject(e.valueExpression)}"
  }

  List<String> unparseArguments(Expression args) { 
    if (args?.expressions?.size() > 0) {       
      return args.expressions.collect { e -> expression(e) }
    }
    return null
  }

  String unparseMethodCall(Expression obj, String mtd, Expression args, boolean spread) { 
    info "[UnparserObjectiveC] unparseMethodCall ${mtd}"
    if (obj instanceof ClassExpression) { 
      owner.useTypeSetLocal.add(obj.type)
    }

    def objStr = obj ? expression(obj) : null
    if (objStr == 'self')
      objStr = null

    def type = null  //obj ? getActualType(obj, currentVariableScope) : null
    Variable decl = null
    if (objStr && hasNameProperty(obj)) {
      type = VariableScopeCategory.getCurrentVariableType(currentVariableScope, obj.name)
      //decl = currentVariableScope.getDeclaredVariable(obj.name) 

      decl = getVariableDecl(obj.name, currentVariableScope)
      if (decl && decl.dynamicTyped && decl.type != type) { 
	objStr = "((${owner.typeName(type)}) ${objStr})" 	    
      }
    }

    def argstext = ''
    if (objStr == null) { 
      if (mtd == 'println' || mtd == 'print') { 
	// To-do: need to format based on type 
	//        handle printf in Java
	def pat = ''
	if (args.expressions && !args.expressions.isEmpty()) { 
	  ( pat, argstext ) = unparsePrintArgs(args.expressions, mtd == 'println')
	}
	return "printf(\"${pat}\"" + (argstext ? ", ${argstext}" : '') + ")"
      }
    }

    boolean isStatic = false
    if (obj instanceof ClassExpression) { 
      type = obj.type
      isStatic = true
    } 
    if (!type) { 
      type = obj ? getActualType(obj, currentVariableScope) : null
    }
    List<String> arglist = unparseArguments(args)
    String mcall = mapCall(objStr, type?.name, mtd, isStatic, arglist)
    if (mcall) return mcall 

    if (objStr == null) { 
      objStr = 'self'
    }

    if (args && !args.expressions.isEmpty()) { 
      MethodInfo minfo = findMethodDef(obj, mtd, args.expressions, false, currentVariableScope); 
      MethodNode m = minfo?.method
      info "[UnparserObjectiveC] unparseMethodCall ${mtd} findMethodDef: ${m!=null}"
      if (m) { 
	argstext = unparseArguments(m.parameters, minfo.overloaded, args)
      } else { 
	argstext = arglist.join(', ')

	if (obj instanceof Variable && mtd == 'call') { 
	  ClosureInfo cInfo = findClosureDef(obj.name, args.expressions, currentVariableScope)
	  ClosureExpression closure = cInfo?.closure
	  if (closure) { 
	    return "${obj.name}(${argstext})"
	  }
	}
	
	argstext = ':' +  argstext 
      }
    } 

    if (!spread) { 
      return "[${objStr} ${mtd}${argstext}]"    
    } else { 
      return "[${objStr} collect { c -> [c ${mtd}:${expression(args)}] }]"
    }
  }

  private static breakAddition(Expression e) { 
    if (e instanceof BinaryExpression && 
	e.operation.text == '+' &&
	e.leftExpression instanceof ConstantExpression && 
	e.leftExpression.type == STRING_TYPE) { 
      [ breakAddition(e.leftExpression), breakAddition(e.rightExpression) ]
    } else { 
      e
    }
  }

  // return string pair [pattern, arguments]
  def unparsePrintArgs(List exps, boolean newline = false) { 
    def pattern = ''
    def arguments = ''
    if (exps && !exps.isEmpty()) { 
      def elist = exps.collect { e -> breakAddition(e) }.flatten()
      //info "  unparsePrintArgs: elist=${elist}"
      def args = []
      pattern = elist.collect{ e -> 
	if (e instanceof ConstantExpression && 
	    e.type == STRING_TYPE) { 
	  e.text
	} else { 
	  def ta = getActualType(e, currentVariableScope)
	  def t = determineType(e, currentVariableScope)
	  if (t != ta) { 
	    info "[UnparserObjectiveC] !!!!!! type: ${e}: ${t} --- ${ta}"
	  }

	  def etext = expression(e)
	  info "[UnparserObjectiveC] unparsePrintArgs: etext=${etext} t=${t.name}"
	  if (etext) { 
	    // add necessary downcast 
	    Variable decl = null
	    boolean dynamicTypeVar = false
	    if (hasNameProperty(e)) {
	      decl = currentVariableScope.getDeclaredVariable(e.name) 
	      if (decl && decl.dynamicTyped && decl.type != t) { 
		dynamicTypeVar = true
		if (isNumericalType(t)) { 
		  etext = "((NSNumber*) ${etext})" 	    
		} else { 
		  etext = "((${owner.owner.typeName(t)}) ${etext})" 	    
		}
	      }
	    }

	    if (t == STRING_TYPE ) { 
	      args << "[${etext} UTF8String]" 
	    } else if (isEnum(t)) { 
	      args << etext 
	    } else if (isNumericalType(t)) { 
	      if (ClassHelper.isPrimitiveType(t) && !dynamicTypeVar) { 
		args << etext 
	      } else { 
		def tname = getUnwrapper(t).nameWithoutPackage
		args << "[${etext} ${tname}Value]"
	      }
	    } else if (isBooleanType(t)) { 
	      if (ClassHelper.isPrimitiveType(t)) { 
		args << etext 
	      } else { 
		args << "[${etext} boolValue]"
	      }
	    } else { 
	      args << "[[${etext} description] UTF8String]"
	    }
	  } else {  
	    args << '""'
	  }
	  getFormatPattern(t)
	}
      }.join('')
      arguments = args.join(', ')
    }
    if (newline) pattern += '\\n'
    return [pattern, arguments]
  }

  // used in method call
  String unparseArguments(Parameter[] params, boolean overloaded, Expression args) { 
    def argstext = ''
    if (params && params.length == args.expressions.size()) {       
      for (int i = 0; i < params.length; i++) { 
	def param = params[i]
	def exp = args.expressions[i]
	def cast = ''
	def type = determineType(exp, currentVariableScope)
	if (param.type != type && 
	    !isSuperType(param.type, type)) {
	  cast = "(${owner.typeName(param.type)}) "
	}

	if (overloaded) {  
	  def tname = owner.JavaTypeName(param.type)
	  int k = tname.lastIndexOf('.')
	  if (k >= 0) tname = tname.substring(k + 1)
	  tname = tname.replaceAll('<', '')
	  tname = tname.replaceAll('>', '')
	  tname = tname.replaceAll(',', '')
	  tname = tname.replaceAll(' ', '')
	  tname = tname.capitalize()
	  if (i == 0) {
	    argstext += 'With' 
	  } else { 
	    argstext += ' and'
	  }
	  argstext += (tname + ':' + cast + expression(exp)) 
	} else { 
	  def pname = params[i].name
	  if (i == 0) { 
	    if (params.length > 1)  
	      pname = pname.capitalize() 
	    else 
	      pname = ''
	  } else { 
	    argstext += ' '
	  }
	  argstext += (pname + ':' + cast + expression(exp)) 
	}
      }
    } else { 
      argstext = ':' + expression(args)
    }
    return argstext
  }

  String unparseConstructorCall(ConstructorCallExpression exp) {  
    def args = exp.arguments
    def argstext = ''
    if (args && !args.expressions.isEmpty()) { 
      argstext = ':' + expression(args)
    } 
    if (exp.isSpecialCall()) { 
      return "${exp.isSuperCall() ? '[super ' : '[self '}init${argstext}]"
    } else {  
      def tname = owner.typeName(exp.type)
      if (tname.endsWith('*'))
	tname = tname.substring(0, tname.length() - 1)

      if (args && !args.expressions.isEmpty()) { 
	def argtypes = args.expressions.collect { e -> getActualType(e, currentVariableScope) }
	def ctor = owner.findCreator(tname, argtypes)
	if (ctor) { 
	  def cargs = mf.format(ctor, args.expressions.collect { e -> expression(e) } as String[])
	  return "[${tname} ${cargs}]"
	} else { 
	  MethodInfo minfo = findConstructorDef(exp.type, args)
	  ConstructorNode c = minfo?.method
	  if (c) argstext = unparseArguments(c.parameters, minfo.overloaded, args)
	  return "[[${tname} alloc] init${argstext}]"
	}
      } 
      return "[[${tname} alloc] init]"
    }
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

      def type = null
      use (TypeCategory) { 
	type = exp.getActualType()
      }
      def tname = type ? owner.typeName(type) : 'id'
      return "${tname}(^${expression(var)})(${closureParameters(exp.parameters)}) = ${unparseClosureExpression(exp)}"
    }
    return null
  }

  String unparserInitExpression(String tname, ClassNode type, Expression exp) { 
    if (exp) { 
      if (exp.class == ConstantExpression && 
	  exp.isNullExpression() && 
	  isNumericalType(type)) {
	if (isObjectType(tname))
	  return "nil"
	else
	  return "0"
      } else if (isNSNumberType(tname) && 
		 exp.class == BinaryExpression && 
		 onlyContainsConstants(exp)) {
	//Use Eval in case the result is something like "2.3 + 32.12"
	return "[NSNumber numberWithDouble:${Eval.me(expression(exp))}]"
      } else { 
	if (isNSNumberType(tname)) { 
	  return "${toNSNumber(exp)}"
	} else { 
	  return "${expression(exp)}"
	}
      }
    }
    return null
  }

  String unparsePropertyExpression(Expression obj, String pname, 
				   ClassNode type, FieldNode f,
				   boolean safe, boolean spread) { 
    //def op = safe ? '?.' : spread ? '*.' : '.'
    if (f && f.static) { 
      owner.useTypeSetLocal.add(type)
      if (f.final) { 
	return f.name
      } else {  
	def tname = owner.typeName(type)
	if (tname.endsWith('*'))
	  tname = tname.substring(0, tname.length() - 1)
	return "[${tname} ${pname}]"
      }
    } else { 
      //def objStr = (obj && ? "${expression(obj)}${op}" : '') 
      //return objStr + pname
      if (obj instanceof VariableExpression) { 
	return "${expression(obj)}.${pname}"
      } else { 
	return "[${expression(obj)} ${pname}]"
      }
    }
  }

  String unparseListBinaryExpression(Expression left, String op, Expression right,
				     boolean lp, boolean rp) { 
    def rtext = (op in [ '[' ]) ? expression(right) : toNSObject(right)
    operators.convertListBinaryExpression(expression(left), op, rtext, lp, rp)
  }

  String unparseMapBinaryExpression(Expression left, String op, Expression right,
				    boolean lp, boolean rp) { 
    operators.convertMapBinaryExpression(expression(left), op, toNSObject(right), lp, rp)
  }

  String unparseAssignmentExpression(Expression left, String op, Expression right) { 
    if (operators.isAssignmentOperator(op) &&
	left instanceof PropertyExpression) { 
      def obj = left.objectExpression
      def op1 = left.safe ? '?.' : left.spreadSafe ? '*.' : '.'
      return (obj && !left.implicitThis ? "${expression(obj)}${op1}" : '') + 
             "${left.propertyAsString} ${op} ${expression(right)}"
    } else {
      if (op == "=" && 
	  left.hasProperty('name')) {
	Variable decl = currentVariableScope.getDeclaredVariable(left.name) 
	if (decl) {
	  info "[UnparserObjectiveC] var ${left.name} : ${decl.type}  -- dynamictyped ${decl.dynamicTyped}"
	  def ltype = decl.dynamicTyped ? decl.type : decl.originType
	  def rtype = getActualType(right, currentVariableScope)
	  if (decl.dynamicTyped && 
	      !isNumericAssignableType(ltype, rtype)) { 
	      //!isSuperType(ltype, rtype)) {
	     
	    ltype = getActualType(right, currentVariableScope)
	    info "[UnparserObjectiveC] var ${left.name} is now ${ltype}"
	    VariableScopeCategory.setCurrentVariableType(currentVariableScope, left.name, ltype)
	    //*************
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

  String unparseClosureExpression(ClosureExpression exp) { 
    def type = null
    use (TypeCategory) { 
      type = exp.getActualType()
    }
    def tname = type ? owner.typeName(type) : 'id'
    def binding = [ 'type'   : tname, 
		    'params' : closureParameters(exp.parameters),
		    'code'   : unparse(exp.code, 2)
		  ]
    def template = owner.engine.createTemplate(owner.templates.closureDef).make(binding)      
    //owner.innerClassDefScrap += template.toString()
    return template.toString()
  }

  String newBigDecimal(val) { 
    "[NSDecimalNumber decimalNumberWithDecimal:[[NSNumber numberWithDouble:${val}] decimalValue]]"
  }

  String newBigInteger(val) { 
    "[NSNumber numberWithLongLong:${val}]"
  }  

  String convertToString(Expression exp) { 
    if (exp) { 
      def type = getActualType(exp, currentVariableScope)
      if (ClassHelper.STRING_TYPE != type) { 
	if (isNumericalType(type)) { 
	  return "[${toNSNumber(exp)} stringValue]"
	} else { 
	  return "[${expression(exp)} description]"
	}
      }
      return expression(exp)
    }
    return ''
  }

  String toNSNumber(Expression exp) { 
    if (exp) { 
      def type = getActualType(exp, currentVariableScope)
      if (isNumericalType(type)) { 
	type = wrapSafely(type)
	def tname = type.nameWithoutPackage
	if (tname == 'Integer') tname = 'Int'
	return "[NSNumber numberWith${tname}: ${expression(exp)}]"
      }
    }
    return expression(exp)
  }

  String toNSObject(Expression exp) { 
    if (exp) { 
      def type = getActualType(exp, currentVariableScope)
      def tname = ''
      if (isNumericalType(type)) { 
	type = wrapSafely(type)
	tname = type.nameWithoutPackage      
	if (tname == 'Integer') tname = 'Int'
	else if (tname == 'Character') tname = 'Char'
	return "[NSNumber numberWith${tname}: ${expression(exp)}]"
      } else if (isBooleanType(type)) { 
	return "[NSNumber numberWithBool: ${expression(exp)}]"
      } 
    }
    return expression(exp)
  }

  /*
    %@     Object
    %d, %i signed int
    %u     unsigned int
    %f     float/double

    %x, %X hexadecimal int
    %o     octal int
    %zu    size_t
    %p     pointer
    %e     float/double (in scientific notation)
    %g     float/double (as %f or %e, depending on value)
    %s     C string (bytes)
    %S     C string (unichar)
    %.*s   Pascal string (requires two arguments, pass pstr[0] as the first, pstr+1 as the second)
    %c     character
    %C     unichar

    %lld   long long
    %llu   unsigned long long
    %Lf    long double
   */
  String getFormatPattern(Expression exp) { 
    if (exp) { 
      def type = getActualType(exp, currentVariableScope)
      return getFormatPattern(type)
    }
    return ''
  }

  String getFormatPattern(ClassNode type) { 
    if (type) { 
      if (isEnum(type)) { 
	return '%d'
      } else if (type == STRING_TYPE) { 
	return '%s'	
      } else if (isBooleanType(type)) { 
	return '%u'	
      } else if (isNumericalType(type)) { 
	switch (type) { 
	case byte_TYPE: case short_TYPE: case int_TYPE: 
	case Byte_TYPE: case Short_TYPE: case Integer_TYPE: 
	  return '%d'
	case long_TYPE: case Long_TYPE: 
	  return '%lld'
	case float_TYPE: case double_TYPE: 
	case Float_TYPE: case Double_TYPE: 
	  return '%f'
	case char_TYPE: case Character_TYPE: 
	  return '%C'
	}
      }
      return '%s'
    }
    return ''
  }

  String getTrueLiteral() { 
    'YES'
  }

  String getFalseLiteral() { 
    'NO'
  }

  String getNullLiteral() { 
    'nil'
  }

  String makeString(String str) { 
    return "@\"${str}\""
  }

  // used in method definition, signature 
  String parameters(Parameter[] params, String prefix = '', boolean overloaded = false ) {     
    def pstr = prefix ?: ''
    if (params && params.length > 0) {       
      for (int i = 0; i < params.length; i++) { 
	def param = parameter(params[i])
	if (overloaded) { 
	  //def tname = params[i].type.name
	  def tname = owner.JavaTypeName(params[i].type)
	  int k = tname.lastIndexOf('.')
	  if (k >= 0) tname = tname.substring(k + 1)
	  tname = tname.replaceAll('<', '')
	  tname = tname.replaceAll('>', '')
	  tname = tname.replaceAll(',', '')
	  tname = tname.replaceAll(' ', '')
	  tname = tname.capitalize()
	  if (i == 0) {
	    pstr += 'With' 
	  } else { 
	    pstr += ' and'
	  }
	  pstr += (tname + ':' + param) 
	} else { 
	  def pname = params[i].name
	  if (i == 0) { 
	    if (params.length > 1)  
	      pname = pname.capitalize() 
	    else 
	      pname = ''
	  } else { 
	    pstr += ' '
	  }
	  pstr += (pname + ':' + param) 
	}
      }
    }
    return pstr
  }

  String closureParameters(Parameter[] params) { 
    /*
    if (params && params.length > 0) {       
      return params.collect { p -> "${owner.typeName(p.type)} ${p.name}"}.join(', ')
    }
    return ''
    */
    def pstr = ''
    if (params && params.length > 0) { 
      for (int i = 0; i < params.length; i++) { 
	def param = "${owner.typeName(params[i].type)} ${params[i].name}"
	pstr += ((i > 0 ? ', ' : '') + param) 
      }
    }
    return pstr
  }

}