
package xj.translate.common

import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*

/*

  Array subscript	[]
  Member access	        . 
  Postfix		expr++ expr--
  Unary			++expr --expr + - ≈ ! *
  Multiplicative	/ %
  Additive		+ -
  Shift			<< >> >>>
  Relational		< <= > >= instanceof 
  Equality		== != <=>
  Bitwise and 		&
  Bitwise exclusive or  ^
  Bitwise inclusive or	|
  Logical and 	  	&&
  Logical or 		||
  Conditional 		:?
  Assignment		= += -= *= /= %= &= ^= ⎪= <<= >>= >>>=

  Spaceship	 	<=>	   Useful in comparisons
  Regex find	 	=~	   Find with a regular expresion? See ﻿﻿﻿Regular Expressions
  Regex match	 	==~	   Get a match via a regex? See ﻿Regular Expressions
  Java Field Override	.@	   Can be used to override generated properties to provide access to a field
  Spread	   	*.	   Used to invoke an action on all items of an aggregate object
  Spread Java Field	*.@	   Amalgamation of the above two
  Method Reference	.&	   Get a reference to a method, can be useful for creating closures from methods
  asType	 	as	   Used for groovy casting, coercing one type to another.
  Membership	 	in	   Can be used as replacement for collection.contains()
  Identity Operator	is	   Identity check. 
  Safe Navigation	?.	   returns nulls instead of throwing NullPointerExceptions
  Elvis Operator	?:	   Shorter ternary operator

  Range 		..
 */

class OperatorPrecedence {  

  // return [ boolean, boolean]: whether left/right sub-expression needs parentheses 
  //                             in unparsing based on operator precedence
  static def needsParentheses(BinaryExpression exp) { 
    if (exp) { 
      return needsParentheses(exp.leftExpression, exp.operation.text, exp.rightExpression)
    }
    return null
  }

  static def needsParentheses(Expression leftExp, String op, Expression rightExp) { 
    if (leftExp && rightExp && op) { 
      int p0 = getOperatorPrecedence(op)
      int p1 = getPrecedence(leftExp)
      int p2 = getPrecedence(rightExp)
      boolean left = p1 > p0
      boolean right = p2 >= p0
      return [left, right]
    }
    return null
  }

  static int getPrecedence(Expression exp) { 
    if (exp) { 
      switch (exp.class) { 
      case BinaryExpression:
	return getOperatorPrecedence(exp.operation.text)

      case BitwiseNegationExpression:
	return getOperatorPrecedence('~_')
      case NotExpression:
	return getOperatorPrecedence('!_')
      case UnaryMinusExpression:
	return getOperatorPrecedence('-_')
      case UnaryPlusExpression:
	return getOperatorPrecedence('+_')

      case PrefixExpression:
	return getOperatorPrecedence(exp.operation.text + '_')

      case PostfixExpression:
	return getOperatorPrecedence('_' + exp.operation.text)

      case ElvisOperatorExpression:
	return getOperatorPrecedence('?:')

      case TernaryExpression:
	return getOperatorPrecedence('_?_:_')
      }
    }
    return 0 
  }

  static int getOperatorPrecedence(String op) { 
    if (op) { 
      def p = precedenceTable[op]
      if (p) return p
    }
    return 0
  }

  static Map precedenceTable = [
    '[]'  : 1,  // Array subscript

    '.'   : 2,  // Member access
    '?.'  : 2,  // Safe navigation 

    '_++' : 3,  // Postfix
    '_--' : 3,
  			
    '++_' : 4,  // Unary prefix 
    '--_' : 4,
    '+_'  : 4,
    '-_'  : 4,
    '~_'  : 4,
    '!_'  : 4,

    '**'  : 5,  // Power

    '/'   : 6,  // Multiplicative
    '*'   : 6,
    '%'   : 6,

    '+'   : 7,  // Additive
    '-'   : 7,

    '<<'  : 8,  // Shift	
    '>>'  : 8,
    '>>>' : 8,
    '..'  : 8,  // Range  
    '..<' : 8, 

    '<'   : 9,  // Relational
    '<='  : 9,
    '>'   : 9,
    '>='  : 9,
    'as'  : 9,
    'in'  : 9,
    'instanceof' : 9,

    '=='  : 10,  // Equality
    '!='  : 10,
    '<=>' : 10,
    'is'  : 10,

    '=~'  : 11,  // regex
    '==~' : 11,  

    '&'   : 12,  // Bitwise and 
  
    '^'   : 13,  // Bitwise exclusive or 

    '|'   : 14,  // Bitwise inclusive or 

    '&&'  : 15,  // Logical and

    '||'  : 16,  // Logical or 

    '?:'  : 17,  // Elvis

    '_?_:_' : 18,  // Ternary conditional
  
    '='    : 19,  // Assignment
    '+='   : 19,
    '-='   : 19,
    '*='   : 19,
    '/='   : 19,
    '%='   : 19,
    '&='   : 19,
    '^='   : 19,
    '⎪='   : 19,
    '<<='  : 19,
    '>>='  : 19,
    '>>>=' : 19,
  ]

}