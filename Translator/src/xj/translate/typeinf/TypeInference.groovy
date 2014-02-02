package xj.translate.typeinf

import groovyjarjarasm.asm.Type;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*

import static org.codehaus.groovy.ast.ClassHelper.*

import xj.translate.common.*

import static xj.translate.typeinf.ClassDependency.* 
import static xj.translate.common.ClassProcessor.* 
import static xj.translate.typeinf.TypeUtil.* 
import static xj.translate.typeinf.VariableScopeCategory.* 
import static xj.translate.ASTUtil.* 
//import static xj.translate.Logger.* 

class TypeInference { 

  public static class DynamicTypesHelper {
    Set<String> oldParsedMethods;   //Last round's parsed dynamic methods
    Set<String> oldParsedVariables; //Last round's parsed dynamic variables
    Set<String> newParsedMethods;   //This round's parsed dynamic methods
    Set<String> newParsedVariables; //This round's parsed dynamic variables
  }

  static verbose = false

  static info(msg) { 
    if (verbose) xj.translate.Logger.info(msg)
  }

  //
  //  Type inference
  //

  //This method is used by the Java and ObjectiveC module processors
  static void inferTypes(Map<String, ClassProcessor> classMap,
			 Map<String, ClassDependency.DependencyNode> allNodes) {
    //Need order classes based on dependencies
    //First parse the code and build a dependency graph
    classMap.each { cname, cp -> 
      findDependencies(cp, allNodes.get(cname), allNodes) }

    List<ClassDependency.DependencyNode> sortedNodes = new ArrayList<ClassDependency.DependencyNode>()
    List<ClassDependency.DependencyNode> rootNodes = new LinkedList<ClassDependency.DependencyNode>() 
    //rootCps are nodes with no parents, start there.
    allNodes.each { nodeName, node -> 
      if (node.parents.size == 0)
	rootNodes.add(node)
    }

    //Now try to sort them using topological sort
    while (rootNodes.size > 0) {
      ClassDependency.DependencyNode node = rootNodes.remove()
      sortedNodes.add(node)

      //start to remove children
      node.children.each { cNode -> 
	cNode.parents.remove(node)
	if (cNode.parents.size == 0)
	  rootNodes.add(cNode)
      }
    }
    
    //If we still have nodes with parents, then there is a cyclic dependency
    List<ClassDependency.DependencyNode> leafNodes = new ArrayList<ClassDependency.DependencyNode>()
    List<ClassDependency.DependencyNode> cyclicNodes = new ArrayList<ClassDependency.DependencyNode>()
    allNodes.each { nodeName, node ->
      if (node.parents.size > 0) {  	
	if (node.children.size == 0)
	  leafNodes.add(node)
	else 
	  cyclicNodes.add(node)
      }
    }

    //While inferring types, we need to keep parsing the class until we gain no new information
    //in case there as cyclic dependencies in the methods/variables
    //The 3 arrays lists used for this are defined at the class level to give all methods access to them
    DynamicTypesHelper typeHelper = new DynamicTypesHelper();
    int newCombinedSize = 0; //Start at -1 so the first while loop happens
    
    //Start parsing for types
    //Process leaf nodes first
    leafNodes.each { node ->
      ClassProcessor cp = classMap.get(node.name)
      ModuleProcessor.currentClassProcessor = cp //To be used for local calls
      typeHelper.oldParsedMethods = new HashSet<String>()
      typeHelper.oldParsedVariables = new HashSet<String>()
      newCombinedSize = 1 //Start at 1 so the first while loop happens
      while (newCombinedSize > 0) {
	typeHelper.newParsedMethods = new HashSet<String>()
	typeHelper.newParsedVariables = new HashSet<String>()
        	
	cp.inferTypes(typeHelper)

	newCombinedSize = typeHelper.newParsedMethods.size() + typeHelper.newParsedVariables.size();
	typeHelper.oldParsedMethods.addAll(typeHelper.newParsedMethods)
	typeHelper.oldParsedVariables.addAll(typeHelper.newParsedVariables)
      }
    }
    
    //Now process types of all of the nodes with the cyclical dependency at once
    typeHelper.oldParsedMethods = new HashSet<String>()
    typeHelper.oldParsedVariables = new HashSet<String>()
    newCombinedSize = 1 //Start at 1 so the first while loop happens
    while (newCombinedSize > 0) {
      typeHelper.newParsedMethods = new HashSet<String>()
      typeHelper.newParsedVariables = new HashSet<String>()
      cyclicNodes.each { node ->
	ClassProcessor cp = classMap.get(node.name)
	ModuleProcessor.currentClassProcessor = cp //To be used for local calls
	cp.inferTypes(typeHelper)
      }
      newCombinedSize = typeHelper.newParsedMethods.size() + typeHelper.newParsedVariables.size();
      typeHelper.oldParsedMethods.addAll(typeHelper.newParsedMethods)
      typeHelper.oldParsedVariables.addAll(typeHelper.newParsedVariables)
    }
    //Finally process types of all the sorted nodes in reverse order
    //(note this will be the only step if acyclic)
    for (int i=sortedNodes.size-1; i >= 0; i--) {
      ClassProcessor cp = classMap.get(sortedNodes.get(i).name)
      ModuleProcessor.currentClassProcessor = cp //To be used for local calls
      typeHelper.oldParsedMethods = new HashSet<String>()
      typeHelper.oldParsedVariables = new HashSet<String>()
      newCombinedSize = 1 //Start at 1 so the first while loop happens
      while (newCombinedSize > 0) {
	typeHelper.newParsedMethods = new HashSet<String>()
	typeHelper.newParsedVariables = new HashSet<String>()
        	
	if (cp != null) //If we failed to find the class processor, skip for now
	  cp.inferTypes(typeHelper)

	newCombinedSize = typeHelper.newParsedMethods.size() + typeHelper.newParsedVariables.size();
	typeHelper.oldParsedMethods.addAll(typeHelper.newParsedMethods)
	typeHelper.oldParsedVariables.addAll(typeHelper.newParsedVariables)
      }
    }
  }  

