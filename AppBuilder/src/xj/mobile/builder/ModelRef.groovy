
package xj.mobile.builder

class ModelRef { 
  def _model
  ModelRef(model) { 
	//println "new ModelRef() name: ${m.id}"
	this._model = model 
  }

  String toString() {
	_model.id
  }
	
  def propertyMissing(String pname) { 
	//println "ModelRef.propertyMissing() name=${_m.id} pname=${pname} return: ${_m[pname]}"
	return _model[pname]
  }	

}