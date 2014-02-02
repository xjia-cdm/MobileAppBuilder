package xj.translate

import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*

class ASTUtil { 

  public static Statement copyStatement(Statement statement, VariableScope scope) {  
    switch(statement.class) { 			  
    case IfStatement: 
      def s1 = copyStatement(statement.getIfBlock(), scope)
      def s2 = copyStatement(statement.getElseBlock(), scope)
      return new IfStatement(copyExpression(statement.getBooleanExpression(), scope), s1, s2)
			  
    case TryCatchStatement:
      def s1 = copyStatement(statement.getTryStatement(), scope)
      def s2 = copyStatement(statement.getFinallyStatement(), scope)
      def result = new TryCatchStatement(s1, s2)
      statement.getCatchStatements().each { c -> result.addCatch(c) }
      return result

    case CatchStatement:
      def s = copyStatement(statement.getCode(), scope)
      Parameter oldP = statement.getVariable()
      Parameter newP = new Parameter(oldP.getType(), oldP.getName(), 
				     copyExpression(oldP.getInitialExpression(), scope))
      return new CatchStatement(newP, s)
			  
    case BlockStatement:
      def newScope = copyVariableScope(statement.variableScope, scope)
      List<Statement> sList = new ArrayList<Statement>()
      statement.getStatements().each { s -> sList.add(copyStatement(s, newScope)) }
      return new BlockStatement(sList, newScope)
			  
    case SynchronizedStatement:
      def s = copyStatement(statement.getCode(), scope)
      return new SynchronizedStatement(copyExpression(statement.getExpression(), scope), s)
			  
    case WhileStatement:
      def s = copyStatement(statement.getLoopBlock(), scope)
      return new WhileStatement(copyExpression(statement.getBooleanExpression(), scope), s)
	    	
    case DoWhileStatement:
      def s = copyStatement(statement.getLoopBlock(), scope)
      return new DoWhileStatement(copyExpression(statement.getBooleanExpression(), scope), s)

    case ForStatement:
      def newScope = copyVariableScope(statement.variableScope, scope)
      def s = copyStatement(statement.getLoopBlock(), newScope)
      Parameter oldP = statement.variable
      Parameter newP = new Parameter(oldP.originType, oldP.name, 
				     copyExpression(oldP.initialExpression, scope))
      def result = new ForStatement(newP, copyExpression(statement.collectionExpression, newScope), s)
      result.setVariableScope(newScope)
      return result
			  
    case SwitchStatement:
      def s1 = copyStatement(statement.getDefaultStatement(), scope)
      List<CaseStatement> sList = new ArrayList<CaseStatement>()
      statement.getCaseStatements().each { s2 -> sList.add(copyStatement(s2, scope)) }
      return new SwitchStatement(copyExpression(statement.getExpression(), scope), sList, s1)
	      
    case CaseStatement: 
      def s = copyStatement(statement.getCode(), scope)
      return new CaseStatement(copyExpression(statement.getExpression(), scope), s)

      // atomic statements 
    case AssertStatement:
      return new AssertStatement(copyExpression(statement.booleanExpression, scope), 
				 copyExpression(statement.messageExpression, scope))

    case BreakStatement:
      return new BreakStatement(statement.label) 

    case ContinueStatement:
      return new ContinueStatement(statement.label) 

    case ExpressionStatement:
      return new ExpressionStatement(copyExpression(statement.expression, scope))

    case ReturnStatement:
      return new ReturnStatement(copyExpression(statement.expression, scope)) 

    case ThrowStatement:
      return new ThrowStatement(copyExpression(statement.expression, scope)) 

    //case EmptyStatement:
    default:
      //Note we do not need to copy Statements that do not contain other statements 
      //(Such as a ReturnStatement), just return the original Statement

      return statement
    }
  }

