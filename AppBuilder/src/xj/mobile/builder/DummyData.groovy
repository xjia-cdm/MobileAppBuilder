
package xj.mobile.builder

import static xj.translate.Logger.info 

class DummyData extends Expando 
implements GroovyInterceptable { 

  //extends ListEntity { 
  /*
 static theInstance = null

  static DummyData getInstance(ModelBuilder builder) { 
	if (!theInstance)
	  theInstance = new DummyData()
	theInstance.builder = builder 
	return theInstance 
  }
  */

  DummyData(ModelBuilder builder) { 
	this.builder = builder 
  }

  @Delegate String sdata = 'data'

  ModelBuilder builder

  def getAt(i) { null }
  void putAt(i, v) {}
  def getProperty(String name) { this }
  void setProperty(String name, value) {}
  def propertyMissing(String name) { this } 
  String toString() { sdata }

  def invokeMethod(String name, arg) { 
    if (name == 'each') { 
      this._dummy_ = getDummyVarName(name, arg)

	  /*
      def viewDecl = builder.viewStack[-1].declarations
      if (!viewDecl) { 
		viewDecl = [:]
		builder.viewStack[-1].declarations = viewDecl
      }
      viewDecl[this._dummy_] = [ isDummy: true, entity: this ]      
	  */

      def e = [ this ].collect(arg[0])
      //println "[ListEntity] ${e}"
      //def object = e[0]
      //object.'#entity' = this
	  
    } else if (name in ['add', 'delete']) { 
      info "DummyData ${this.name} ${name}: " + (arg? arg[0] : '()')

    } else { 
      super.invokeMethod(name, arg)
	  // throw new UnsupportedOperationException("DummyData: ${name}") 
	  // call getAt, asBoolean
    }
  }
  
  def getDummyVarName(String name, arg) { 
    info "[DummyData] handle() name: ${name}  arg: ${arg}"
	String dummy = null
    if (arg[0] instanceof Closure) { 
	  info "[DummyData] ${arg[0].metaClass}"

      def c = arg[0].metaClass.classNode.getDeclaredMethods("doCall")[0]
      // println c.parameters
      //info "[ListEntityHandler] ${c.parameters[0].name}"
	  info "[DummyData] ${c.parameters[0].name}"

      //this._dummy_ = c.parameters[0].name
      //println c.code
	  dummy = c.parameters[0].name
    }
	return dummy 
  }

}