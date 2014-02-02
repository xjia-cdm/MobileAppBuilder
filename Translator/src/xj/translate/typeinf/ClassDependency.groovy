package xj.translate.typeinf

import java.util.List;
import java.util.ArrayList;

import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.*;

import xj.translate.common.ClassProcessor

import static xj.translate.common.ClassProcessor.* 

class ClassDependency { 

  public static class DependencyNode {
    String name;
    List parents = new ArrayList();
    List children = new ArrayList();
  }

  static void findDependencies(ClassProcessor cp,
			       DependencyNode node, 
			       Map<String, DependencyNode> allNodes) {
    cp.staticMethodMap.each { mname, mlist -> 
      if (!cp.isScript || mname != 'main') { 
	mlist?.each { ClassProcessor.MethodInfo minfo -> 
	  //Check for references in the method arguments
	  def params = minfo.method.getParameters()
	  for (int i=0; i<params.length; i++) {
	    def name = params[i].type.name
	    //Remove any package information
	    if (name.lastIndexOf('.') > -1)
	      name = name.substring(name.lastIndexOf('.'))
	    def tempNode = allNodes.get(name)
	    if (tempNode != null && node.name != tempNode.name) {
	      tempNode.parents.add(node)
	      node.children.add(tempNode)
	    }
	  }
	  //Check for references in the code
	  Set<String> dependencies = findDependencies(minfo.method.code, allNodes.keySet())
	  Iterator it = dependencies.iterator();
	  while(it.hasNext()) {
	    String nodeName = it.next(); 
	    if (node.name != nodeName) {
	      allNodes.get(nodeName).parents.add(node)
	      node.children.add(allNodes.get(nodeName))
	    }
	  }
	}
      }
    }

    cp.methodMap.each { mname, mlist -> 
      mlist?.each { ClassProcessor.MethodInfo minfo -> 
	//Check for references in the method arguments
	def params = minfo.method.getParameters()
	for (int i=0; i<params.length; i++) {
	  def name = params[i].type.name
	  //Remove any package information
	  if (name.lastIndexOf('.') > -1)
	    name = name.substring(name.lastIndexOf('.'))
	  def tempNode = allNodes.get(name)
	  if (tempNode != null) {
	    tempNode.parents.add(node)
	    node.children.add(tempNode)
	  }
	}
	//Check for references in the code
	Set<String> dependencies = findDependencies(minfo.method.code, allNodes.keySet())
	Iterator it = dependencies.iterator();
	while(it.hasNext()) {
	  String nodeName = it.next();     
	  allNodes.get(nodeName).parents.add(node)
	  node.children.add(allNodes.get(nodeName))
	} 
      }
    }

    cp.fieldMap.each { fname, finfo -> 
      def fieldType = finfo.field.type.name
      if (fieldType.lastIndexOf('.') > -1)
	fieldType = fieldType.substring(fieldType.lastIndexOf('.'))
      def tempNode = allNodes.get(fieldType)
      if (tempNode != null) {
	tempNode.parents.add(node)
	node.children.add(tempNode)
      }
    }
  }
  

  static Set<String> findDependencies(Statement statement, Set<String> classNames) {
    Set<String> result = new HashSet<String>()

    if (statement == null)
      return result;
	  
    //First look for more statements
    def statements = findStatements(statement)
    for (int i=0; i<statements.size(); i++)
    result.addAll(findDependencies(statements.get(i), classNames))

    //Then account for any expressions or other possible dependencies
    switch (statement.class) {
    case ExpressionStatement: 
    case ReturnStatement:
      result.addAll(findExpressionDependencies(statement.expression, classNames))
      return result;
    case ForStatement:
      if (classNames.contains(statement.variable.type.name))
	result.add(statement.variable.type.name)
      return result;
		  
    case TryCatchStatement:
      for (int i=0; i<statement.getCatchStatements().size; i++) {
	if (classNames.contains(statement.getCatchStatement(i).getExceptionType().name))
	  result.add(statement.getCatchStatement(i).getExceptionType().name)
      }
      return result;
		  
    case IfStatement: 
      result.addAll(findExpressionDependencies(statement.getBooleanExpression(), classNames))
      return result;
		  
    case WhileStatement:
    case DoWhileStatement:
      result.addAll(findExpressionDependencies(statement.booleanExpression, classNames))
      return result;
		  
    case ForStatement:
      result.addAll(findExpressionDependencies(statement.getCollectionExpression(), classNames))
      return result;
		  
    case SwitchStatement:
      result.addAll(findExpressionDependencies(statement.expression, classNames))
      return result;
  		  
    default: 
      return result;
    }
  }