  static void inferMethodTypes(MethodNode m, String className, DynamicTypesHelper typeHelper) { 
    if (m) { 
      //TODO: Check for dynamic variables/methods that are dependent on other dynamic variables/methods
      //Parse the code for types
      untypedVariables.clear()
      inferStatementTypes(m.code, m.variableScope) 
      
      //Now try to determine the type of the method
      if (m.code) { 
	if (m.dynamicReturnType) { //Make sure to account for void types
	  List returns = MethodHelper.findReturnStatements(m.code, m.variableScope, typeHelper, className)
	  //info "[TypeInference] inferMethodTypes ${m.name} returns: ${returns}"

	  boolean isDynamicDependent = false
	  if (m.returnType.name != "void") {
	    ClassNode rtype = null
	    for (rt in returns) { 
	      if (rtype != null) { 
		rtype = TypeUtil.commonType(rtype, rt[1])
	      } else { 
		rtype = rt[1]
	      } 
	      if (rt[2]) isDynamicDependent = true
	    }
	    m.returnType = rtype
	  }

	  /*
	  boolean isDynamicDependent = false
	  if (m.returnType.name != "void") {
	    ArrayList<ClassNode> returnTypes = new ArrayList<ClassNode>();
	    for (int i=0; i<m.code.statements.size; i++) {
	      def statement = m.code.statements.get(i)
	      def returnStatements = MethodHelper.findReturnStatements(statement)
	      for (int j=0; j<returnStatements.size; j++) {
		def exp = returnStatements.get(j).expression
		returnTypes.add(determineType(exp, null)) ///// !!! no local scope 
		//Will need to see if the expression is dynamic, possibly use the following code
		if (isDynamicTyped(exp, null, typeHelper, className))
		  isDynamicDependent = true
	      }
	    }
	    m.returnType = TypeUtil.commonType(returnTypes)
	  }
	  */

	  String fullName = className + "." + m.name;
	  if (!isDynamicDependent && !typeHelper?.oldParsedMethods?.contains(fullName))
	    typeHelper?.newParsedMethods?.add(fullName)
		  
	}
      } 
    }
  }

  // return value: null if not; [ String name, String type, boolean positive ]
  static def isTypePred(Expression exp) { 
    if (exp) { 
      if (exp instanceof BooleanExpression) {
	return isTypePred(exp.expression) 
      } else if (exp instanceof NotExpression)	{ 
	def t = isTypePred(exp.expression)
	if (t) { 
	  t[2] = !t[2]
	  return t
	}
      } else if (exp instanceof BinaryExpression) { 
	def left = exp.leftExpression 
	def right = exp.rightExpression
	if (exp.operation.text == 'instanceof') { 
	  //info "[TypeInference] isTypePred @2: ${exp}"
	  if (left instanceof VariableExpression &&
	      right instanceof ClassExpression) { 
	    return [ left.name, right.text, true ]
	  }
	} else if (exp.operation.text == '==') { 
	  if (left instanceof PropertyExpression &&
	      left.objectExpression instanceof VariableExpression &&
	      left.propertyAsString == 'class' &&
	      right instanceof ClassExpression) { 
	    return [ left.objectExpression.name, right.text, true ]
	  }
	}
      }
    }
    return null
  }

  static void handleTypePredIfStatement(pred, Statement stmt, VariableScope currentVariableScope) { 
    if (pred) { 
      if (stmt instanceof BlockStatement) { 
	VariableScopeCategory.presetCurrentVariableType(stmt.variableScope, pred[0], ClassHelper.make(pred[1]))
      } else { 
	VariableScopeCategory.setCurrentVariableType(currentVariableScope, pred[0], ClassHelper.make(pred[1]))
      }
    }
  }

  // return value: null if not; var name if yes
  static String isTypeExp(Expression exp) { 
    if (exp instanceof PropertyExpression &&
	exp.objectExpression instanceof VariableExpression &&
	exp.propertyAsString == 'class') { 
       return exp.objectExpression.name
    }
    return null
  }

  static void handleTypePredCaseStatement(String varname, CaseStatement stmt, VariableScope currentVariableScope) { 
    if (varname) { 
      if (stmt.expression instanceof ClassExpression) { 
	String typename = stmt.expression.text
	if (stmt.code instanceof BlockStatement) { 
	  VariableScopeCategory.presetCurrentVariableType(stmt.code.variableScope,
							  varname, ClassHelper.make(typename))
	} else { 
	  VariableScopeCategory.setCurrentVariableType(currentVariableScope, 
						       varname, ClassHelper.make(typename))
	}
      }
    }
  }

  static void inferStatementTypes(Statement statement, VariableScope currentVariableScope) {  
    if (statement == null)
      return;

    //if (statement.hasProperty('variableScope')) { 
    if (statement instanceof BlockStatement || statement instanceof ForStatement ) { 
      currentVariableScope = statement.variableScope
      VariableScopeCategory.clearCurrentTypes(currentVariableScope)
    }
    
    switch (statement.class) { 
    case ReturnStatement:
    case ThrowStatement:
    case ExpressionStatement:
      inferExpressionTypes(statement.expression, currentVariableScope) 
      break

    case ForStatement:
      def cexp = statement.collectionExpression
      inferExpressionTypes(cexp, currentVariableScope) 
      inferStatementTypes(statement.loopBlock, currentVariableScope) 
      break

    case WhileStatement:
      inferExpressionTypes(statement.booleanExpression, currentVariableScope) 
      inferStatementTypes(statement.loopBlock, currentVariableScope) 
      break      

    case IfStatement:
      def pred = isTypePred(statement.booleanExpression)
      if (pred) info "[TypeInference] Found type pred: ${pred}"
      inferExpressionTypes(statement.booleanExpression, currentVariableScope)       
      if (pred && pred[2])
	handleTypePredIfStatement(pred, statement.ifBlock, currentVariableScope)
      inferStatementTypes(statement.ifBlock, currentVariableScope) 
      if (statement.elseBlock) { 
	if (pred && !pred[2])
	  handleTypePredIfStatement(pred, statement.elseBlock, currentVariableScope)
	inferStatementTypes(statement.elseBlock, currentVariableScope) 
      }
      break      

    case SwitchStatement:
      String varname = isTypeExp(statement.expression)
      inferExpressionTypes(statement.expression, currentVariableScope) 
      statement.caseStatements.each{ s -> 
	if (varname) handleTypePredCaseStatement(varname, s, currentVariableScope)
	inferStatementTypes(s, currentVariableScope) 
      }
      inferStatementTypes(statement.defaultStatement, currentVariableScope) 
      break

    case CaseStatement:
      inferExpressionTypes(statement.expression, currentVariableScope)       
      inferStatementTypes(statement.code, currentVariableScope) 
      break; 

    case SynchronizedStatement:
      inferExpressionTypes(statement.expression, currentVariableScope) 
      inferStatementTypes(statement.code, currentVariableScope) 
      break

    default:
      def statements = ClassDependency.findStatements(statement)
      for (int i = 0; i < statements.size(); i++)
        inferStatementTypes(statements.get(i), currentVariableScope) 
    }    

    if (statement instanceof BlockStatement || statement instanceof ForStatement ) { 
      VariableScopeCategory.clearCurrentTypes(currentVariableScope)
    }
  }

