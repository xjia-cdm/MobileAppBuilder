
package xj.mobile.codegen

import org.codehaus.groovy.ast.*
import org.codehaus.groovy.control.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.ast.builder.AstBuilder

import xj.mobile.common.*
import xj.mobile.model.ModelNode
import xj.mobile.model.sm.Transition
import xj.mobile.model.impl.ClassModel
import xj.mobile.lang.ast.*

import xj.translate.common.Unparser
import xj.translate.common.ModuleProcessor

import static xj.mobile.lang.ast.PrettyPrinter.print
import static xj.mobile.lang.ast.ASTUtils.getClosureParameters
import static xj.mobile.util.CommonUtils.*
import static xj.translate.Logger.info 

class ActionHandler {

  ViewProcessor viewProcessor
  ClassModel classModel

  ActionTransformer transformer

  ActionHandler() { }

  ActionHandler(ViewProcessor viewProcessor, ClassModel classModel) { 
    this.viewProcessor = viewProcessor
	this.classModel = classModel
  }

  void setContext(ViewProcessor viewProcessor, ClassModel classModel = null) { 
    this.viewProcessor = viewProcessor
	this.classModel = classModel ?: viewProcessor?.classModel
	if (viewProcessor)
	  viewProcessor.generator.unparser.setExternalTypeInfo(viewProcessor.typeInfo)
  }

  Statement transformAction(Statement stmt, Parameter[] params, ModelNode widget) {
    transformer.setContext(viewProcessor, widget, params)
    stmt.visit(transformer)
    return stmt
  }

  Expression transformAction(Expression expr, Parameter[] params, ModelNode widget) {
    transformer.setContext(viewProcessor, widget, params)
    return transformer.transform(expr)
  }

  // srcInfo.code is a ClosureExpression
  String generateActionCode(Map srcInfo, ModelNode widget) { 
    String actionCode = null
	def unparser = viewProcessor.generator.unparser
	unparser.vp = viewProcessor 
	unparser.classModel = classModel

    def src = srcInfo?.code
    if (src instanceof ClosureExpression && viewProcessor) { 

	  if (srcInfo.param) { 
		viewProcessor.typeInfo.parameterMap = srcInfo.param		
		unparser.params = srcInfo.param
	  }
	  info '[ActionHandler] srcInfo.param: ' + srcInfo.param?.getClass()
	  info '[ActionHandler] srcInfo.param: ' + srcInfo.param 

      info '[ActionHandler] Action code pre-transform:\n' + print(src, 2)
      def writer = new StringWriter()
      src.code.visit new groovy.inspect.swingui.AstNodeToScriptVisitor(writer)
      info '[ActionHandler] Action unparsed pre-transform:\n' + writer
      info "[ActionHandler] current class: ${ModuleProcessor.currentClassProcessor.name}"

      def code = xj.translate.ASTUtil.copyStatement(src.code, src.variableScope)
      def params = src?.parameters //getClosureParameters(src)

      def transformedAction = transformAction(code, params, widget)
      //info '[ActionHandler] Action code post-transform, before unparse:\n' + print(code, 2)

      info '[ActionHandler] Action params: ' + params
      info '[ActionHandler] Action code post-transform:\n' + print(code, 2)

      actionCode = unparser.unparse(transformedAction)

      info '[ActionHandler] Action unparsed post-transform:\n' + actionCode

      if (srcInfo.decl) { 
		srcInfo.decl.each { d -> 
		  info "[ActionHandler] updates ${d.updates}"
		  if (d.updates) { 
			def updateCode = generateUpdateCode(d.updates, widget, src.variableScope)
			if (updateCode) { 
			  actionCode += "\n${updateCode}"
			}
		  }
		}	
      }

	  viewProcessor.typeInfo.parameterMap = null
	  unparser.params = null
    }

	def updates = srcInfo?.updates
	if (updates) { 
	  def updateCode = generateUpdateCode(updates, widget, src?.variableScope)
	  if (updateCode) { 
		if (actionCode) { 
		  actionCode +=  "\n${updateCode}"
		} else { 
		  actionCode = updateCode
		}
	  }
	}

    return actionCode
  }

