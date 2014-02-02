package xj.mobile.api

import static xj.mobile.util.CommonUtils.capitalize

class AndroidPropertyDef extends PropertyDef {  

  String xmlName

  def getSetterTemplate() {
    setter ?: "\${name}.set${capitalize(this.delegate ?: this.name)}(\${value})"
  }
  
  def getGetterTemplate() {
    getter ?: (type == 'boolean' ? "\${name}.is${capitalize(this.delegate ?: this.name)}()" :
			   "\${name}.get${capitalize(this.delegate ?: this.name)}()" )
  }

}