package xj.translate.typeinf

import java.util.ArrayList;
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*

import xj.translate.common.*

import static xj.translate.common.ClassProcessor.*
import static xj.translate.typeinf.TypeInference.*

class MethodHelper { 

  static void transformField(FieldNode f, ClassProcessor cp, boolean addReturn = true) { 
    if (f) { 
      if (addReturn && 
	  f.initialExpression instanceof ClosureExpression) { 
	Statement code = addReturnsIfNeeded(f.initialExpression.code)
	f.initialExpression.setCode(code)
      }
    }
  }

  static void transformMathod(MethodNode m, ClassProcessor cp, boolean addReturn = true) { 
    if (m) { 
      info "[MethodHelper] VariableScope of ${m.name}"
      dumpVariableScope(m.variableScope)
      info "[MethodHelper] =========================="

      if (cp.isScript) { 
	if (m.name == 'run') { 
	  m.returnType = ClassHelper.VOID_TYPE
	}
      }

      Statement statement = m.getCode()

      //println "VariableScope of ${m.name} body"
      //dumpVariableScope(statement?.variableScope)
      //println "=========================="
      
      if (addReturn && statement) // needed for @interface methods
	m.setCode(addReturnsIfNeeded(statement, !m.isVoidMethod()))

    }
  }

  static private Statement addReturnsIfNeeded(Statement statement, boolean needReturn = true) { 
    if (statement) { 
      switch (statement.class) { 
      case EmptyStatement:      
	if (needReturn) {
	  ReturnStatement ret = new ReturnStatement(ConstantExpression.NULL);
	  ret.setSourcePosition(statement);
	  return ret;
	}
	break; 
	
      case ExpressionStatement:
	if (statement.expression instanceof DeclarationExpression && 
	    statement.expression.rightExpression instanceof ClosureExpression) { 
	  //info "[MethodHelper] addReturnsIfNeeded -> Closure"
	  
	  //Statement code = addReturnsIfNeeded(statement.expression.rightExpression.code, true)
	  //statement.expression.rightExpression.setCode(code)
	  addReturnsIfNeeded(statement.expression.rightExpression.code, true)
	}
	if (needReturn) {
	  Expression expr = statement.expression
	  Statement ret = new ReturnStatement(expr)
	  ret.setSourcePosition(expr)
	  return ret;
	}
	break; 

      case SynchronizedStatement:
      	SynchronizedStatement sync = (SynchronizedStatement) statement;
	sync.setCode(addReturnsIfNeeded(sync.code, needReturn));
	// return sync;
	break;

      case IfStatement:
	IfStatement ifs = (IfStatement) statement;
	ifs.setIfBlock(addReturnsIfNeeded(ifs.getIfBlock(), needReturn));
	ifs.setElseBlock(addReturnsIfNeeded(ifs.getElseBlock(), needReturn));
	//return ifs;
	break; 

      case TryCatchStatement:
	TryCatchStatement trys = (TryCatchStatement) statement;
	trys.setTryStatement(addReturnsIfNeeded(trys.getTryStatement(), needReturn));
	if (trys.finallyStatement) { 
	  trys.setFinallyStatement(addReturnsIfNeeded(trys.finallyStatement, needReturn))
	} else {  
	  final int len = trys.getCatchStatements().size();
	  for (int i = 0; i != len; ++i) {
	    final CatchStatement catchStatement = trys.getCatchStatement(i);
	    catchStatement.setCode(addReturnsIfNeeded(catchStatement.getCode(), needReturn));
	  }
	}
	//return trys;
	break; 

      case SwitchStatement:
	SwitchStatement swi = (SwitchStatement) statement;
	for (CaseStatement caseStatement : swi.getCaseStatements()) {
	  caseStatement.setCode(adjustSwitchCaseCode(caseStatement.getCode(), needReturn, false));
	}
	swi.setDefaultStatement(adjustSwitchCaseCode(swi.getDefaultStatement(), needReturn, true));
	//return swi;
	break;

      case BlockStatement:
	final List<Statement> list = statement.statements
	if (!list.isEmpty()) {
	  for (int i = 0; i < list.size(); i++) { 
	    //list.set(i, addReturnsIfNeeded(list.get(i)));
	    list[i] = addReturnsIfNeeded(list[i], i == list.size() - 1 ? needReturn : false);
	  }
	  //return new BlockStatement(list, statement.getVariableScope());
	} else if (needReturn) { 
	  ReturnStatement ret = new ReturnStatement(ConstantExpression.NULL);
	  ret.setSourcePosition(statement);
	  return ret;
	}
      
      }
    }

    return statement
  }