  static List<String> untypedVariables = new ArrayList<String>()

  static void inferExpressionTypes(Expression exp, VariableScope currentVariableScope) {  
    //Return a list of dynamic variables that depend on dynamic variables/methods
    //And also determine typing information
    switch (exp.class) { 
    case DeclarationExpression:
      def var = exp.variableExpression
      Variable decl = currentVariableScope.getDeclaredVariable(var.name)
      if (decl) {
	if (exp.rightExpression.class == EmptyExpression || 
	    exp.rightExpression.class == ConstantExpression && 
	    exp.rightExpression.isNullExpression()) {
  	  				
	  if (!exp.leftExpression.dynamicTyped)
	    decl.type = exp.leftExpression.type
	  else {
	    untypedVariables.add(var.name);
	  }	
	} else {
	  if (exp.leftExpression.dynamicTyped)
	    decl.type = determineType(exp.rightExpression, currentVariableScope)
	}
	if (exp.rightExpression.class == ClosureExpression) { 
	  def t = determineType(exp.rightExpression, currentVariableScope)
	  info "[TypeInference] add closure def ${var.name} ${t}"
	  VariableScopeCategory.putClosure(currentVariableScope, var.name, exp.rightExpression)
	}
	info "[TypeInference] inferExpressionTypes: DeclarationExpression: ${var.name} is now ${decl.type}"
      }
      break;
      
    case BinaryExpression:
      if (exp.operation.text == "=") {
	if (exp.leftExpression.class == PropertyExpression) {
	  return;
	} 
	def var = exp.leftExpression
	if (var instanceof VariableExpression) { 
	  Variable decl = currentVariableScope.getDeclaredVariable(var.name) 
	  if (decl && decl.dynamicTyped) {
	    info "[TypeInference] got decl: ${decl.text}"
	    if (untypedVariables.contains(var.name)) {
	      decl.type = determineType(exp.rightExpression, currentVariableScope)
	      untypedVariables.remove(var.name);
	    } else {
	      def rtype = determineType(exp.rightExpression, currentVariableScope)
	      decl.type = TypeUtil.commonType(rtype, decl.type)
	      if (!isNumericAssignableType(decl.type, rtype)) { 
		VariableScopeCategory.setCurrentVariableType(currentVariableScope, var.name, rtype)
	      }
	    }
	    info "[TypeInference] inferExpressionTypes: Assignment: ${var.name} is now ${decl.type}"
	  }
	}
      }
      break;

    case MethodCallExpression:
      determineType(exp, currentVariableScope)
      exp.arguments?.expressions?.each { e -> inferExpressionTypes(e, currentVariableScope) }
      break;
      
    case ListExpression:
      exp.expressions.each{ e -> inferExpressionTypes(e, currentVariableScope) }
      break;

    case ClosureExpression:
      determineType(exp, currentVariableScope)
      break; 
      
    default:
      break;
    }
  }

  //
  //  utility methods for type inference 
  //

  // search for variable declarations up the chain of scopes
  // only search for local variable declarations, not field declarations 
  public static Variable getDeclaredVariable(String name, VariableScope localVariableScope) { 
    Variable var = null
    VariableScope variableScope = localVariableScope
    while (variableScope) { 
      var = variableScope.getDeclaredVariable(name)
      if (var) break
      variableScope = variableScope.getParent()
    }
    return var
  }

  // return a pair [varriableScope, variable]
  public static def findDeclaredVariableScope(String name, VariableScope localVariableScope) { 
    Variable var = null
    VariableScope variableScope = localVariableScope
    //info "[TypeInference]   findDeclaredVariableScope ${name} ${variableScope}"
    while (variableScope) { 
      var = variableScope.getDeclaredVariable(name)
      if (var) break
      variableScope = variableScope.getParent()
    }
    return [variableScope, var]
  }

  public static ClassNode getVariableType(Variable var,  VariableScope localVariableScope) { 
    if (var) { 
      VariableScope vscope = localVariableScope
      while (vscope != null) { 
	Variable decl = vscope.getDeclaredVariable(var.name) 
	if (decl) return decl.type 
	vscope = vscope.parent
      }
      return var.type
    }
    return null
  }

  public static Variable getVariableDecl(String var,  VariableScope localVariableScope) { 
    if (var) { 
      VariableScope vscope = localVariableScope
      while (vscope != null) { 
	Variable decl = vscope.getDeclaredVariable(var) 
	if (decl) return decl 
	vscope = vscope.parent
      }
    }
    return null
  }

  public static ClassNode getActualType(Expression exp, VariableScope localVariableScope) {  
    ClassNode type = null
    if (exp) { 
      type = TypeCategory.getActualType(exp)
      if (!type) { 
	type = determineType(exp, localVariableScope)
      }
    }
    return type
  }

  // search for declared fields, up the chain of super classes 
  static FieldNode getDeclaredField(ClassNode c, String name) { 
    FieldNode f = null
    if (c && name) { 
      f = c.getDeclaredField(name) 
      if (!f) { 
	f = getDeclaredField(c.superClass, name)
	if (!f) { 
	  for (ClassNode inf : c.interfaces) { 
	    f = getDeclaredField(inf, name)
	    if (f) break	
	  }
	}
      }
    }
    return f 
  }

