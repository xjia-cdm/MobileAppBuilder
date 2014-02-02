package xj.mobile.test

import org.junit.*
import static org.junit.Assert.*

import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*

import org.codehaus.groovy.ast.builder.AstBuilder 

import static xj.mobile.util.GroovyEvaluator.*

class EvaluatorTest { 

  @Test
  void test1() { 
	def result 
	result = evaluate(new ConstantExpression('abc')) 
	println result 
	assert result == 'abc'

	result = evaluate(new AstBuilder().buildFromString("\"Hello\"")[0]) 
	println result 
	assert result == 'Hello'

	result = evaluate(new AstBuilder().buildFromString("1 + 2")[0]) 
	println result 
	assert result == 3

	result = evaluate(new AstBuilder().buildFromString("x + y")[0], [ x: 100, y: 200 ]) 
	println result 
	assert result == 300

  }


  static void main(args) { 
    println "Test App Builder: Evaluator"
	def test = new EvaluatorTest ()
	test.test1()
  }

}