package xj.translate.objc

import java.text.MessageFormat as mf

import xj.translate.common.Operators

class OperatorsObjectiveC extends Operators {  
  OperatorsObjectiveC() { 
    specialOperatorMap = [
      '**' : 'pow({0}, {1})'
    ] 

  }

  String getSpecialImportFile() { 
    'math.h'
  }

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

    'instanceof' : '[{0} isKindOfClass: [{1} class]]'

  ]

  static Map stringOperatorMap = [
    '=='  : '[{0} isEqualToString:{1})]',
    '!='  : '![{0} isEqualToString:{1})]',

    '<=>' : '[{0} compare:{1}] != 0',
    '>'   : '[{0} compare:{1}] > 0',
    '>='  : '[{0} compare:{1}] >= 0',
    '<'   : '[{0} compare:{1}] < 0',
    '<='  : '[{0} compare:{1}] <= 0',

    '+'   : '[{0} plus:{1}]',
    '-'   : '[{0} minus:{1}]',
    '*'   : '[{0} multiply:{1}]',
    //'**'  : '{0}.pow({1})',
    //'/'   : '{0}.divide({1})',
    //'%'   : '{0}.remainder({1})',

    '$++' : '[{0} next]',
    '++$' : '[{0} next]',
    '$--' : '[{0} previous]',
    '--$' : '[{0} previous]',

  ]

  static Map listOperatorsMap = [
    '=='  : '[{0} isEqualToArray:{1})]',
    '!='  : '![{0} isEqualToArray:{1})]',

    '+'   : '[{0} plus:{1}]',
    '-'   : '[{0} minus:{1}]',
    '*'   : '[{0} multiply:{1}]',
    '['   : '[{0} objectAtIndex:{1}]',
  ];

  static Map mapOperatorsMap = [:];

  static Map bigDecimalOperatorMap = [
    '=='  : '[{0} isEqualToString:{1})]',
    '!='  : '![{0} isEqualToString:{1})]',

    '<=>' : '[{0} compare:{1}] != 0',
    '>'   : '[{0} compare:{1}] > 0',
    '>='  : '[{0} compare:{1}] >= 0',
    '<'   : '[{0} compare:{1}] < 0',
    '<='  : '[{0} compare:{1}] <= 0',

    '+'   : '[{0} decimalNumberByAdding:{1}]',
    '-'   : '[{0} decimalNumberBySubtracting:{1}]',
    '*'   : '[{0} decimalNumberByMultiplyingBy:{1}]',
    '**'  : '[{0} decimalNumberByRaisingToPower:{1}]',
    '/'   : '[{0} decimalNumberByDividingBy:{1}]',
    //'%'   : '[{0}.remainder:{1}]',
  ]

  static Map bigIntegerOperatorMap = [
    '=='  : '[{0} isEqualToString:{1})]',
    '!='  : '![{0} isEqualToString:{1})]',

    '<=>' : '[{0} compare:{1}] != 0',
    '>'   : '[{0} compare:{1}] > 0',
    '>='  : '[{0} compare:{1}] >= 0',
    '<'   : '[{0} compare:{1}] < 0',
    '<='  : '[{0} compare:{1}] <= 0',

  ]

  String convertListBinaryExpression(String left, String op, String right,
				     boolean needsLeftParen, boolean needsRightParen) { 
    if (left && op && right) { 
      String pat = listOperatorsMap[op]
      if (pat) { 
	if (op in ['+', '-', '*'])
	  owner.addUtilFile('NSMutableArray+Util')
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

  String convertBigDecimalBinaryExpression(String left, String op, String right,
					   boolean needsLeftParen, boolean needsRightParen) { 
    if (left && op && right) { 
      String pat = bigDecimalOperatorMap[op]
      if (pat) { 
	return mf.format(pat, left, right)
      }
      return "[${left} decimalValue] ${op} [${right} decimalValue]"
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
      return "[${left} longLongValue] ${op} [${right} longLongValue]"
    }
    return null
  }


  String convertStringBinaryExpression(String left, String op, String right,
				       boolean needsLeftParen, boolean needsRightParen) {  
    if (left && op && right) { 
      String pat = stringOperatorMap[op]
      if (pat) { 
	owner.addUtilFile('NSString+Util')
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
	  //owner.addUtilFile('StringUtil')
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
	  //owner.addUtilFile('StringUtil')
	}
	return mf.format(pat, exp)
      } else {  
	return "${op}${exp}"
      }
    }
    return null
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

  // converting: obj.class = X
  String convertTypePredicate(String obj, String lhs, String rhs) { 
    return "[${obj} isMemberOfClass: [${rhs} class]]"
  }

}