  public static ClassNode determineType(Expression exp, VariableScope localVariableScope = null) {  
    //info "[TypeInference] entered determineType on ${exp}"
    if (exp) { 
      boolean isStatic = false;
      ClassNode type = null;
      switch (exp.class) {
      case ElvisOperatorExpression:
      case TernaryExpression: 
	determineType(exp.booleanExpression)
	type = TypeUtil.commonType(determineType(exp.trueExpression, localVariableScope), 
				   determineType(exp.falseExpression, localVariableScope));
	break; 
	  
      case BinaryExpression:
	//info "[TypeInference] start binaryExpression ${exp.text}"
	def left = determineType(exp.leftExpression, localVariableScope)
	def right = determineType(exp.rightExpression, localVariableScope)
	String op = exp.operation.text
	if (op == '[') { 
	  type = indexType(left, right)
	} else if (Operators.isBooleanType(op)) {
	  type = ClassHelper.make(boolean)
	} else if ((left == ClassHelper.make(String) || right == ClassHelper.make(String)) && 
		   TypeUtil.isStringExpressionType(exp.operation.text)) { 
	  type = ClassHelper.STRING_TYPE;
	} else if ((isNumericalType(left) && isNumericalType(right)) || 
		   exp.operation.text == "=") {
	  type = TypeUtil.commonType(left, right);
	} else { 
	  //Otherwise we assume that we're taking on the type of the left side
	  type = left;
	}
	//info "[TypeInference] end binaryExpression ${left.text} ${right.text}"
	break;

      case PostfixExpression:
      case PrefixExpression:
	type = determineType(exp.expression, localVariableScope)
	break;

      case ConstructorCallExpression:
	type = ClassHelper.make(exp.type.name);
	break; 

      case MapExpression:
	type = exp.type
	if (TypeUtil.isMapType(type)) {
	  def kvTypes = findMapType(exp, localVariableScope)
	  if (kvTypes[0] != ClassHelper.OBJECT_TYPE || kvTypes[1] != ClassHelper.OBJECT_TYPE) {
	    def type2 = new ClassNode(type.name, 1, ClassHelper.make(java.lang.Object))
	    def gen1 = new GenericsType(kvTypes[0])
	    def gen2 = new GenericsType(kvTypes[1])
	    type2.setGenericsTypes([ gen1, gen2 ] as GenericsType[])
	    type = type2
	  }
	}
	break; 

      case ListExpression:
	type = exp.type
	if (TypeUtil.isListType(type)) {
	  def elementType = findCollectionType(exp, localVariableScope)
	  if (elementType != ClassHelper.OBJECT_TYPE) {
	    def type2 = new ClassNode(type.name, 1, ClassHelper.make(java.lang.Object))
	    def gen = new GenericsType(elementType)
	    type2.setGenericsTypes([gen] as GenericsType[])
	    type = type2
	  }
	}
	break; 

      case RangeExpression:
	type = ClassHelper.RANGE_TYPE
	def elementType = wrapSafely(determineType(exp.from))
	if (elementType != ClassHelper.OBJECT_TYPE) {
	  def type2 = new ClassNode(type.name, 1, ClassHelper.make(java.lang.Object))
	  def gen = new GenericsType(elementType)
	  type2.setGenericsTypes([gen] as GenericsType[])
	  type = type2
	}
	break; 
      
      case VariableExpression:
	if (exp.name == 'this') { 
	  type = ClassHelper.make(ModuleProcessor.currentClassProcessor.name)
	} else { 
	  if (exp.dynamicTyped) { 	
	    if (localVariableScope) { 	 
	      type = VariableScopeCategory.getCurrentVariableType(localVariableScope, exp.name)
	      if (type) break;
	    } 
	    def classProcessor = ModuleProcessor.currentClassProcessor
	    isStatic = exp.isInStaticContext() 
	    FieldNode field = findFieldDecl(classProcessor, exp.name, isStatic)
	    if (field) { 
	      type = field.type
	      break; 
	    }
	  } 
	  type = ClassHelper.getUnwrapper(exp.type)
	}
	break; 
    	
      case PropertyExpression:
	if (exp.propertyAsString == 'this') { 
	  type = exp.objectExpression.type
	} else { 
	  FieldNode field = findFieldDecl(exp, localVariableScope)
	  if (field) { 
	    type = field.type
	  } else { 
	    //TODO account for dynamically typed properties
	    type = ClassHelper.OBJECT_TYPE
	  }
	}
	break; 

      case StaticMethodCallExpression:
	isStatic = true;
      case MethodCallExpression:
	type = determineTypeMethodCall(exp, isStatic, localVariableScope)
	info "[TypeInference] determineType MethodCallExpression ${exp.text}   type: ${type?.name}"
	break; 

      case ClosureExpression:
	if (exp.code) { 
	  inferStatementTypes(exp.code, exp.variableScope) 
	  List returns = MethodHelper.findReturnStatements(exp.code, exp.variableScope)
	  type = null
	  for (rt in returns) { 
	    if (type != null) { 
	      type = TypeUtil.commonType(type, rt[1])
	    } else { 
	      type = rt[1]
	    } 
	  }
	}
	break; 
    	
      default:
	if (exp.type.equals(ClassHelper.BigDecimal_TYPE)) { //Try to narrow it down
	  type = reduceBigDecimal(exp)
	} else if (exp.type){ 
	  type = ClassHelper.getUnwrapper(exp.type)
	}
      }

      if (type) { 
	use (TypeCategory) { 
	  exp.setActualType(type)
	}
      }
      return type
    }
    
    return null;
  }

  private static ClassNode determineTypeMethodCall(MethodCallExpression exp, 
						   boolean isStatic, 
						   VariableScope localVariableScope) { 
    info "[TypeInference] determineTypeMethodCall ${exp.text}"

    String mname = exp.methodAsString
    if (mname == 'print' || mname == 'println') return null
    
    Class c;
    Class[] params = null;
    ClassNode type = null;

    MethodInfo mInfo = findMethodDef(exp, isStatic, localVariableScope)
    MethodNode mNode = mInfo?.method
    if (mNode) { 
      return mNode.returnType;
    } 

    ClosureInfo cInfo = findClosureDef(exp, localVariableScope)
    ClosureExpression closure = cInfo?.closure
    if (closure) { 
      if (cInfo.myCopies.size() == 1) {
	type = TypeCategory.getActualType(cInfo.myCopies[0])
      } else { 	
	type = TypeCategory.getActualType(closure)
      }
      
      if (type) 
	return type
    }
    
    //If not, see if it is in a library or .jar using java reflection
    try {
      c = Class.forName("${exp.objectExpression.type.name}");
      int paramNum = exp.arguments.expressions.size;
      if (paramNum > 0) {
	params = new Class[paramNum];
	for (i in 0 ..< paramNum) {
	  //params[i] = exp.arguments.expressions.get(i).type.clazz
	  params[i] = determineType(exp.arguments.expressions[i], localVariableScope)?.clazz
	}
      }
      java.lang.reflect.Method m = c.getDeclaredMethod(exp?.getMethodAsString(), params);
      type = ClassHelper.make(m.getReturnType());
    } catch (ClassNotFoundException e) {
      //Otherwise we return Object
      info "[TypeInference] Error: No source found for ${exp.objectExpression.type.name}"
      type = ClassHelper.make(Object);
    } catch (NoSuchMethodException e) {
      info "[TypeInference] Error: No method found that matches ${exp.objectExpression.type.name}.${exp.getMethodAsString()}(${params})"
      boolean matchFound = false;
      ClassNode result = ClassHelper.make(Object)
      
      //TODO possibly search methods of the same name to see if there is only 1 and assume we are using that
      //Still working on this
      //for (int i=0; i<c.getDeclaredMethods().length; i++) {
      //	Method m = c.getDeclaredMethods()[i]
      //	if (m.getName() == exp.objectExpression.type.name
      //	    && m.getParameterTypes().length == params.length) {
      //	  if (matchFound) {//Assume we've found a match but keep looking to see if there's more
      //	    	return ClassHelper.make(Object);
      //			
      //	  } else if (!matchFound) { //Since we have 2 matches, just return object instead
      //			matchFound = true;
      //			result = ClassHelper.make(m.getReturnType())
      //	 }
      //     }
      //}
      
      type = result;
    }
    return type
  }

