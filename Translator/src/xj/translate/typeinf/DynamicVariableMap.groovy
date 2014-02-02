package xj.translate.typeinf

import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*

class DynamicVariableMap { 
  
  Stack<Map<String, ClassNode>> stack = new Stack()

  void pop() { 
    stack.pop() 
  }

  void push() { 
    stack.push([:])
  }

}