  static private Statement adjustSwitchCaseCode(Statement statement, boolean needReturn, boolean defaultCase) {
    if(statement instanceof BlockStatement) {
      final List list = ((BlockStatement)statement).getStatements();
      if (!list.isEmpty()) {
	int idx = list.size() - 1;
	Statement last = (Statement) list.get(idx);
	if (last instanceof BreakStatement) {
	  if (needReturn) list.remove(idx);
	  return addReturnsIfNeeded(statement, needReturn);
	} else if (defaultCase) {
	  return addReturnsIfNeeded(statement, needReturn);
	}
      }
    } else {
      if (defaultCase && needReturn && statement instanceof EmptyStatement) {
	return addReturnsIfNeeded(statement, needReturn);
      }
    }
    return statement;
  }

  static void dumpVariableScope(VariableScope vs) { 
    info "[MethodHelper] referencedClassVariables"
    vs?.referencedClassVariables.each {name, var -> 
      info "[MethodHelper] ${name}: ${var.type.name}"
    }

    info "[MethodHelper] referencedLocalVariables"
    vs?.referencedLocalVariablesIterator.each { var -> 
      info "[MethodHelper] ${var.name}: ${var.type.name}"
    }
    /*
    def iter = vs?.referencedLocalVariablesIterator
    while (iter.hasNext()){ 
      def var = iter.next()
      println "${var.name}: ${var.type.name}"
    }
    */
    
  }

  // return a list of triples [ ReturnStatement, Type, isDynamicTyped]
  // or a list only containing [ ReturnStatement, Type  ] if the typeHelper and className are null
  public static List findReturnStatements(Statement statement, 
					  VariableScope localVariableScope,
					  TypeInference.DynamicTypesHelper typeHelper = null, 
					  String className = null) {
    List result = []
    if (statement == null)
      return result;
	  
    switch(statement.class) { 
    case ReturnStatement:
      if (!(statement.expression.class == ConstantExpression && 
	    statement.expression.value == null)) { 
	ClassNode type = determineType(statement.expression, localVariableScope)
	if (typeHelper != null) {
	  boolean isDynamic = isDynamicTyped(statement.expression, localVariableScope, typeHelper, className)
	  result.add( [ statement, type, isDynamic ])
	} else {
	  result.add( [ statement, type] )
	}
      }
      return result;
		  
    case IfStatement: 
      result.addAll(findReturnStatements(statement.getIfBlock(), localVariableScope, typeHelper, className))
      result.addAll(findReturnStatements(statement.getElseBlock(), localVariableScope, typeHelper, className))
      return result;
		  
    case TryCatchStatement:
      result.addAll(findReturnStatements(statement.getTryStatement(),
					 localVariableScope, typeHelper, className))
      if (statement.getFinallyStatement()) { 
	result.addAll(findReturnStatements(statement.getFinallyStatement(),
					   localVariableScope, typeHelper, className))
      } else { 
	for (int i=0; i<statement.getCatchStatements().size; i++) { 
	  result.addAll(findReturnStatements(statement.getCatchStatement(i).getCode(), 
					     localVariableScope, typeHelper, className))
	}
      }
      return result;
		  
    case BlockStatement:
      for (s in statement.statements) { 
	result.addAll(findReturnStatements(s, statement.variableScope, typeHelper, className))
      }
    
      /*
      //for (int i=0; i<statement.getStatements().size; i++) 
      //result.addAll(findReturnStatements(statement.getStatements().get(i)))
      result.addAll(findReturnStatements(statement.getStatements().get(statement.getStatements().size - 1),
					 statement.variableScope, typeHelper, className))
      */
      return result;
		  
    case SynchronizedStatement:
      result.addAll(findReturnStatements(statement.getCode(), localVariableScope, typeHelper, className))
      return result;
		  
    case WhileStatement:
    case DoWhileStatement:
      result.addAll(findReturnStatements(statement.getLoopBlock(), localVariableScope, typeHelper, className))
      return result;

    case ForStatement:
      result.addAll(findReturnStatements(statement.getLoopBlock(), 
					 statement.variableScope, typeHelper, className))
      return result;
		  
    case SwitchStatement:
      for (int i=0; i<statement.getCaseStatements().size; i++) 
        result.addAll(findReturnStatements(statement.getCaseStatement(i).getCode(), 
					   localVariableScope, typeHelper, className))
      result.addAll(findReturnStatements(statement.getDefaultStatement(), 
					 localVariableScope, typeHelper, className))
      return result;
		  
    default:
      return result;
    }
  }