  // return pair of ClassNode [ KeyType, ValueType ]
  public static findMapType(MapExpression mapExp, VariableScope localVariableScope = null) { 
    info "[TypeInference] findMapType: ${mapExp}"
    LinkedHashSet<ClassNode> keyTypes = null
    LinkedHashSet<ClassNode> valueTypes = null
    for (MapEntryExpression kvpair : mapExp.mapEntryExpressions) { 
      if (keyTypes == null) { 
	keyTypes = getAllTypes(wrapSafely(determineType(kvpair.keyExpression, localVariableScope)))
	valueTypes = getAllTypes(wrapSafely(determineType(kvpair.valueExpression, localVariableScope)))
      } else { 
	LinkedHashSet<ClassNode> curKeyTypes = 
	  getAllTypes(wrapSafely(determineType(kvpair.keyExpression, localVariableScope)))
	LinkedHashSet<ClassNode> newKeyTypes = new LinkedHashSet<ClassNode>()
	for (ClassNode cn : keyTypes) { 
	  if (curKeyTypes.contains(cn))
	    newKeyTypes.add(cn)
	}
	keyTypes = newKeyTypes

	LinkedHashSet<ClassNode> curValueTypes = 
	  getAllTypes(wrapSafely(determineType(kvpair.valueExpression, localVariableScope)))
	LinkedHashSet<ClassNode> newValueTypes = new LinkedHashSet<ClassNode>()
	for (ClassNode cn : valueTypes) { 
	  if (curValueTypes.contains(cn))
	    newValueTypes.add(cn)
	}
	valueTypes = newValueTypes
      }
    }
    info "[TypeInference] findMapType: ${mapExp} --> ${keyTypes}  ${valueTypes}"
    return [ keyTypes.iterator().next(), valueTypes.iterator().next() ]
  }

  public static ClassNode findCollectionType(ListExpression listExp, VariableScope localVariableScope = null) {
    //info "[TypeInference] findCollectionType: ${listExp}"
    LinkedHashSet<ClassNode> commonTypes = null
    for (Expression e : listExp.getExpressions()) {
      if (commonTypes == null)
	commonTypes = getAllTypes(wrapSafely(determineType(e, localVariableScope)))
      else {
	//Remove any types from commonTypes if the new element does not also have it
	LinkedHashSet<ClassNode> myTypes = getAllTypes(wrapSafely(determineType(e, localVariableScope)))
	LinkedHashSet<ClassNode> newCommonTypes = new LinkedHashSet<ClassNode>()
	for (ClassNode cn : commonTypes) { 
	  if (myTypes.contains(cn))
	    newCommonTypes.add(cn)
	}
	commonTypes = newCommonTypes
      }
    }
    //info "[TypeInference] findCollectionType: ${listExp} --> ${commonTypes}"
    return commonTypes.iterator().next();
  }

  //TODO this is a temp fix. Need to account for at least variableExpression
  //And constructor call expressions
  public static ClassNode findCollectionType(Expression exp) {
    return ClassHelper.OBJECT_TYPE
  }

  // determine type for exp[index]
  public static ClassNode indexType(ClassNode left, ClassNode right) { 
    if (TypeUtil.isListType(left)) { 
      GenericsType[] gen = left.getGenericsTypes()
      if (gen && gen.length == 1) { 
	return gen[0].type
      }
    }
    return ClassHelper.OBJECT_TYPE
  }

  public static ClassNode reduceBigDecimal(Expression exp) {
    if (exp && exp.metaClass.hasProperty(exp, 'value')) { 
      def bd = exp.value;
      BigDecimal bdFloat = new BigDecimal(bd.floatValue()+"");
      BigDecimal bdDouble = new BigDecimal(bd.doubleValue()+"");
      
      /*
      //If the float value is equal to the double value, then we can use float
      if (bdFloat.toString().equalsIgnoreCase(bdDouble.toString()))
      return ClassHelper.float_TYPE;
      */
      //Similarly for double to the bigdecimal value
      if (bdDouble.toString().equalsIgnoreCase(bd.toString()))
	return ClassHelper.double_TYPE;
    }
    //Otherwise we need to stick with bigdecimal
    return ClassHelper.BigDecimal_TYPE;
  }

  public static boolean onlyContainsConstants(Expression exp) {
    switch (exp.class) {  
    case ConstantExpression:
      return true

    case BinaryExpression:
      return onlyContainsConstants(exp.leftExpression) && onlyContainsConstants(exp.rightExpression)

    case PrefixExpression:
    case PostfixExpression:
    case UnaryMinusExpression:
    case UnaryPlusExpression:
      return onlyContainsConstants(exp.expression)

    default:
      return false
    }  
  }

  public static boolean hasNameProperty(Expression exp) {
    exp?.hasProperty('name')
  }

  public static FieldNode findFieldDecl(Expression exp, VariableScope localVariableScope) { 
    boolean isStatic = exp.objectExpression instanceof ClassExpression
    String fname = exp.propertyAsString
    def classProcessor = null
    if (exp.implicitThis) { 
      classProcessor = ModuleProcessor.currentClassProcessor
    } else { 
      ClassNode type = determineType(exp.objectExpression, localVariableScope)
      if (type)
	classProcessor = ModuleProcessor.classMap.get(type.nameWithoutPackage)
    }

    if (classProcessor) { 
      fname = classProcessor.normalizeVariableName(fname)
      return findFieldDecl(classProcessor, fname, isStatic)
    } 
    return null
  }

