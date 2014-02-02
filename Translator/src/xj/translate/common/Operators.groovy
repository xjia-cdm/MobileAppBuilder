package xj.translate.common

import java.text.MessageFormat as mf

class Operators {  

  ClassProcessor getOwner() { 
    ModuleProcessor.currentClassProcessor
  }

  /*
    Operator 	 Method
    a + b 	a.plus(b)
    a - b 	a.minus(b)
    a * b 	a.multiply(b)
    a ** b 	a.power(b)
    a / b 	a.div(b)
    a % b 	a.mod(b)
    a | b 	a.or(b)
    a & b 	a.and(b)
    a ^ b 	a.xor(b)
    a++ or ++a 	a.next()
    a-- or --a 	a.previous()
    a[b] 	a.getAt(b)
    a[b] = c 	a.putAt(b, c)
    a << b 	a.leftShift(b)
    a >> b 	a.rightShift(b)

    ~a 	        a.bitwiseNegate()
    -a 	        a.negative()
    +a 	        a.positive() 

    a == b 	a.equals(b) or a.compareTo(b) == 0 **
    a != b 	! a.equals(b)
    a <=> b 	a.compareTo(b)
    a > b 	a.compareTo(b) > 0
    a >= b 	a.compareTo(b) >= 0
    a < b 	a.compareTo(b) < 0
    a <= b 	a.compareTo(b) <= 0 

    switch(a) { case(b) : } 	b.isCase(a)
   */

  // op -> message format
  static Map operatorMap = [
    '=='  : '{0}.equals({1})',
    '!='  : '!{0}.equals({1})',

    '<=>' : '{0}.compareTo({1}) != 0',
    '>'   : '{0}.compareTo({1}) > 0',
    '>='  : '{0}.compareTo({1}) >= 0',
    '<'   : '{0}.compareTo({1}) < 0',
    '<='  : '{0}.compareTo({1}) <= 0',

    '+'   : '{0}.plus({1})',
    '-'   : '{0}.minus({1})',
    '*'   : '{0}.multiply({1})',
    '**'  : '{0}.power({1})',
    '/'   : '{0}.div({1})',
    '%'   : '{0}.mod({1})',
    '|'   : '{0}.or({1})',
    '&'   : '{0}.and({1})',
    '^'   : '{0}.xor({1})',

    '<<'  : '{0}.leftShift({1})',
    '>>'  : '{0}.rightShift({1})',

    '$++' : '{0}.next()',
    '++$' : '{0}.next()',
    '$--' : '{0}.previous()',
    '--$' : '{0}.previous()',

    '['  : '{0}.getAt({1})',
    '[]=' : '{0}.putAt({1})',

    '~$'  : '{0}.bitwiseNegate()',
    '-$'  : '{0}.negative()',
    '+$'  : '{0}.positive()',

  ]

  // map operators in java

  static Map specialOperatorMap = [
    '**' : 'Math.pow({0}, {1})'
  ]

  static Map stringOperatorMap = [
    '=='  : '{0}.equals({1})',
    '!='  : '!{0}.equals({1})',

    '<=>' : '{0}.compareTo({1}) != 0',
    '>'   : '{0}.compareTo({1}) > 0',
    '>='  : '{0}.compareTo({1}) >= 0',
    '<'   : '{0}.compareTo({1}) < 0',
    '<='  : '{0}.compareTo({1}) <= 0',

    //'+'   : 'StringUtil.plus({0}, {1})',
    '-'   : 'StringUtil.minus({0}, {1})',
    '*'   : 'StringUtil.multiply({0}, {1})',
    //'**'  : '{0}.pow({1})',
    //'/'   : '{0}.divide({1})',
    //'%'   : '{0}.remainder({1})',

    '$++' : 'StringUtil.next({0})',
    '++$' : 'StringUtil.next({0})',
    '$--' : 'StringUtil.previous({0})',
    '--$' : 'StringUtil.previous({0})',

  ]