  def generateUpdateCode(Set updates, ModelNode widget, VariableScope scope = null) { 
	def updateCode = null

	if (updates) { 
	  updates.each { u -> 
		def w = viewProcessor.getWidget(u[0])
		if (w) { 
		  def updateSrc = w["${u[1]}.src"]?.code
		  if (updateSrc) { 
			def ucode = generateUpdateCode(updateSrc, w, u[0], 
										   u[1], scope)
										   
			if (updateCode) { 
			  updateCode += "\n${ucode}"
			} else { 
			  updateCode = ucode
			}
		  }
		}
	  }
	}
	return updateCode
  }

  // src is an update Expression
  String generateUpdateCode(Expression src, ModelNode widget, 
							String wname, String attribute, 
							VariableScope scope = null) { 
	if (src) { 
	  info "[ActionHandler] unparseUpdateCode() enter ${wname}.${attribute} = ${src}"

	  def unparser = viewProcessor.generator.unparser
	  unparser.classModel = classModel
	  unparser.vp = viewProcessor 

	  info "[ActionHandler] unparseUpdateCode() vp: ${unparser.vp?.viewName} class: ${unparser.classModel?.name} params: ${unparser.params?.keySet()}"

	  def updateExp = xj.translate.ASTUtil.copyExpression(src, scope) 
	  def exp = new SetViewPropertyExpression(viewProcessor.view.id, wname, attribute, updateExp)	  

	  exp.viewType = widget?.widgetType
	  exp.platformViewType = widget.getPlatformWidgetName(viewProcessor.platform)

	  info "[ActionHandler] SetViewPropExp: ${exp.viewType} ${exp.platformViewType} ${viewProcessor.platform}"

	  if (widget?.parent.embedded) exp.owner = 'owner'
	  def stmt = new ExpressionStatement(exp)			
	  def transformedUpdate = transformAction(stmt, null, widget)
	  
	  info "[ActionHandler] update code ${stmt}"
	  def ucode = unparser.unparse(transformedUpdate)
	  info "[ActionHandler] update code unparsed: ${ucode}"

	  info "[ActionHandler] unparseUpdateCode() leave ${src}"
	  return ucode
	}		
	return null
  }

  // src is an update Expression
  String unparseUpdateExp(Expression src, ModelNode widget, 
						  Map params = null,
						  VariableScope scope = null) {  
	if (src) { 
	  info "[ActionHandler] unparseUpdateExp() enter ${src}"

	  def unparser = viewProcessor.generator.unparser
	  unparser.classModel = classModel
	  unparser.vp = viewProcessor 
	  viewProcessor.typeInfo.parameterMap = params

	  def updateExp = xj.translate.ASTUtil.copyExpression(src, scope) 		
	  def transformedUpdate = transformAction(updateExp, null, widget)
	  
	  info "[ActionHandler] unparseUpdateExp() ${updateExp}"
	  def ucode = unparser.unparse(transformedUpdate)
	  info "[ActionHandler] unparseUpdateExp() unparsed: ${ucode}"

	  viewProcessor.typeInfo.parameterMap = null
	  return ucode
	}		
	return null
  }


  String unparseMapExp(MapExpression src, 
					   String var, 
					   ModelNode widget, 
					   Map params = null,
					   VariableScope scope = null) {  
	if (src) { 
	  info "[ActionHandler] unparseMapExp() enter ${src}"

	  def unparser = viewProcessor.generator.unparser
	  unparser.classModel = classModel
	  unparser.vp = viewProcessor 
	  viewProcessor.typeInfo.parameterMap = params

	  def updateExp = xj.translate.ASTUtil.copyExpression(src, scope) 		
	  def transformedUpdate = transformAction(updateExp, null, widget)
	  
	  info "[ActionHandler] unparseMapExp() ${updateExp}"
	  def ucode = unparser.unparseMap(transformedUpdate, var)
	  info "[ActionHandler] unparseMapExp() unparsed: ${ucode}"

	  viewProcessor.typeInfo.parameterMap = null
	  return ucode
	}		
	return null
  }


}