  public static FieldNode findFieldDecl(ClassProcessor cp, 
					String fname, 
					boolean isStatic,
					boolean excludePrivate = false) {
    if (cp && fname) { 
      info "[TypeInference]   findFieldDecl: ${cp.name}  ${fname}  isStatic=${isStatic}"

      def finfo = null 
      if (isStatic) { 
	finfo = cp.staticFieldMap.get(fname)	  
      } else {  
	finfo = cp.fieldMap.get(fname)
	if (!finfo)
	  finfo = cp.staticFieldMap.get(fname)	  
      }
      if (finfo && 
	  (!excludePrivate || 
	  !java.lang.reflect.Modifier.isPrivate(finfo.field.modifiers))) { 
	return finfo.field
      } else { 
	FieldNode f = null
	if (cp.classNode.superClass) {
	  def scp = ModuleProcessor.classMap.get(cp.classNode.superClass.nameWithoutPackage)
	  if (scp) { 
	    // this is a work-around due to groovy setting the default visibility on AST to private 
	    f = findFieldDecl(scp, fname, isStatic) //, true)
	    if (f) return f
	  }
	}
	if (!cp.classNode.allInterfaces.isEmpty()) { 
	  for (c in cp.classNode.allInterfaces) { 
	    if (c.nameWithoutPackage != cp.name) { 
	      def icp = ModuleProcessor.classMap.get(c.nameWithoutPackage)
	      if (icp) { 
		// this is a work-around due to groovy setting the default visibility on AST to private 
		f = findFieldDecl(icp, fname, isStatic) //, true)
		if (f) return f
	      }
	    }
	  }
	}
      }
    }
    return null
  }
 
  public static MethodInfo findMethodDef(MethodCallExpression exp, 
					 boolean isStatic, 
					 VariableScope localVariableScope) {
    return findMethodDef(exp.implicitThis ? null : exp.objectExpression, 
			 exp.methodAsString, exp.arguments.expressions,
			 isStatic, localVariableScope)
  }

  public static MethodInfo findMethodDef(Expression obj, 
					 String method, 
					 List args,
					 boolean isStatic, 
					 VariableScope localVariableScope) {
    def classProcessor
    if (obj == null) { 
      classProcessor = ModuleProcessor.currentClassProcessor
    } else { 
      ClassNode type = determineType(obj, localVariableScope)
      classProcessor = ModuleProcessor.classMap.get(type.nameWithoutPackage)
    }

    if (!classProcessor)
      classProcessor = ModuleProcessor.currentClassProcessor

    return findMethodDef(classProcessor, method, args, isStatic)
  }

  public static MethodInfo findMethodDef(ClassProcessor cp, 
					 String mname, 
					 List args,
					 boolean isStatic,
					 boolean excludePrivate = false) {    
    if (cp && mname) {
      info "[TypeInference] findMethodDef ${cp.name} ${mname} ${args}"

      MethodInfo mInfo = null
      if (isStatic) { 
	def methodInfos = cp.staticMethodMap.get(mname)
	mInfo = findMatchingMethodDef(methodInfos, args)
      } else { 
	def methodInfos = cp.methodMap.get(mname)
	mInfo = findMatchingMethodDef(methodInfos, args)
	if (!mInfo) {
	  methodInfos = cp.staticMethodMap.get(mname)
	  mInfo = findMatchingMethodDef(methodInfos, args)
	}
      }	

      def m //This will be the methodNode we end up using
      if (mInfo) {
    	  m = mInfo.method
      } else { //Need to look in the dynamic methods
	if (isStatic) { 
	  def methodInfos = cp.staticMethodMap.get(mname)
	  mInfo = findMatchingMethodDef(methodInfos, args, true)
	} else { 
	  def methodInfos = cp.methodMap.get(mname)
	  mInfo = findMatchingMethodDef(methodInfos, args, true)
	  if (!mInfo) {
	    methodInfos = cp.staticMethodMap.get(mname)
	    mInfo = findMatchingMethodDef(methodInfos, args, true)
	  }
	}
    	//If we found one, then we should add it to the list of methods 
    	//but changing the arg to the more specific one
    	if (mInfo) {
	  if (needToCopyMethod(mInfo.method, args)) {
	    def newM = changeMethodParams(mInfo.method, args)
	    newM.isOriginalMethod = false
	    newM.originalMethodNode = mInfo.method
	    mInfo.myCopies.add(newM.method)
	    if (isStatic) {
	      cp.staticMethodMap.get(mname) << newM
	    } else {
	      cp.methodMap.get(mname) << newM
	    }
	    m = newM.method
	    mInfo = newM
	  } else {
	    m = mInfo.method
	  }
    	}
      } 

      if (m &&
	  (!excludePrivate || 
	   !java.lang.reflect.Modifier.isPrivate(m.modifiers))) { 
	return mInfo
      } else { 
	if (cp.classNode) { 
	  if (cp.classNode.superClass) {
	    def scp = ModuleProcessor.classMap.get(cp.classNode.superClass.nameWithoutPackage)
	    if (scp) { 
	      mInfo = findMethodDef(scp, mname, args, isStatic, true)
	      if (mInfo) return mInfo
	    }
	  }
	  if (!cp.classNode.allInterfaces.isEmpty()) { 
	    for (c in cp.classNode.allInterfaces) { 
	      if (c.nameWithoutPackage != cp.name) { 
		def icp = ModuleProcessor.classMap.get(c.nameWithoutPackage)
		if (icp) { 
		  mInfo = findMethodDef(icp, mname, args, isStatic, true)
		  if (mInfo) return mInfo
		}
	      }
	    }
	  }
	}
      }
    }
    return null
  }

  public static ClosureInfo findClosureDef(MethodCallExpression exp, 
					   VariableScope localVariableScope) {
    Expression obj = exp.objectExpression 
    List args = exp.arguments.expressions
    if (obj instanceof Variable) { 
      return findClosureDef(obj.name, args, localVariableScope)
    }
    return null
  }

  public static ClosureInfo findClosureDef(String name, List args, 
					   VariableScope localVariableScope) {
    if (localVariableScope) { 
      info "[TypeInference] findClosureDef: ${name}"	
      def vscope = null, var = null
      (vscope, var) = findDeclaredVariableScope(name, localVariableScope)
      if (vscope) { 
	ClosureInfo cinfo = VariableScopeCategory.getClosureInfo(vscope, name)
	if (cinfo) {
	  if (matchingArguments(cinfo.closure.parameters, 
				cinfo.closure.variableScope, 
				args, true)) { 
	    if (!cinfo.myCopies.isEmpty()) { 
	      cinfo.myCopies = cinfo.myCopies.findAll { c -> 
		!matchingArguments(c.parameters, c.variableScope, args, true) }
	    }
	    if (needToCopyClosure(cinfo.closure, args)) { 
	      info '[TypeInference] need change closure param'
	      def newClosure = changeClosureParams(cinfo.closure, args)
	      cinfo.returnType = determineType(newClosure, localVariableScope)
	      cinfo.myCopies.add(newClosure)
	    } else {  
	      cinfo.returnType = TypeCategory.getActualType(cinfo.closure)
	    }
	    info "[TypeInference] findClosureDef ${cinfo.returnType.name} ${cinfo.myCopies.size()}"
	    return cinfo
	  }
	}
      }
    }
    return null
  }

