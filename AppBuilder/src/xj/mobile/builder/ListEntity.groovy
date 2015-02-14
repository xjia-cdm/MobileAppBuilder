
package xj.mobile.builder

import org.codehaus.groovy.ast.ClassNode

import static xj.translate.Logger.info 

class ListEntity extends Expando implements GroovyInterceptable { 
  
  ModelBuilder buidler
  ListEntityDummy dummy
  def entityClass

  def fields = [] // a list of fields, including the inherited fields 

  ListEntity(ModelBuilder builder, args) { 
    super(args)
    this.builder = builder
    dummy = new ListEntityDummy()

    info "[ListEntity] args=${args}"

	// add all the declared field info of the entity class to the list fields
	// include all field in the super class 
    entityClass = xj.mobile.builder.AppBuilder.app.classes[this['class'].name.toString()]
    ClassNode c = entityClass
    while (c && c.name != 'java.lang.Object') { 
      info "[ListEntity] get fields: c=${c.name} ${c.superClass?.name}"
      c.fields.each { f -> fields << f }
      c = xj.mobile.builder.AppBuilder.app.classes[c.superClass.name]
    }
    info "[ListEntity] fields: ${fields.name}"
  }

  def invokeMethod(String name, arg) { 
    if (name == 'each') { 
	  this._dummy_ = getDummyVarName(name, arg)

	  // insert declaration for the dummy 
      def viewDecl = builder.viewStack[-1].declarations
      if (!viewDecl) { 
		viewDecl = [:]
		builder.viewStack[-1].declarations = viewDecl
      }
      viewDecl[this._dummy_] = [ isDummy: true, entity: this ]      

      def e = [ this ].collect(arg[0])
      //println "[ListEntity] ${e}"
      def object = e[0]
      object.'#entity' = this
    } else if (name in ['add', 'delete']) { 
      info "ListEntity ${this.name} ${name}: " + (arg? arg[0] : '()')

    } else { 
      super.invokeMethod(name, arg)
	  //throw new UnsupportedOperationException("ListEntity: ${name}")
	  // call getAt
    }
  }

  def getDummyVarName(String name, arg) { 
    info "[ListEntity] handle() name: ${name}  arg: ${arg}"
	String dummy = null
    if (arg[0] instanceof Closure) { 
      info "[ListEntity] ${arg[0].metaClass}"

      def c = arg[0].metaClass.classNode.getDeclaredMethods("doCall")[0]
      // println c.parameters
      info "[ListEntity] ${c.parameters[0].name}"

      //this._dummy_ = c.parameters[0].name
      //println c.code
	  dummy = c.parameters[0].name
    }
	return dummy 
  }

}

class ListEntityDummy { 
  
} 
