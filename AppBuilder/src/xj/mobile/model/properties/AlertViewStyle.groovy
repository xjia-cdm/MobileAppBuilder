package xj.mobile.model.properties

/*
 *  Generated by xj.mobile.tool.ProcessType
 *  Created on: Wed Dec 11 11:19:25 CST 2013
 */
class AlertViewStyle extends Property { 

  static values = [:]
  static names = [ 'Default', 'SecureTextInput', 'PlainTextInput', 'LoginAndPasswordInput' ]

  static final AlertViewStyle Default = new AlertViewStyle('Default')
  static final AlertViewStyle SecureTextInput = new AlertViewStyle('SecureTextInput')
  static final AlertViewStyle PlainTextInput = new AlertViewStyle('PlainTextInput')
  static final AlertViewStyle LoginAndPasswordInput = new AlertViewStyle('LoginAndPasswordInput')

  String name
  
  private AlertViewStyle(name) { 
    this.name = name
    values[name] = this
  }

  String toIOSString() { 
    "UIAlertViewStyle${name}"
  }

  String toAndroidJavaString() { 
    "AlertViewStyle${name}"
  }

  String toShortString() { 
    name
  }

  String toString() { 
    "AlertViewStyle.${name}"
  }

  static boolean isCompatible(value) { 
	(value instanceof String) || 
	(value instanceof List) 
  }

  static boolean hasValue(name) { 
    values.hasKey(name)
  }

  static AlertViewStyle getValue(name) { 
    values[name]
  }

}