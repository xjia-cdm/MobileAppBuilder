package xj.mobile.ios

import xj.mobile.*
import xj.mobile.model.ui.*
import xj.mobile.lang.*
import xj.mobile.common.*
import xj.mobile.model.properties.Property
import xj.mobile.model.properties.ModalTransitionStyle
import xj.mobile.codegen.templates.WidgetTemplates
import xj.mobile.codegen.Delegate

import xj.mobile.codegen.CodeGenerator

import static xj.mobile.codegen.CodeGenerator.InjectionPoint.*
import static xj.mobile.codegen.templates.IOSDelegateTemplates.*
import static xj.mobile.codegen.templates.IOSWidgetTemplates.*
import static xj.mobile.common.ViewUtils.*
import static xj.mobile.util.CommonUtils.*

import static xj.translate.Logger.info 

class WidgetProcessor extends xj.mobile.common.WidgetProcessor { 

  WidgetProcessor(ViewProcessor vp) { 
    super(vp)
    widgetTemplates = WidgetTemplates.getWidgetTemplates('ios')
	generator = CodeGenerator.getCodeGenerator('ios')
  }

  void declareWidget(Widget widget) { 
    String name = getWidgetName(widget)
	String widgetType = widget.widgetType

	def classModel = vp.classModel
    def wtemp = getWidgetTemplate(widget)

    if (wtemp && wtemp.uiclass) { 
	  def uiclass = wtemp.uiclass
	  if (wtemp.uiclass instanceof Closure) { 
		uiclass = wtemp.uiclass(widget)
	  }

	  info "[WidgetProcessor] declareWidget ${name} template uiclass ${uiclass}"

	  classModel.declareProperty(uiclass, name)
	}
  }

  void process(Widget widget) { 
    String name = getWidgetName(widget)
	String widgetType = widget.widgetType
	String platformName = widget.getPlatformWidgetName('ios')

	def classModel = vp.classModel
    def wtemp = getWidgetTemplate(widget)

    if (wtemp && wtemp.uiclass) { 
      def template
	  def uiclass = wtemp.uiclass
	  if (wtemp.uiclass instanceof Closure) { 
		uiclass = wtemp.uiclass(widget)
	  }

	  info "[WidgetProcessor] process ${name} template uiclass ${uiclass}"

	  //classModel.declareProperty(uiclass, name)
	  String ivarName = vp.getIVarName(name)

      def binding = [ name: ivarName,
					  widgetType: widgetType,
					  uiclass: uiclass, 
					  frame: widget._frame ? widget._frame[0..3].join(', ') : '0, 0, 0, 0' ]

	  for (int i = 0; ; i++) { 
		if (wtemp["arg${i}"]) { 
		  def val = wtemp["arg${i}"]
		  if (val instanceof Closure) { 
			val = val(widget)
		  }
		  binding["arg${i}"] = val
		} else { 
		  break 
		}
	  } 

	  generator.injectCodeFromTemplateRef(classModel, "${platformName}:header", binding)
	  generator.injectCodeFromTemplateRef(classModel, "${platformName}:framework", binding)	  
	  generator.injectCodeFromTemplateRef(classModel, "${platformName}:delegate", binding)

	  def excludeAttrs = []
	  if (vp.autoLayout) { 
		generator.injectCodeFromTemplateRef(classModel, "${platformName}:autoCreate", binding)
		generator.injectCodeFromTemplateRef(classModel, "${platformName}:autoResize", binding)
	  } else { 
		generator.injectCodeFromTemplateRef(classModel, "${platformName}:create", binding)
		if (wtemp.initWithAttributes)
		  excludeAttrs += wtemp.initWithAttributes
		if (wtemp.needsFrame && widget._frame)
		  generator.injectCodeFromTemplateRef(classModel, "${platformName}:setFrame", binding)
	  }

	  handleAttributes(widget, wtemp, classModel, excludeAttrs, vp.autoLayout as boolean) 

	  if (wtemp.images) { 
		classModel.systemImageFiles.addAll(wtemp.images)
	  }

	  generator.injectCodeFromTemplateRef(classModel, "${platformName}:addSubview", binding)

	  if (vp.autoLayout) { 
		widget['#layout']?.each { c -> 
		  def param = toLayoutParam(c, binding)
		  if (param instanceof List) { 
			param.each { p -> 
			  generator.injectCodeFromTemplateRef(classModel, "${platformName}:autoLayout", 
												  binding + p)
			}
		  } else { 
			generator.injectCodeFromTemplateRef(classModel, "${platformName}:autoLayout", 
												binding + param)
		  }
		  classModel.injectCode(LoadView, ' ') // add a blank line
		}
	  } 

      if (wtemp.processor) { 
		wtemp.processor.process(widget, vp)
	  } else if (wtemp.template) { 
		binding += [ actionCode: genActionCode(widget) ]
		if (wtemp.templateVars instanceof Map) { 
		  wtemp.templateVars.each { var, exp -> 
			if (exp instanceof Closure) { 
			  binding[var] = exp(widget) 
			} else { 
			  binding[var] = exp 
			}
		  }
		}
		generator.injectCodeFromTemplateRef(vp.classModel, wtemp.template, binding)
      } else { 
		handleAction(widget, wtemp) 
	  }

	  /*
	  if (!wtemp.skip_action)
		handleAction(widget, wtemp) 
	  */

      vp.handleImageFiles(widget)
    }
  }

