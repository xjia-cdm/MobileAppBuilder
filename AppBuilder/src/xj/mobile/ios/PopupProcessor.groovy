package xj.mobile.ios

import xj.mobile.*
import xj.mobile.model.ui.*
import xj.mobile.common.ViewProcessor
import xj.mobile.codegen.templates.PopupTemplates
import xj.mobile.codegen.Delegate

import static xj.mobile.common.ViewUtils.*
import static xj.mobile.util.CommonUtils.*
import static xj.mobile.lang.ast.ASTUtils.getClosureParameters
import static xj.translate.Logger.info 

class PopupProcessor extends xj.mobile.common.PopupProcessor { 

  PopupProcessor(ViewProcessor vp) { 
    super(vp)
	popupTemplates = PopupTemplates.getPopupTemplates('ios')
  }

  void process(Widget popup) { 
    String name = getWidgetName(popup)
    info "[PopupProcessor] process ${name}"

    def temp = getPopupTemplate(popup)
    if (temp) { 
      info "[PopupProcessor] found popup template"

      if (temp.delegate) { 
		if (!(temp.delegate in vp.classModel.delegates)) { 
		  def delegate = new Delegate(widgetType: popup.widgetType,
									  delegateName: temp.delegate) 
		  vp.classModel.delegates << temp.delegate
		  vp.classModel.delegateActions[temp.delegate] = delegate
		}
      }

	  def t1 = getDataVarTypeForWidget(popup)
	  def t2 = simpleType(t1)
	  def params = t2 ? [data: t2] : null

      boolean staticText = true
      def eh = null
      if (vp instanceof ListViewProcessor) {
		eh = vp.findEntityHandler(popup)
      }

	  def attrs = [  'title', 'message', 'cancel', 'affirm'  ]
	  def attrValues = vp.generateSetAttributesCode(popup, attrs, 'nil', params)
	  if (eh) { 
		attrs.each { a -> 
		  if (popup["${a}.src"] != null) { 
			staticText = false
			attrValues[a] = generateExpressionCode(popup, a, eh)
		  }
		}
	  }

	  if (attrValues.cancel == 'nil' && popup.widgetType == 'Alert' && !popup.buttons) { 
		attrValues.cancel = '@\"OK\"'
	  }
	  if (attrValues.affirm == 'nil' && !popup.buttons && !popup.children) { 
		attrValues.affirm = '@\"OK\"'
	  }

      def other = 'nil'
      if (popup.children) {
		def items = popup.children.findAll { it.widgetType == 'Item' } 
		if (items.every { it['text.src'] == null }) { 
		  other = items.collect {  "@\"${it.text}\"" }.join(', ') + ', nil'
		} else { 
		  staticText = false
		  other = items.collect {  
			generateExpressionCode(it, 'text', eh)
		  }.join(',\n\t\t\t\t\t') + ', nil'		  
		}
      } else if (popup.buttons) { 
		other = popup.buttons.collect { "@\"${it}\"" }.join(', ') + ', nil'
      }

      if (popup.widgetType == 'Alert' && popup.affirm) { 
		other = attrValues.affirm + ', ' + other
      }
	
      def ctemp = temp.create
      def binding = [ name : name,
					  uiclass : temp.uiclass,
					  other : other ] + attrValues

      def template = engine.createTemplate(ctemp).make(binding)
      def body = template.toString()

	  attrs = getWidgetAttributes(popup, [ 'title', 'message', 'cancel', 'affirm', 'other' ])
	  def attrCode = vp.setAttributes(popup, attrs, vp.classModel)
	  body += attrCode.collect { '\n' + it[1] }.join('')

      if (staticText) { 
		if (t2 == null) {  // no transition data  
		  body = """if (${name} == nil) {
${indent(body)}
}"""
		}
      } else if (eh) { 
		body = eh.getDataScrap() + '\n' + body
      }
	  
      def stemp = temp.show
      def arg = (isMenu(popup.widgetType)) ? ':(NSIndexPath *)indexPath' : ''
	  if (t2) { 
		def type = vp.getTransitionDataType(t2)
		if (arg) { 
		  arg += " withData: (${vp.getTransitionNativeType(type)} *) data"
		} else { 
		  arg = "_withData: (${vp.getTransitionNativeType(type)} *) data"
		}
	  }
      def binding2 = [ name : name,
					   arg : arg,
					   body : body,
					   indent: xj.mobile.util.CommonUtils.&indent ]
      template = engine.createTemplate(stemp).make(binding2)
      vp.classModel.popupActionScrap += template.toString()
	  vp.classModel.declareProperty(temp.uiclass, name)

      handleAction(popup, temp, eh)
    }
  }

  String generateExpressionCode(widget, attr, eh) { 
    if (widget && attr) { 
      def src = widget["${attr}.src"]
      if (src != null && eh) { 
		eh.generateExpressionCode(src)
      }  else { 
		"@\"${widget[attr]}\"" 
      }
    } else { 
      null
    }
  }

  boolean isMenu(wtype) { 
    (vp instanceof ListViewProcessor) && (wtype == 'Menu')
  }

  def getActionTemplateName(wtype) { 
    PopupTemplates.actionTemplateName(isMenu(wtype))
  }

  void handleAction(popup, temp, eh = null) { 
    String actionCode = null
	vp.generator.unparser.entityUnparser = eh
    if (popup.children) {
      def itemActions = []
      popup.children.eachWithIndex { item, i -> 
		def code = vp.widgetProcessor.genActionCode(item)
		if (code) { 
		  itemActions << "if (buttonIndex == ${i}) {\n" + indent(code) + '\n}'
		}
      }
      if (itemActions) { 
		actionCode = itemActions.join(' else ') 
      }
    } else {  
      actionCode = vp.widgetProcessor.genActionCode(popup)
    }
	vp.generator.unparser.entityUnparser = null 

    // handle popup/alerts action with parameters 
    def closure = getActionInfo(popup)?.code
    def params = getClosureParameters(closure)
    def preActionTemplate = temp.delegate_action_pre
    if (actionCode && preActionTemplate && params) { 
      info "[PopupProcessor] preActionTemplate: ${preActionTemplate}"
      info "[PopupProcessor] preActionTemplate params: ${params}"
      def binding = [ param : params[0] ]
      def template = engine.createTemplate(preActionTemplate).make(binding)
      actionCode = "${template}\n" + actionCode
    }
	
    if (eh) { 
	  boolean hasRead = popup.'#readEntity'
	  boolean hasWrite = popup.'#writeEntity'
      if (hasRead || hasWrite) { 
		actionCode = "NSIndexPath *indexPath = ${vp.longPressIndexPathVar};\n" + 
		             eh.getDataScrap() + '\n' + 
					 actionCode
      }
      if (hasWrite)
		actionCode = actionCode + '\n[self.tableView reloadData];\n[self writeData];'
    }

	vp.classModel.injectActionCode(popup.widgetType, getWidgetName(popup), temp, actionCode) 

  }


  
}