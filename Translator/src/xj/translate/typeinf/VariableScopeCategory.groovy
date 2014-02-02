package xj.translate.typeinf

import java.util.* 

import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*

 /*
  *  This class augments the VariableScope class
  */
@Category(VariableScope)
class VariableScopeCategory {

  static class VariableInfo {
    String name              // orginal declared name
    String declaredType      // target-language specific
    boolean primitive        // target-language specific
 
    // for local scope only 
    String localName         // if redeclared in local scope 
    ClassNode localType
  }

  static class ClosureInfo { 
    String name
    ClosureExpression closure
    ClassNode returnType

    List<ClosureExpression> myCopies = []
  }

  static class VariableTable {
    Map<String, VariableInfo> variableMap = [:] 
    List<VariableInfo> presetVarInfo = []

    Map<String, ClosureInfo> closureMap = [:] 
  }

  static IdentityHashMap<VariableScope, VariableTable> varTableMap = new IdentityHashMap()

  //
  //  Methods dealing with current type of variables
  //

  ClassNode getCurrentVariableType(String name) { 
    ClassNode type = null
    VariableTable vtable = varTableMap[this]
    if (vtable) { 
      VariableInfo vinfo = vtable.variableMap[name]
      if (vinfo)
	type = vinfo.localType
    }

    // if no local narrowed type search for declared type in the variable scope 
    if (!type) { 
      VariableScope vscope = this
      while (vscope != null) { 
	Variable decl = vscope.getDeclaredVariable(name) 
	if (decl) return decl.type 
	vscope = vscope.parent
      }
    }

    return type
  } 

  VariableInfo getVariableInfo(String name) { 
    VariableTable vtable = varTableMap[this]
    return vtable ? vtable.variableMap[name] : null
  }

  void setCurrentVariableType(String name, ClassNode type) { 
    if (name) { 
      VariableTable vtable = varTableMap[this]
      if (!vtable) 
	varTableMap[this] = vtable = new VariableTable()
      vtable.variableMap[name] = type ? new VariableInfo(name : name, localType : type) : null
    }
  }

  void presetCurrentVariableType(String name, ClassNode type) { 
    if (name) { 
      VariableTable vtable = varTableMap[this]
      if (!vtable) 
	varTableMap[this] = vtable = new VariableTable()
      if (type)
	vtable.presetVarInfo << new VariableInfo(name : name, localType : type)
    }
  }

  void clearCurrentTypes() { 
    VariableTable vtable = varTableMap[this]
    if (vtable) { 
      vtable.variableMap.clear() 
      if (vtable.presetVarInfo) { 
	vtable.presetVarInfo.each { vinfo -> vtable.variableMap[vinfo.name] = vinfo }
	vtable.presetVarInfo.clear()
      }
    }
  }

  //
  //  Methods dealing with closures 
  //

  ClosureInfo getClosureInfo(String name) { 
    VariableTable vtable = varTableMap[this]
    return vtable ? vtable.closureMap[name] : null
  }

  void putClosure(String name, ClosureExpression closure) { 
    if (name) { 
      VariableTable vtable = varTableMap[this]
      if (!vtable) 
	varTableMap[this] = vtable = new VariableTable()
      vtable.closureMap[name] = closure ? new ClosureInfo(name : name, closure : closure) : null
    }
  }


}