  def toLayoutParam(constraints, binding, boolean eq = true) { 
	if (constraints[1] instanceof List) { 
	  return constraints[1].collect { w -> toLayoutParam([constraints[0], w, constraints[2]], binding, false)}
	} else { 
	  def param = [
		parent: 'self.view',
		item1: binding.name,
		item2: (constraints[1] ? vp.getIVarName(constraints[1]) : 'self.view'),
		attribute1: 'NSLayoutAttributeTop', 
		attribute2: 'NSLayoutAttributeTop', 
		relation: 'NSLayoutRelationEqual',
		multiplier: '1.0',
		constant: constraints[2] ?: 0
	  ]

	  switch (constraints[0]) { 
	  case 'top': 
		param.attribute1 = 'NSLayoutAttributeTop'
		param.attribute2 = 'NSLayoutAttributeTop'
		break;
	  case 'left':
		param.attribute1 = 'NSLayoutAttributeLeft'
		param.attribute2 = 'NSLayoutAttributeLeft' 
		break;
	  case 'right':
		param.attribute1 = 'NSLayoutAttributeRight'
		param.attribute2 = 'NSLayoutAttributeRight'
		param.constant = -constraints[2]
		break;
	  case 'bottom':
		param.attribute1 = 'NSLayoutAttributeBottom'
		param.attribute2 = 'NSLayoutAttributeBottom' 
		param.constant = -constraints[2]
		break;
	  case 'next':
		param.attribute1 = 'NSLayoutAttributeLeft'
		param.attribute2 = 'NSLayoutAttributeRight'
		//param.constant = -constraints[2]
		break;
	  case 'below':
		param.attribute1 = 'NSLayoutAttributeTop'
		param.attribute2 = 'NSLayoutAttributeBottom'
		if (!eq) { 
		  param.relation = 'NSLayoutRelationGreaterThanOrEqual'
		}
		break;
	  }
	  return param
	}
  }

  void handleAttributes(widget, wtemp, classModel, excludeAttrs = null, boolean autoLayout = false) { 
	String name = getWidgetName(widget)
	def defaultAttrCode = []
	def defaultAttributes = [:]
	def defaultTemp 
	if (autoLayout && wtemp.defaultAttributesAutoLayout) {
	  defaultTemp = wtemp.defaultAttributesAutoLayout
	} else {  
	  defaultTemp = wtemp.defaultAttributes
	}
	if (defaultTemp) { 
	  defaultAttributes = defaultTemp
	  if (defaultAttributes instanceof Closure) { 
		defaultAttributes = defaultAttributes(widget)
	  }

	  defaultAttributes.each { attr, value -> 
		def code = generator.generateSetAttributeCode(vp, widget.widgetType, 
													  widget.getPlatformWidgetName(vp.platform),
													  name, 
													  attr, widget[attr], value)
		if (code != null) { 
		  defaultAttrCode << [ code[0], "${code[1]};" ]
		}			  
	  }
	}

	// excluding default attributes
	def attrs = getWidgetAttributes(widget) - defaultAttributes.keySet()
	wtemp.initialAttributes?.each { attr -> 
	  if (attr instanceof List) { 
		// combination attributes with a signle setter 
		if (attr.every{ widget[it] != null }) 
		  attrs.removeAll(attr)		
	  } else { 
		if (widget[attr]) 
		  attrs.remove(attr)
	  }
	}

	def attrCode = vp.setAttributes(widget, attrs, classModel)
	def actualAttrNames = attrCode.collect{ it[0] }
	def code = defaultAttrCode.findAll { !(it[0] in actualAttrNames) }.collect { it[1] }.join('\n')
	generator.injectCodeFromTemplate(classModel, CodeGenerator.InjectionPoint.LoadView, code)

	wtemp.initialAttributes?.each { attr -> 
	  vp.processAttribute(name, widget, attr)
	}

	code = attrCode.findAll { !excludeAttrs || !(it[0] in excludeAttrs) }.collect { it[1] }.join('\n')
	generator.injectCodeFromTemplate(classModel, CodeGenerator.InjectionPoint.LoadView, code)
  }

  void handleAction(widget, wtemp) { 
    String actionCode = ''
    def codeTemplate = getTemplate(wtemp, 'actionCode') 
    if (codeTemplate) { 
      def binding = [ name : widget.id,
					  widget : widget ]
      def template = engine.createTemplate(codeTemplate).make(binding)
      actionCode = template.toString()
    }
    String userCode = genActionCode(widget)
    if (userCode) {
      if (actionCode) { 
		actionCode += ('\n' + userCode) 
      } else { 
		actionCode = userCode
      }
    }

    //String name = getWidgetName(widget)
	//String ivarName = vp.getIVarName(name)
	vp.classModel.injectActionCode(widget.nodeType, getWidgetName(widget), //ivarName, 
								   wtemp, actionCode) 
  }

}




