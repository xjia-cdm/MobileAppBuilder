package xj.mobile.model

class Null {
  
  static final NULL_OBJECT = new Null()

  static Null getInstance() { 
  	return NULL_OBJECT
  }

  Null() { }

  def propertyMissing(String name) { 
	return NULL_OBJECT
  }

  def propertyMissing(String name, value) { 
	return NULL_OBJECT
  }

  def methodMissing(String name, args) { 
	return NULL_OBJECT
  }

}