  public static VariableScope copyVariableScope(VariableScope scope, VariableScope parent = null) {
    if (scope) {
      VariableScope newScope
      if (parent == null)
	newScope = new VariableScope(scope.getParent())
      else
	newScope = new VariableScope(parent)
      newScope.setDynamicResolving(scope.isResolvingDynamic())
      newScope.setInStaticContext(scope.isInStaticContext())
      def iterator = scope.getReferencedClassVariablesIterator()
      while (iterator.hasNext()) {
	newScope.putReferencedClassVariable(iterator.next())
      }
      iterator = scope.getReferencedLocalVariablesIterator()
      while (iterator.hasNext()) {
	newScope.putReferencedLocalVariable(iterator.next())
      }
	    
      iterator = scope?.declaredVariables?.each { key, val -> 
	def var;
	switch(val.class) {
	case Parameter:
	  var = new Parameter(val.type, val.name, 
			      copyExpression(val.getInitialExpression(), scope))
	  var.dynamicTyped = val.dynamicTyped;
	  var.setInStaticContext(val.isInStaticContext())
	  var.setClosureSharedVariable(val.isClosureSharedVariable())
	  break;
	case FieldNode:
	  var = new FieldNode(val.name, val.modifiers, val.type, val.owner, 
			      copyExpression(val.initialValueExpression, scope))
	  var.closureSharedVariable = val.closureSharedVariable
	  var.inStaticContext = val.inStaticContext
	  var.type = val.type
	  var.holder = val.holder
	  var.dynamicTyped = val.dynamicTyped
	  break;
	case PropertyNode:
	  var = new PropertyNode(val.field, val.modifiers, val.getterBlock, val.setterBlock)
	  var.closureSharedVariable = val.closureSharedVariable
	  var.inStaticContext = val.inStaticContext
	  var.type = val.type
	  var.dynamicTyped = val.dynamicTyped
	  break;
	case DynamicVariable:
	  var = new DynamicVariable(val.name, val.isInStaticContext())
	  var.type = val.type
	  var.initialExpression = copyExpression(val.initialExpression, scope)
	  var.dynamicTyped = val.dynamicTyped
	  break;
	case VariableExpression:
	  if (val.dynamicTyped) { 
	    var = new VariableExpression(val.name) 
	  } else { 
	    var = new VariableExpression(val.name, val.originType) 
	  }
	  //var = new VariableExpression(val.getAccessedVariable())
	  var.setUseReferenceDirectly(val.isUseReferenceDirectly())
	  var.setInStaticContext(val.isInStaticContext())
	  var.setClosureSharedVariable(val.isClosureSharedVariable())
	  break;
	default:
	  var = val
	}
	newScope.putDeclaredVariable(var)
      }
      return newScope
    }
    return null
  }

  static public List copyExpressionList(List expressions, VariableScope scope) { 
    if (expressions) 
      return expressions.collect{ e -> copyExpression(e, scope) }
    return null
  }

  static public Parameter[] copyParameters(Parameter[] param, VariableScope scope) { 
    if (param != null) { 
      if (param.length > 0) {  
	Parameter[] result = new Parameter[param.length]
	for (int i = 0; i < param.length; i++) { 
	  result[i] = new Parameter(param[i].originType, param[i].name, 
				    copyExpression(param[i].initialValue, scope))
	}
	return result
      }
      return Parameter.EMPTY_ARRAY
    }
    return null
  }

