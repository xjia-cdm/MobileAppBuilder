package xj.translate.typeinf

import java.util.* 

import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*

@Category(Expression)
class TypeCategory {

  static class TypeInfo {
    ClassNode type 
  }

  static IdentityHashMap<Expression, TypeInfo> typeMap = new IdentityHashMap()

  ClassNode getActualType() { 
    TypeInfo ti = typeMap.get(this)
    if (ti && ti.type) { 
      return ti.type
    }
    return null
  } 

  void setActualType(ClassNode type) { 
    TypeInfo ti = typeMap.get(this)
    if (ti) { 
      ti.type = type 
    } else { 
      typeMap.put(this, new TypeInfo(type : type))
    }
  }

}