  private static boolean needToCopyMethod(MethodNode m, List args) {
    return m ? needToCopy(m.parameters, m.code, m.variableScope, args) : false
  }

  private static boolean needToCopyClosure(ClosureExpression c, List args) {
    return c ? needToCopy(c.parameters, c.code, c.variableScope, args) : false
  }

  /*
   *   Conditions to copy a method or closure with a more specific data type
   *   1. must be dynamically declared parameter
   *   2. the parameter may affect the return type
   *   3. the argument type is primitive or string 
   */
  private static boolean needToCopy(Parameter[] params, 
				    Statement code, 
				    VariableScope vscope, 
				    List args) {
    List returns = MethodHelper.findReturnStatements(code, vscope)
    //Find all of the variable expressions that the return statements are dependent on
    List<VariableExpression> varExpressions = [] 
    for (rt in returns) {
      varExpressions.addAll(MethodHelper.findVariableExpressions(rt[0].expression))
    }
    //Now just get a list of the dynamic variable names
    List<String> dynamicReturnVars = [] 
    for (var in varExpressions) {
      if (var.dynamicTyped)
	dynamicReturnVars.add(var.name)
    }
    if (params.length > 0) {
      for (i in 0 .. (params.length-1)) {
	//First check if the calling type is primitive
	def type = args[i].type
	if (type == ClassHelper.BigDecimal_TYPE)
	  type = reduceBigDecimal(args[i])
	if (params[i].dynamicTyped) {
	  if (ClassHelper.isPrimitiveType(type) || 
	      ClassHelper.STRING_TYPE == type	|| 
		dynamicReturnVars.contains(params[i].name)) {
	    return true
	  }
	}
      }
    }
    return false
  }
  
  private static MethodInfo changeMethodParams(MethodNode m, List args) {
    if (m) {
      def variableScope = copyVariableScope(m.getVariableScope())
      Parameter[] params = changeParameters(m.parameters, variableScope, args)
      Statement code = copyStatement(m.code, variableScope)
      
      def newM = new MethodNode(m.name, m.modifiers, ClassHelper.make(Object), params, m.exceptions, code)
      newM.setVariableScope(variableScope)
      return new MethodInfo(method : newM)
    }
    return null
  }

  private static ClosureExpression changeClosureParams(ClosureExpression c, List args) {
    if (c) {
      def variableScope = copyVariableScope(c.getVariableScope())
      Parameter[] params = changeParameters(c.parameters, variableScope, args)
      Statement code = copyStatement(c.code, variableScope)
      def newClosure = new ClosureExpression(params, code)
      newClosure.setVariableScope(variableScope)
      return newClosure
    }
    return null
  }


  // Change the parameters based on argument types 
  private static Parameter[] changeParameters(Parameter[] oldParams, 
					      VariableScope variableScope, 
					      List args) { 
    if (oldParams && args) { 
      Parameter[] params = new Parameter[oldParams.length]
      for (i in 0..(oldParams.length-1)) {
	def param = oldParams[i]
	//Check for lists to try to get generic types
	def argType = args[i].type
	if (TypeUtil.isListType(argType)) {

	  def genericType = findCollectionType(args[i])
	  if (genericType != ClassHelper.OBJECT_TYPE) {
	    ClassNode type2 = new ClassNode(argType.name, 1, ClassHelper.make(java.lang.Object))

	    //def gen = new GenericsType(ClassHelper.OBJECT_TYPE, [genericType] as ClassNode[], null)
	    //gen.setName('?')
	    def gen = new GenericsType(genericType)
	    type2.setGenericsTypes([gen] as GenericsType[])

	    argType = type2
	  }
	}
	if (oldParams[i].isDynamicTyped()) {
	  //Check for BigDecimal that can be reduced to Double
	  if (argType == ClassHelper.BigDecimal_TYPE)
	    argType = reduceBigDecimal(args[i])
	  params[i] = new Parameter(argType, param.name, 
				    copyExpression(param.getInitialExpression(), variableScope))
	  params[i].dynamicTyped = true
	  Variable decl = variableScope?.getDeclaredVariable(param.name)
	  if (decl) {
	    decl.type = argType
	    decl.dynamicTyped = true
	  }
	} else {
	  params[i] = oldParams[i]
	}
      }
      return params
    }
    return null
  }

  public static MethodInfo findConstructorDef(ClassNode type, Expression args) { 
    info "[TypeInference] findConstructorDef name=${type?.nameWithoutPackage}"
    def classProcessor
    if (type == null) { 
      classProcessor = ModuleProcessor.currentClassProcessor
    } else { 
      classProcessor = ModuleProcessor.classMap.get(type.nameWithoutPackage)
    }
    if (classProcessor)
      return findMatchingConstructorDef(classProcessor.constructors, args.expressions)
    else 
      return null
  }

  private static MethodInfo findMatchingMethodDef(List methodInfos,  
						  List args, 
						  boolean includeDynamic = false) { 
    def foundMethod = null
    if (methodInfos) {
      int offset = 0 //Needed for when we remove items from methodInfos
      for (int k=0; k - offset < methodInfos.size; k++) { 
	def methodInfo = methodInfos.get(k - offset)
	if (matchingArguments(methodInfo.method.parameters, 
			      methodInfo.method.variableScope, 
			      args, includeDynamic)) {
	  if (foundMethod == null) {  //First method found
	    foundMethod = methodInfo
	  } else { //Found other matches as well, delete them
	    methodInfos.remove(k - offset)
	    offset++;
	  }
	}
      }
    }	
    return foundMethod; //null if nothing is found
  }

  private static MethodInfo findMatchingConstructorDef(List<MethodInfo> ctrs, List args) { 
    if (ctrs) { 
      for (int i = 0; i < ctrs.size(); i++) { 
	if (matchingArguments(ctrs[i].method.parameters, 
			      ctrs[i].method.variableScope,
			      args))
	  return ctrs[i]
      }
    } 
    return null
  }