  static Map listOperatorsMap = [
    '=='  : '{0}.equals({1})',
    '!='  : '!{0}.equals({1})',

    '+' : 'Lists.plus({0}, {1})',
    '-' : 'Lists.minus({0}, {1})',
    '*' : 'Lists.multiply({0}, {1})',

    '[' : '{0}.get({1})',

  ];

  static Map mapOperatorsMap = [:];

  static Map bigDecimalOperatorMap = [
    '=='  : '{0}.equals({1})',
    '!='  : '!{0}.equals({1})',

    '<=>' : '{0}.compareTo({1}) != 0',
    '>'   : '{0}.compareTo({1}) > 0',
    '>='  : '{0}.compareTo({1}) >= 0',
    '<'   : '{0}.compareTo({1}) < 0',
    '<='  : '{0}.compareTo({1}) <= 0',

    '+'   : '{0}.add({1})',
    '-'   : '{0}.subtract({1})',
    '*'   : '{0}.multiply({1})',
    '**'  : '{0}.pow({1})',
    '/'   : '{0}.divide({1})',
    '%'   : '{0}.remainder({1})',
  ]

  static Map bigIntegerOperatorMap = [
    '=='  : '{0}.equals({1})',
    '!='  : '!{0}.equals({1})',

    '<=>' : '{0}.compareTo({1}) != 0',
    '>'   : '{0}.compareTo({1}) > 0',
    '>='  : '{0}.compareTo({1}) >= 0',
    '<'   : '{0}.compareTo({1}) < 0',
    '<='  : '{0}.compareTo({1}) <= 0',

    '+'   : '{0}.add({1})',
    '-'   : '{0}.subtract({1})',
    '*'   : '{0}.multiply({1})',
    '**'  : '{0}.pow({1})',
    '/'   : '{0}.divide({1})',
    '%'   : '{0}.remainder({1})',
    '|'   : '{0}.or({1})',
    '&'   : '{0}.and({1})',
    '^'   : '{0}.xor({1})',
  ]

  static Set<String> JAVA_OPERATORS = [ 
    '==', '!=', '>', '>=', '<', '<=', 
    '+', '-', '*', '/', '%', '|', '&', '^',
    '<<', '>>'
  ] as Set;

  static Set<String> JAVA_ASSIGNMENT_OPERATORS = [
    '=', 
    '+=', '-=', '*=', '/=', '%=',
    '&=', '|=', '^=', 
    '<<=', '>>=', 
  ] as Set; 

  String convertListBinaryExpression(String left, String op, String right,
				     boolean needsLeftParen, boolean needsRightParen) { 
    if (left && op && right) { 
      String pat = listOperatorsMap[op]
      if (pat) { 
	if (pat.contains('Lists')) { 
	  owner.addUtilFile('Lists')
	}
	return mf.format(pat, left, right)
      }
      if (needsLeftParen) left = "(${left})"
      if (needsRightParen) right = "(${right})"
      return "${left} ${op} ${right}"
    }
    return null 
  }

  String convertMapBinaryExpression(String left, String op, String right,
				    boolean needsLeftParen, boolean needsRightParen) { 
    if (left && op && right) { 
      String pat = mapOperatorsMap[op]
      if (pat) { 
	return mf.format(pat, left, right)
      }
      if (needsLeftParen) left = "(${left})"
      if (needsRightParen) right = "(${right})"
      return "${left} ${op} ${right}"
    }
    return null 
  }
  
  String convertNumericBinaryExpression(String left, String op, String right,
					boolean needsLeftParen, boolean needsRightParen) { 
    if (left && op && right) { 
      if (!JAVA_OPERATORS.contains(op)) { 
	String pat = specialOperatorMap[op]
	if (pat) { 
	  def imp = specialImportFile
	  if (imp) owner.addImportFile(imp)
	  return mf.format(pat, left, right)
	}
      }
      if (needsLeftParen) left = "(${left})"
      if (needsRightParen) right = "(${right})"
      return "${left} ${op} ${right}"
    }
    return null
  }