    //This will look through an expression and return a list of all VariableExpressions it contains
    public static List<VariableExpression> findVariableExpressions(Expression e) {
	    List<VariableExpression> result = new ArrayList<VariableExpression>()
	    //First check generically for .getExpressions() and .getExpression
	    try {
	      def list = e.getExpressions()
	      def itr = list.iterator()
	      while (itr.hasNext()) {
		result.addAll(findVariableExpressions(itr.next()))
	      }
	    } catch (MissingMethodException mme) {}
	    try {
	      result.addAll(findVariableExpressions(e.getExpression()))
	    } catch (MissingMethodException mme) {}
	
	    //Now go through specific cases
	    switch (e.class) {
	    //What we're looking for
	    case VariableExpression:
	      result.add(e)
	      return result;
	    //Look through anything that might contain an expression for other
	    //variable expressions that might be hiding
	    case DeclarationExpression:
	      result.addAll(findVariableExpressions(e.leftExpression))
	      result.addAll(findVariableExpressions(e.rightExpression))
	      return result;
	    case BinaryExpression:
	      result.addAll(findVariableExpressions(e.leftExpression))
	      result.addAll(findVariableExpressions(e.rightExpression))
	      return result;
	    case ClosureExpression:
	      def list = findStatements(e.getCode())
	      for (int i=0; i<list.size(); i++) {
		list.addAll(findStatements(list.get(i)))
		result.addAll(findDependencies(list.get(i)))
	      }
	      return result;
	    case ConstructorCallExpression:
	      result.addAll(findVariableExpressions(e.getArguments()))
	      return result;
	    case ElvisOperatorExpression:
	    case TernaryExpression:
	      result.addAll(findVariableExpressions(e.getBooleanExpression()))
	      result.addAll(findVariableExpressions(e.getTrueExpression()))
	      result.addAll(findVariableExpressions(e.getFalseExpression()))
	      return result;
	    case FieldExpression:  
	      def f = e.getField()
	      result.addAll(findVariableExpressions(f.getInitialExpression()))
	      result.addAll(findVariableExpressions(f.getInitialValueExpression()))
	      return result;
	    case GStringExpression:
	      result.addAll(findVariableExpressions(e.asConstantString()))
	      return result;
	    case MapExpression:
	    case NamedArgumentListExpression:
	      def list = e.getMapEntryExpressions()
	      def itr = list.iterator()
	      while (itr.hasNext()) {
		result.addAll(findVariableExpressions(itr.next()))
	      }
	      return result;
	    case MapEntryExpression:
	      result.addAll(findVariableExpressions(e.getKeyExpression()))
	      result.addAll(findVariableExpressions(e.getValueExpression()))
	      return result;
	    case MethodCallExpression:
	      result.addAll(findVariableExpressions(e.getMethod()))
	      result.addAll(findVariableExpressions(e.getObjectExpression()))
	      result.addAll(findVariableExpressions(e.getArguments()))
	      return result;
	    case MethodPointerExpression:
	      result.addAll(findVariableExpressions(e.getMethodName()))
	      return result;
	    case PropertyExpression:
	    case AttributeExpression:
	      result.addAll(findVariableExpressions(e.getObjectExpression()))
	      result.addAll(findVariableExpressions(e.getProperty()))
	      return result;
	    case RangeExpression:
	      result.addAll(findVariableExpressions(e.getFrom()))
	      result.addAll(findVariableExpressions(e.getTo()))
	      return result;
	    case StaticMethodCallExpression:
	      result.addAll(findVariableExpressions(e.getArguments()))
	      return result;
	    default:
	      return result;
	    }
	  }

}