  // minimum number of arguments to match the parameter, considering optional parameters 
  private static int minArgs(Parameter[] parameters) { 
    if (parameters) { 
      int len = parameters.length
      while (len > 0) { 
	if (parameters[len - 1].hasInitialExpression()) { 
	  len--
	} else { 
	  break
	}
      }
      return len
    }
    return 0
  }

  private static boolean matchingArguments(Parameter[] parameters, 
					   VariableScope vscope, 					   
					   List args, 
					   boolean includeDynamic = false) { 
    if (!parameters && !args) 
      return true
    int maxLen = parameters.length
    int minLen = minArgs(parameters)
    int argLen = args.size()
    //if (parameters.length == args.size()) {
    if (argLen >= minLen && argLen <= maxLen) {
      boolean match = true
      
      // A list of types which should be changed to a more general type if a match is found
      def toBeGeneralized = [] 

      def oldArgsGenericsTypes;
      if (parameters.length > 0) {
	for (i in 0 .. (parameters.length-1)) {
	  if (i >= argLen) { 
	    // optional parameter 
	    args << parameters[i].initialExpression
	    continue
	  }

	  if (includeDynamic && 
	      parameters[i].dynamicTyped && 
	      parameters[i].type == ClassHelper.OBJECT_TYPE) continue
	  
	  //def type = args[i].type
	  def type = determineType(args[i], vscope)
	  
	  //Do a check for polymorphism
	  boolean isInheritance = false;
	  boolean diffListType = false;
	  def t1 = parameters[i].type

	  info "[TypeInference] matchingArguments [${i}]: ${type} ${t1}"

	  if (t1 != ClassHelper.OBJECT_TYPE) {
	    if (TypeUtil.isListType(t1)) {
	      ////return TypeUtil.isListType(type) // all list type considered same
	      
	      ////////////
	      isInheritance = true
	      //Try to find a generic type for the arguments
	      def genericType = findCollectionType(args[i])
	      if (genericType != ClassHelper.OBJECT_TYPE) {
		ClassNode type2 = new ClassNode(type.name, 1, ClassHelper.make(java.lang.Object))
		oldArgsGenericsTypes = type.getGenericsTypes()
		
		def gen = new GenericsType(genericType)
		type2.setGenericsTypes([gen] as GenericsType[])
		
		args[i].type = type = type2;
	      }
	      
	      def gTypes1 = t1.getGenericsTypes()
	      def gTypes2 = type.getGenericsTypes()
	      if (gTypes1 && gTypes2 &&
		  gTypes1.size() == gTypes2.size()) {
		if (gTypes1.size() == 1 && 
		    gTypes1[0].type == ClassHelper.OBJECT_TYPE && 
		    gTypes2[0].type != ClassHelper.OBJECT_TYPE) { 
		  diffListType = true;
		} else {

		  // generalize element type 
		  
		  for (int j = 0; j < gTypes1.size(); j++) {
		    /*
		      if (gTypes1[j].type.isDerivedFrom(gTypes2[j].type) && 
		      !toBeGeneralized.contains(i) &&
		      m.parameters[i].dynamicTyped) {
		      toBeGeneralized.add(i)
		      }
		      if (!(gTypes1[j].type.isDerivedFrom(gTypes2[j].type) ||
		      gTypes2[j].type.isDerivedFrom(gTypes1[j].type))) { 
		      diffListType = true
		      }
		    */
		    if (gTypes1[j].type != gTypes2[j].type) { 
		      diffListType = true
		    }
		  }
		}
	      } else {
		diffListType = true
	      }
	    } else if (t1 != type) { 
	      if (type.isDerivedFrom(t1)) 
		//If the args are derived from the parameters, that's what we want
		isInheritance = true 
	      if (t1.isDerivedFrom(type)) { 
		//Otherway around, then we have to change the method parameters to the generic type
		//But only do this if this method is a match, so save the index for later
		if (!toBeGeneralized.contains(i) && 
		    parameters[i].dynamicTyped)
		  toBeGeneralized.add(i)
		isInheritance = true
	      }	
	    }
	  }
	  
	  //Try to reduce big decimals to doubles
	  if (ClassHelper.BigDecimal_TYPE == type)
	    type = reduceBigDecimal(args[i])
	  //Also check float vs double, in case reduceBigDecimal went down too far
	  if ((parameters[i].type != type &&
	       !(ClassHelper.float_TYPE == type && 
		 ClassHelper.double_TYPE == parameters[i].type) &&
	       !isInheritance) || 
	      diffListType) {
	    match = false
	    break
	  }
	}
      }
      if (match) {
	def variableScope = vscope //No need to copy, just change
	for (i in toBeGeneralized) {
	  parameters[i].type = args[i].type
	  Variable decl = variableScope?.getDeclaredVariable(parameters[i].name)
	  if (decl) 
	    decl.type = args[i].type
	}
      }
      return match
    }
    return false
  }

  public static boolean isDynamicTyped(Expression e, 
				       VariableScope localVariableScope, 
				       DynamicTypesHelper typeHelper, 
				       String className) {
    boolean isStatic = false;
    //still account for: fieldExpression, propertyExpression, attributeExpression
    switch (e.class) {
    case BinaryExpression:
      return (isDynamicTyped(e.leftExpression, localVariableScope, typeHelper, className) || 
	      isDynamicTyped(e.rightExpression, localVariableScope, typeHelper, className));
    case VariableExpression:
      return e.isDynamicTyped();
    case StaticMethodCallExpression:
      isStatic = true;
    case MethodCallExpression: 
      MethodInfo mInfo = findMethodDef(e, isStatic, localVariableScope)
      MethodNode mNode = mInfo?.method
      if (mNode != null && mNode.isDynamicReturnType()) { 
	String fullName;
	if (isStatic) { 
	  fullName = (className ? (className + '.') : '') + mNode.method
	} else { 
	  fullName = (className ? (className + '.') : '') + mNode.name
	}
	if (!isStatic && !typeHelper?.oldParsedMethods?.contains(fullName))
	  return true
      }
    default: 
      return false;
    }
  }  
  
}


class TypeInferenceVisitor extends CodeVisitorSupport { 

  TypeInferenceVisitor(ClassProcessor cp) { 
    owner = cp
  }
  
  ClassProcessor owner

  void visitBlockStatement(BlockStatement block) { 

  }

  void visitForLoop(ForStatement forLoop) { 

  }

}