  String convertBigDecimalBinaryExpression(String left, String op, String right,
					   boolean needsLeftParen, boolean needsRightParen) { 
    if (left && op && right) { 
      String pat = bigDecimalOperatorMap[op]
      if (pat) { 
	return mf.format(pat, left, right)
      }
      if (needsLeftParen) left = "(${left})"
      if (needsRightParen) right = "(${right})"
      return "${left} ${op} ${right}"
    }
    return null
  }

  String convertBigIntegerBinaryExpression(String left, String op, String right,
					   boolean needsLeftParen, boolean needsRightParen) { 
    if (left && op && right) { 
      String pat = bigIntegerOperatorMap[op]
      if (pat) { 
	return mf.format(pat, left, right)
      }
      if (needsLeftParen) left = "(${left})"
      if (needsRightParen) right = "(${right})"
      return "${left} ${op} ${right}"
    }
    return null
  }

  String convertStringBinaryExpression(String left, String op, String right,
				       boolean needsLeftParen, boolean needsRightParen) { 
    if (left && op && right) { 
      String pat = stringOperatorMap[op]
      if (pat) { 
	if (pat.contains('StringUtil')) { 
	  owner.addUtilFile('StringUtil')
	}
	return mf.format(pat, left, right)
      }
      if (needsLeftParen) left = "(${left})"
      if (needsRightParen) right = "(${right})"
      return "${left} ${op} ${right}"
    }
    return null
  }

  String convertStringPostfixExpression(String exp, String op) {  
    if (exp && op) { 
      String pat = stringOperatorMap['$' + op]
      if (pat) { 
	if (pat.contains('StringUtil')) { 
	  owner.addUtilFile('StringUtil')
	}
	return mf.format(pat, exp)
      } else {  
	return "${exp}${op}"
      }
    }
    return null
  }

  String convertStringPrefixExpression(String exp, String op) {  
    if (exp && op) { 
      String pat = stringOperatorMap[op + '$']
      if (pat) { 
	if (pat.contains('StringUtil')) { 
	  owner.addUtilFile('StringUtil')
	}
	return mf.format(pat, exp)
      } else {  
	return "${op}${exp}"
      }
    }
    return null
  }

  String getSpecialImportFile() { 
    null
  }

  String convertBinaryExpression(String left, String op, String right, 
				 boolean needsLeftParen, boolean needsRightParen) { 
    if (left && op && right) { 
      String pat = operatorMap[op]
      if (pat) { 
	return mf.format(pat, left, right)
      } else {  
	if (needsLeftParen) left = "(${left})"
	if (needsRightParen) right = "(${right})"
	return "${left} ${op} ${right}"
      }
    }
    return null
  }

  String convertPostfixExpression(String exp, String op) {  
    if (exp && op) { 
      String pat = operatorMap['$' + op]
      if (pat) { 
	return mf.format(pat, exp)
      } else {  
	return "${exp}${op}"
      }
    }
    return null
  }

  String convertPrefixExpression(String exp, String op) {  
    if (exp && op) { 
      String pat = operatorMap[op + '$']
      if (pat) { 
	return mf.format(pat, exp)
      } else {  
	return "${op}${exp}"
      }
    }
    return null
  }

  // converting: obj.class = X
  String convertTypePredicate(String obj, String lhs, String rhs) { 
    return "${lhs}.equals(${rhs}.class)"
  }

  boolean isAssignmentOperator(String op) { 
    return JAVA_ASSIGNMENT_OPERATORS.contains(op)
  }

  public static isBooleanType(String op) {
    switch (op) {
    case "==":
    case "!=":
    case "<":
    case "<=":
    case ">":
    case ">=":
    case "&&":
    case "&":
    case "||":
    case "|":
    case "^":
      return true
    default: 
      return false
    }
  }

}