  //Drills down one layer of statements
  static List<Statement> findStatements(Statement statement) {
    List<Statement> result = new ArrayList<Statement>()
    if (statement == null)
      return result;
	  
    switch(statement.class) {
    case ExpressionStatement: 
    case ReturnStatement:
      return result;
		  
    case IfStatement: 
      result.add(statement.getIfBlock())
      result.add(statement.getElseBlock())
      return result;
		  
    case TryCatchStatement:
      result.add(statement.getTryStatement())
      for (int i=0; i<statement.getCatchStatements().size; i++) {
	result.add(statement.getCatchStatement(i).getCode())
      }
      result.add(statement.getFinallyStatement())
      return result;
		  
    case BlockStatement:
      for (int i=0; i<statement.getStatements().size; i++) 
      result.add(statement.getStatements().get(i))
      return result;
		  
    case SynchronizedStatement:
      result.add(statement.getCode())
      return result;
		  
    case WhileStatement:
    case DoWhileStatement:
      result.add(statement.getLoopBlock())
      return result;
    case ForStatement:
      result.add(statement.getLoopBlock())
      return result;
		  
    case SwitchStatement:
      for (int i=0; i<statement.getCaseStatements().size; i++) 
      result.add(statement.getCaseStatement(i).getCode())
      result.add(statement.getDefaultStatement())
      return result;
		  
    default:
      return result;
    }
  }

  static Set<String> findExpressionDependencies(Expression e, Set<String> classNames) {
    Set<String> result = new HashSet<String>()
    //First check generically for .getExpressions(), .getExpression and .getType()
    if (classNames.contains(e.type.name))
      result.add(e.getType().name)
    try {
      def list = e.getExpressions()
      def itr = list.iterator()
      while (itr.hasNext()) {
	result.addAll(findExpressionDependencies(itr.next(), classNames))
      }
    } catch (MissingMethodException mme) {}
    try {
      result.addAll(findExpressionDependencies(e.getExpression(), classNames))
    } catch (MissingMethodException mme) {}

    //Now go through specific cases
    switch (e.class) {
    case DeclarationExpression:
      result.addAll(findExpressionDependencies(e.leftExpression, classNames))
      result.addAll(findExpressionDependencies(e.rightExpression, classNames))
      return result;
    case BinaryExpression:
      result.addAll(findExpressionDependencies(e.leftExpression, classNames))
      result.addAll(findExpressionDependencies(e.rightExpression, classNames))
      return result;
    case ClassExpression:
      if (classNames.contains(e.text))
	result.add(e.text)
      return result;
    case ClosureExpression:
      def list = findStatements(e.getCode())
      for (int i=0; i<list.size(); i++) {
	list.addAll(findStatements(list.get(i)))
	result.addAll(findDependencies(list.get(i), classNames))
      }
      return result;
    case ConstructorCallExpression:
      result.addAll(findExpressionDependencies(e.getArguments(), classNames))
      return result;
    case ElvisOperatorExpression:
    case TernaryExpression:
      result.addAll(findExpressionDependencies(e.getBooleanExpression(), classNames))
      result.addAll(findExpressionDependencies(e.getTrueExpression(), classNames))
      result.addAll(findExpressionDependencies(e.getFalseExpression(), classNames))
      return result;
    case FieldExpression:  
      def f = e.getField()
      result.addAll(findExpressionDependencies(f.getInitialExpression(), classNames))
      result.addAll(findExpressionDependencies(f.getInitialValueExpression(), classNames))
      if (classNames.contains(f.getOwner()?.getNameWithoutPackage()))
	result.add(f.getOwner().getNameWithoutPackage())
      return result;
    case GStringExpression:
      result.addAll(findExpressionDependencies(e.asConstantString(), classNames))
      return result;
    case MapExpression:
    case NamedArgumentListExpression:
      def list = e.getMapEntryExpressions()
      def itr = list.iterator()
      while (itr.hasNext()) {
	result.addAll(findExpressionDependencies(itr.next(), classNames))
      }
      return result;
    case MapEntryExpression:
      result.addAll(findExpressionDependencies(e.getKeyExpression(), classNames))
      result.addAll(findExpressionDependencies(e.getValueExpression(), classNames))
      return result;
    case MethodCallExpression:
      result.addAll(findExpressionDependencies(e.getMethod(), classNames))
      result.addAll(findExpressionDependencies(e.getObjectExpression(), classNames))
      result.addAll(findExpressionDependencies(e.getArguments(), classNames))
      return result;
    case MethodPointerExpression:
      result.addAll(findExpressionDependencies(e.getMethodName(), classNames))
      return result;
    case PropertyExpression:
    case AttributeExpression:
      result.addAll(findExpressionDependencies(e.getObjectExpression(), classNames))
      result.addAll(findExpressionDependencies(e.getProperty(), classNames))
      return result;
    case RangeExpression:
      result.addAll(findExpressionDependencies(e.getFrom(), classNames))
      result.addAll(findExpressionDependencies(e.getTo(), classNames))
      return result;
    case StaticMethodCallExpression:
      result.addAll(findExpressionDependencies(e.getArguments(), classNames))
      if (classNames.contains(e.getOwnerType().name))
	result.add(e.getOwnerType().name)
      return result;
    default:
      return result;
    }
  }

}