  static public Expression copyExpression(Expression exp, VariableScope scope) { 
    if (exp) { 
      switch (exp.class) { 
      case ArgumentListExpression:
	if (exp.expressions) { 
	  return new ArgumentListExpression(copyExpressionList(exp.expressions, scope))
	} else {  
	  return new ArgumentListExpression()
	}
      case TupleExpression:
	if (exp.expressions) { 
	  return new TupleExpression(copyExpressionList(exp.expressions, scope))
	} else {  
	  return new TupleExpression()
	}

      case ArrayExpression:
	return new ArrayExpression(exp.elementType, 
				   copyExpresionList(exp.expressions, scope), 
				   copyExpressionList(exp.sizeExpression, scope))

      case AttributeExpression:
	return new AttributeExpression(copyExpression(exp.objectExpression, scope), 
				       copyExpression(exp.property, scope), 
				       exp.safe) 
      case PropertyExpression:
	return new PropertyExpression(copyExpression(exp.objectExpression, scope), 
				      copyExpression(exp.property, scope), 
				      exp.safe) 

      case DeclarationExpression:
	return new DeclarationExpression(copyExpression(exp.leftExpression, scope), 
					 exp.operation, 
					 copyExpression(exp.rightExpression, scope)) 

      case BinaryExpression:
	return new BinaryExpression(copyExpression(exp.leftExpression, scope), 
				    exp.operation, 
				    copyExpression(exp.rightExpression, scope)) 

      case BitwiseNegationExpression:
	return new BitwiseNegationExpression(copyExpression(exp.expression, scope))
	
      case NotExpression:
	return new NotExpression(copyExpression(exp.expression, scope))
	
      case BooleanExpression:
	return new BooleanExpression(copyExpression(exp.expression, scope))

      case CastExpression:
	return new CastExpression(exp.type, 
				  copyExpression(exp.expression, scope), 
				  exp.ignoreAutoboxing) 
	
      case ClassExpression:
	return new ClassExpression(exp.type)
	
      case ClosureExpression:
	def newScope = copyVariableScope(exp.variableScope, scope)
	def newClosure = new ClosureExpression(copyParameters(exp.parameters, scope), 
					       copyStatement(exp.code, newScope)) 
	newClosure.setVariableScope(newScope)
	return newClosure
	
      case ClosureListExpression:
	def newScope = copyVariableScope(exp.variableScope, scope)
	def newClosureList = new ClosureListExpression(copyExpressionList(exp.expressions, scope))
	newClosureList.setVariableScope(newScope)
	return newClosureList
	
      case ListExpression:
	return new ListExpression(copyExpresionList(exp.expressions, scope))
	
      case ConstantExpression:
	return new ConstantExpression(exp.value) 
	
      case ConstructorCallExpression:
	def ctr = new ConstructorCallExpression(exp.type, copyExpression(exp.arguments, scope)) 	
	return ctr
	
      case ElvisOperatorExpression:
	return new ElvisOperatorExpression(copyExpression(exp.trueExpression, scope), 
					   copyExpression(exp.falseExpression, scope))
      case TernaryExpression:
	return new TernaryExpression(copyExpression(exp.booleanExpression, scope), 
				     copyExpression(exp.trueExpression, scope), 
				     copyExpression(exp.falseExpression, scope))

      case EmptyExpression:
	return EmptyExpression.INSTANCE 

      case FieldExpression:
	return new FieldExpression(exp.field) 

      case GStringExpression:
	return new GStringExpression(exp.text, 
				     copyExpressionList(exp.strings, scope), 
				     copyExpressionList(exp.values, scope)) 
	
      case MapEntryExpression:
	return new MapEntryExpression(copyExpression(exp.keyExpression, scope), 
				      copyExpression(exp.valueExpression, scope)) 

      case NamedArgumentListExpression:
	return new NamedArgumentListExpression(copyExpressionList(exp.mapEntryExpressions, scope)) 
      case MapExpression:
	return new MapExpression(copyExpressionList(exp.mapEntryExpressions, scope)) 

      case MethodCallExpression:
	return new MethodCallExpression(copyExpression(exp.objectExpression, scope), 
					copyExpression(exp.method, scope), 
					copyExpression(exp.arguments, scope)) 

      case MethodPointerExpression:
	return new MethodPointerExpression(copyExpression(exp.expression, scope), 
					   copyExpression(exp.methodName, scope)) 

      case PostfixExpression:
	return new PostfixExpression(copyExpression(exp.expression, scope), 
				     exp.operation) 

      case PrefixExpression:
	return new PrefixExpression(exp.operation,
				    copyExpression(exp.expression, scope))
				     
      case RangeExpression:
	return new RangeExpression(copyExpression(exp.from, scope), 
				   copyExpression(exp.to, scope), 
				   exp.inclusive) 

      case SpreadExpression:
	return new SpreadExpression(copyExpression(exp.expression, scope)) 

      case SpreadMapExpression:
	return new SpreadMapExpression(copyExpression(exp.expression, scope)) 

      case StaticMethodCallExpression:
	return new StaticMethodCallExpression(copyExpression(exp.objectExpression, scope), 
					      exp.method, 
					      copyExpression(exp.arguments, scope)) 

      case UnaryMinusExpression:
	return new UnaryMinusExpression(copyExpression(exp.expression, scope)) 

      case UnaryPlusExpression:
	return new UnaryPlusExpression(copyExpression(exp.expression, scope)) 

      case VariableExpression:
	if (exp.dynamicTyped) { 
	  return new VariableExpression(exp.name) 
	} else { 
	  return new VariableExpression(exp.name, exp.originType) 
	}

      case AnnotationConstantExpression:
      case Expression:
      default:
	break
      }
    }
    return exp
  }
   

}
