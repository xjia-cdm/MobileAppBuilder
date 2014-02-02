package xj.translate.objc

import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilationUnit.PrimaryClassNodeOperation
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.control.CompilationFailedException
import groovy.text.GStringTemplateEngine

import static org.codehaus.groovy.ast.ClassHelper.*
import static org.apache.commons.lang3.StringUtils.*

import xj.translate.*
import xj.translate.common.*
import xj.translate.java.JavaClassProcessor
import xj.translate.typeinf.TypeUtil

import static xj.translate.common.Unparser.*
import static xj.translate.typeinf.TypeUtil.*
import static xj.translate.Logger.* 

import static xj.translate.typeinf.MethodHelper.transformMathod

class ObjectiveCClassProcessor extends JavaClassProcessor { 

  ObjectiveCClassProcessor(ModuleNode moduleNode,
			   ClassNode classNode,
			   String infile,
			   Templates templates, 
			   Unparser unparser) { 
    super(moduleNode, classNode, infile, templates, unparser)
  }

  def init() { 
    super.init()

    methodMap.keySet().each { mname -> 
      def mlist = methodMap[mname]
      mlist?.each { MethodInfo minfo -> 
	MethodNode m = minfo.method

      }
    }
    

    //classNode.visitContents(new ObjectiveCVisitor(this))

    /*
    for (c in useTypeSet + hasTypeSet + useStaticTypeSet + hasStaticTypeSet) { 
      String header = ObjectiveCModuleProcessor.headerFileMap[c]
      if (header) addImportFile(header)
    }
    */
  }

  String getExtendsString() { 
    if (classNode.superClass && !isScript) {	
      if (classNode.isEnum() && classNode.superClass.name != 'java.lang.Enum' ||
	  !classNode.isEnum() && classNode.superClass.name != 'java.lang.Object') { 
	return ': ' + normalizeTypeName(classNode.superClass) + ' '      
      }
    }
    return ': NSObject'
  }

  String getImplementsString() { 
    if (!classNode.isInterface() && 
	classNode.allInterfaces) { 
      return '< ' + classNode.allInterfaces.collect { inf -> normalizeTypeName(inf) }.join(', ') + ' > '
    }
    return ''
  }

  def publicInstanceVarScrap    = ''
  def protectedInstanceVarScrap = ''
  def privateInstanceVarScrap   = ''
  def packageInstanceVarScrap   = ''

  def propertyDeclScrap         = ''
  def propertyImplScrap         = ''

  def constructorDeclScrap      = ''
  def methodDeclScrap           = ''
  def staticMethodDeclScrap     = ''
  def initializerScrap          = ''
  def staticInitializerScrap    = ''

  def typedefScrap              = ''
  def constantDeclScrap         = ''
  def innerClassInfScrap        = ''

  void buildScraps() { 
    super.buildScraps() 
    fieldDeclScrap = publicInstanceVarScrap + 
                     protectedInstanceVarScrap + 
		     privateInstanceVarScrap +
                     packageInstanceVarScrap    

    generateInitializer()
  }

  def handleInnerClasses() { 
    classNode.innerClasses?.each { ic -> 
      def cp = ModuleProcessor.getClassProcessor(ic.nameWithoutPackage)
      ModuleProcessor.currentClassProcessor = cp
      cp.buildScraps()
      if (!ic.isInterface() && !ic.isEnum()) { 
	innerClassDefScrap += cp.generateCode()
      }	
      innerClassInfScrap += cp.generateHeaderCode()
      ModuleProcessor.currentClassProcessor = this
    }
  }

  String getterName(String fname) { 
    fname
  }

  void handleStaticField(FieldInfo finfo) { 
    if (finfo) { 
      FieldNode f = finfo.field 
      def fname = f.name
      def ftype = typeName(f.type)

      info "[ObjectiveCClassProcessor] handelStaticField ${fname} ${ftype} final=${f.final}"

      if (f.final) { 
	constantDeclScrap += generateFieldCode(finfo)
      } else { 
	staticFieldDeclScrap += generateFieldCode(finfo)
      }

      if (getterNeeded(f)) { 
	def binding = [ 'type'       : ftype, 
			'name'       : fname,
			'mod'        : '+ ',
			'methodName' : getterName(fname)
		      ]
	def template = engine.createTemplate(templates.getterDecl).make(binding)
	staticMethodDeclScrap += template.toString()

	template = engine.createTemplate(templates.getterDef).make(binding)
	def getterTemplate = template.toString()
	staticMethodDefScrap += getterTemplate
      }
      
      if (setterNeeded(f)) { 
	def binding = [ 'type'       : ftype, 
			'name'       : fname,
			'mod'        : '+ ',
			'methodName' : setterName(fname)
		      ]
	def template = engine.createTemplate(templates.setterDecl).make(binding)
	staticMethodDeclScrap += template.toString()
	template = engine.createTemplate(templates.setterDef).make(binding)
	def setterTemplate = template.toString()
	staticMethodDefScrap += setterTemplate
      }
    }
  }

  void handleField(FieldInfo finfo) { 
    if (finfo) { 
      FieldNode f = finfo.field 
      def fname = f.name
      def ftype = typeName(f.type)

      def ivar = generateFieldCode(finfo)
      int mod = f.modifiers
      if (java.lang.reflect.Modifier.isPrivate(mod)) { 
	privateInstanceVarScrap += ivar
      } else if (java.lang.reflect.Modifier.isPublic(mod)) { 
	publicInstanceVarScrap += ivar
      } else if (java.lang.reflect.Modifier.isProtected(mod)) { 
	protectedInstanceVarScrap += ivar
      } else { 
	packageInstanceVarScrap += ivar
      }

      if (fname != 'this$0') { 
	def attr = ''
	if (ftype == 'id' || ftype.endsWith('*')) 
	  attr = '(nonatomic, retain)'
	def binding = [ 'type'      : ftype, 
			'name'      : fname,
			'attribute' : attr ]
	def template = engine.createTemplate(templates.propertyDeclaration).make(binding)
	propertyDeclScrap += template.toString()
	template = engine.createTemplate(templates.propertyImplementation).make(binding)
	propertyImplScrap += template.toString()
      }
    }
  }

  def generateFieldCode(FieldInfo finfo) { 
    if (finfo) { 
      FieldNode f = finfo.field 
      def fname = f.name
      def ftype = typeName(f.type)
      def finit = ''
      if (f.initialExpression) {
	def exp = unparser.unparse(f.initialExpression) 
	if (f.static) { 
	  if (f.initialExpression instanceof ConstantExpression) { 
	    finit = " = ${exp}"
	  } else { 	  
	    def init = "${fname} = ${exp};"
	    if (staticInitializerScrap.length() == 0) { 
	      staticInitializerScrap = init
	    } else { 
	      staticInitializerScrap += ('\n' + init)
	    }
	  }
	} else { 
	  def init = "${fname} = ${exp};"
	  if (initializerScrap.length() == 0) { 
	    initializerScrap = init
	  } else { 
	    initializerScrap += ('\n' + init)
	  }
	}
      } 
	
      def binding = [ 'type' : ftype, 
		      'name' : fname,
		      'mod'  : f.static ? 'static ' : '', 
		      'init' : finit
		    ]
      def template = engine.createTemplate(templates.variableDeclaration).make(binding)
      return template.toString()
    }
    ''
  }

  void generateInitializer() { 
    def initCode = ''
    def initDecl = ''

    if (constructors.isEmpty() && 
	initializerScrap.length() > 0) { 
      def sig = "-(id) init"
      def body = 
"""  if (self = [super init]) {
${indent(initializerScrap, 2)}
  }
  return self;"""
      def binding = [ 'signature' : sig,
		      'body'      : body,
		    ]
      def template = engine.createTemplate(templates.methodDefinition).make(binding)
      constructorDefScrap = template.toString()
      initDecl =
"""
$sig;
"""
    }

    if (staticInitializerScrap.length() > 0) { 
      def sig = "+(void) initialize"
      def body = 
"""  if (self == [${name} class]) {
${indent(staticInitializerScrap, 2)}
  }"""
      def binding = [ 'signature' : sig,
		      'body'      : body,
		    ]
      def template = engine.createTemplate(templates.methodDefinition).make(binding)
      initCode += template.toString()
      initDecl +=
"""
$sig;
"""
    }

    methodDefScrap = initCode +  methodDefScrap 
    methodDeclScrap = initDecl +  methodDeclScrap 
  }

  def generateMethodSignature(MethodNode m, boolean overloaded) { 
    if (m) { 
      def mst = (m.static ? '+' : '-')
      def rtype =  m instanceof ConstructorNode ? 'id' : typeName(m.returnType)
      def param = unparser.parameters(m.parameters, '', overloaded)
      def name = m instanceof ConstructorNode ? 'init' : m.name
      if (m.static && 'main' == name) { 
	return 'int main(int argc, char* argv[])'
      }      
      if (!m.static && 'toString' == name) { 
	name = 'description'
      }
      def binding = [ 'modifiers'  : mst,
		      'returnType' : rtype, 
		      'name'       : name,
		      'params'     : param,
		    ]
      def template = engine.createTemplate(templates.methodSignature).make(binding)
      return template.toString()
    }
    return ''
  }

  def generateMethodDef(MethodNode m, boolean overloaded) { 
    if (m) { 
      def methodSig = generateMethodSignature(m, overloaded)
      def body = unparser.unparse(m.code, 1)
      if (m.static && 'main' == m.name) { 
	body = """  NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
${body}
  [pool release];"""
      }           
      def binding = [ 'signature' : methodSig,
		      'body'      : body,
		    ]
      def template = engine.createTemplate(templates.methodDefinition).make(binding)
      return template.toString()
    }
    return ''
  }

  void handleStaticMethod(MethodInfo minfo, boolean overloaded) { 
    if (minfo) { 
      MethodNode m = minfo.method
      staticMethodDeclScrap  += generateMethodDecl(m, overloaded)
      staticMethodDefScrap += generateMethodDef(m, overloaded)
    }
  }

  void handleMethod(MethodInfo minfo, boolean overloaded) { 
    if (minfo) { 
      MethodNode m = minfo.method
      methodDeclScrap += generateMethodDecl(m, overloaded)
      if (!classNode.interface && !minfo.method.abstract) { 
	methodDefScrap += generateMethodDef(m, overloaded)
      }
    }
  }

  void handleConstructor(ConstructorNode c, boolean overloaded) { 
    if (c && c.code) {       
      constructorDeclScrap += generateMethodDecl(c, overloaded)

      def sig = generateMethodSignature(c, overloaded)
      def s1 = c.code.statements[0]
      def init
      if (s1 && s1.class == ExpressionStatement && 
	  s1.expression.class == ConstructorCallExpression) { 
	init = unparser.unparse(s1.expression)
	c.code.statements.remove(0)
      } else { 
	init = '[super init]'
      }
      
      def ibody = ''
      if (initializerScrap.length() > 0) { 
	ibody = """${indent(initializerScrap, 2)}
"""
      } 
      def cbody = unparser.unparse(c.code, 2)
      def body = 
"""  if (self = ${init}) {
${ibody}${cbody}
  }
  return self;"""
      def binding = [ 'signature' : sig,
		      'body'      : body,
		    ]
      def template = engine.createTemplate(templates.methodDefinition).make(binding)
      constructorDefScrap += template.toString()
    }    
  }

  def generatePreamble(String outfile, 
		       String headerfile, 
		       String message) { 
    return generatePreamble(outfile, message, 
			    [ headerfile ] + importList, 
			    null)

  }

  def generateHeaderPreamble(String outfile, 
			     String message = null) { 
    return generatePreamble(outfile, message, 
			    defaultImportList + headerImportList,
			    forwardDeclList) 
  }

  String generateCode() { 
    if (classNode) { 
      //buildScraps()

      def binding = [ 'name'         : name,
		      'extend'       : extendsString,
		      'implement'    : implementsString,
		      'staticVar'    : staticFieldDeclScrap,
		      'propertyImpl' : propertyImplScrap,
		      'methodImpl'   : constructorDefScrap + methodDefScrap + staticMethodDefScrap,
		      'innerClass'   : innerClassDefScrap,
		    ]
      def template = engine.createTemplate(templates.classDef).make(binding)
      return template.toString()
    }
    return null
  }

  def generateHeaderCode() { 
    if (classNode.interface) { 
      def binding = [ 'name'         : name,
		      'implement'    : implementsString,
		      'methodDecl'   : constructorDeclScrap + methodDeclScrap,
		      'constantDecl' : typedefScrap + constantDeclScrap,
		      'innerClass'   : innerClassInfScrap,
		    ]
      def template = engine.createTemplate(templates.protocol).make(binding)
      return template.toString()
    } else if (classNode.enum) { 
      def binding = [ 'name'   : name,
		      'values' : enumValueScrap,
		    ]
      def template = engine.createTemplate(templates.enumDef).make(binding)
      return template.toString()      
    } else { 
      def binding = [ 'name'         : name,
		      'extend'       : extendsString,
		      'implement'    : implementsString,
		      'instanceVar'  : indent(fieldDeclScrap, 1),
		      'propertyDecl' : propertyDeclScrap,
		      'methodDecl'   : constructorDeclScrap + methodDeclScrap + staticMethodDeclScrap,
		      'constantDecl' : typedefScrap + constantDeclScrap,
		      'innerClass'   : innerClassInfScrap,
		    ]
      def template = engine.createTemplate(templates.classInf).make(binding)
      return template.toString()
    }
  }

  String getPackageDeclaration() { '' }

  String generateImports(List impfiles = null) { 
    if (impfiles && !impfiles.isEmpty()) { 
      return impfiles.sort().collect { f -> "#import <${f}>" }.join('\n')
    }
    ''
  }

  void generateBuildFile(appname, mainclass) {   
    File buildfile = Config.instance.getBuildFile()
    def files = ModuleProcessor.outputFiles.findAll { f -> f.endsWith('.m') }.join(' ')
    def binding = [ 'appname'  : appname,
		    'filelist' : files
		  ] 
    def template = engine.createTemplate(templates.makefile).make(binding)
    def code = template.toString()

    println "Write make file to ${buildfile}"
    println '=========================================================='
    println code
    println '==========================================================\n'

    buildfile.write(code)
  }

  // used in header preamble 
  def getHeaderImportList() { 
    def uselist = [] as Set
    for (c in superTypeSet) {  
      def name = c.nameWithoutPackage
      if (ObjectiveCModuleProcessor.classes.contains(name) && 
	  name != classNode.nameWithoutPackage) { 
	uselist << name + '.h'
      }
    }
    return uselist
  }

  // used in body preamble 
  def getImportList() { 
    def uselist = [] as Set
    for (c in useTypeSet + useTypeSetLocal + hasTypeSet + useStaticTypeSet + hasStaticTypeSet) { 
      //while (c.outerClass) c = c.outerClass
      def name = c.nameWithoutPackage
      if (name != classNode.nameWithoutPackage) { 
	if (ObjectiveCModuleProcessor.classes.contains(name)) { 
	  uselist << name + '.h'      
	} else { 
	  String header = ObjectiveCModuleProcessor.headerFileMap[name]
	  if (header) { 
	    uselist << header
	  }
	}
      }
    }
    info "[ObjectiveCClassProcessor] ModuleProcessor.classes: ${ModuleProcessor.classes}"
    info "[ObjectiveCClassProcessor] uselist: ${uselist}"

    return importFiles + uselist
  }  

  // used in header 
  List getForwardDeclList() { 
    def uselist = [] as Set
    for (c in useTypeSet + hasTypeSet + useStaticTypeSet) { 
      def name = c.nameWithoutPackage
      if (ObjectiveCModuleProcessor.classes.contains(name) && 
	  !superTypeSet.contains(c) &&
	  name != classNode.nameWithoutPackage) { 
	uselist << name
      }
    }
    for (c in hasStaticTypeSet) { 
      def name = c.nameWithoutPackage
      if (ObjectiveCModuleProcessor.classes.contains(name) && 
	  !superTypeSet.contains(c)) {  
	uselist << name
      }
    }
    if (classNode.innerClasses) {  
      for (c in classNode.innerClasses) { 
	if (!java.lang.reflect.Modifier.isStatic(c.modifiers)) { 
	  uselist << classNode.nameWithoutPackage
	}
      }
    }
    return uselist as List
  }

  String generateForwardDecl(List forwardDecls = null) {
    if (forwardDecls && !forwardDecls.empty) { 
      return '\n\n' + forwardDecls.sort().collect { c -> "@class ${c};" }.join('\n')
    }
    '' 
  }

  String findCreator(String tname, List argTypes) { 
    String ctor = null
    if (tname && argTypes && !argTypes.isEmpty()) { 
      def clist = creatorMap[tname] 
      if (clist) { 
	for (cp in clist) { 
	  if (matchArgTypes(argTypes, cp[0])) { 
	    return cp[1]
	  }
	}
      }
    }
    return ctor
  }

  boolean matchArgTypes(t1, t2) { 
    if (t1 && t2 && t1.size() == t2.size()) { 
      for (int i in 0 ..< t1.size()) { 
	if (!isAssignableFrom(t2[i], t1[i])) 
	  return false
      }
      return true
    }
    return false
  }

  String typeName(ClassNode c, boolean mapTypes = true) { 
    String tname = ''
    if (c) {
      def cname = c.name
      int k = cname.indexOf('<')
      if (k > 0) { 
	cname = cname.substring(0, k)
      }      
      tname = typeName(cname, c.enum, mapTypes) 
    }
    return tname 
  }

  String JavaTypeName(ClassNode c) { 
    String tname = ''
    if (c) {
      tname = typeName(c.name, c.enum, false) 
      GenericsType[] gt = c.genericsTypes
      if (gt && !TypeUtil.allObjectTypes(gt)) {  
	int k = tname.indexOf('<')
	if (k > 0) { 
	  tname = tname.substring(0, k) 
	}
	tname = tname + '<' + gt.collect { t -> typeName(t.name, false, false) }.join(',') + '>'
      }
    }
    return tname 
  }

  String typeName(String tname) { 
    return typeName(tname, ModuleProcessor.isEnum(tname), true)
  }

  String typeName(String tname, boolean isEnum, boolean mapTypes) { 
    tname = super.typeName(tname, false)
    int k = tname.indexOf('<')
    if (k > 0) { 
      tname = tname.substring(0, k)
    }      

    if (tname == 'Object') {  
      tname = 'id'
    } else { 
      def t1 = primitiveTypeMap[tname]
      if (t1) { 
	tname = t1
      } else { 
	if (mapTypes) { 
	  t1 = findNSType(tname) //objectTypeMap[tname]
	  if (t1) { 
	    tname = t1 + '*'
	    if (isException(t1)) { 
	      addUtilFile('ExceptionUtil')
	    }
	  } else { //if (ObjectiveCModuleProcessor.classes.contains(tname)) { 
	    if (!isEnum) { 
	      tname = tname + '*'
	    }
	  }
	}
      }
      if (isEnum) { 
	int i = tname.lastIndexOf('$')
	if (i >= 0) tname = tname.substring(i + 1)
      }
    }
    return tname
  }

  String mapTypeName(String tname) { 
    /*
    if (tname.startsWith('groovy.')) { 
      if (typeMap.containsKey(tname)) 
	tname = typeMap[tname]
    }

    if (tname.startsWith('java.util.')) { 
      tname = tname.substring(10)
    }
    */

    return tname
  }
  
  String findNSType(tname) { 
    String t = null
    if (tname) { 
      t = objectTypeMap[tname]
      if (t == null) { 
	int i = tname.lastIndexOf('.')
	if (i > 0) { 
	  String t1 = tname.substring(i + 1)
	  String pkg = tname.substring(0, i)
	  if (packageMap[t1] == pkg) { 
	    t = objectTypeMap[t1]
	  }	    
	}
      } 
    }
    return t
  }
  
  String normalizeVariableName(String name) { 
    def newName = specialNameMap[name]
    info "[ObjectiveCClassProcessor] normalizeVariableName ${name} -> ${newName}"

    if (newName) return newName
    return name
  }

  void addUtilFile(file) { 
    if (file && !utilFiles.contains(file)) { 
      utilFiles << file
      def ufile = utilFileMap[file]
      if (!ufile) { 
	ufile = file
      }
      addImportFile(ufile + '.h')
      ModuleProcessor.addUtilFile(ufile + '.h')
      ModuleProcessor.addUtilFile(ufile + '.m')      
    } 
  }

  boolean isFilteredField(FieldNode f) { 
    if (classNode.outerClass != null && 
	f.name == 'this$0') return false
    return super.isFilteredField(f) 
  }

  static boolean isObjectType(String tname) { 
    tname && (tname == 'id' || tname.endsWith('*')) 
  }

  static boolean isNSType(String tname) { 
    tname && (tname == 'id' || tname.endsWith('*') && tname.startsWith('NS')) 
  }

  static boolean isNSNumberType(String tname) { 
    tname && (tname == 'id' || tname.startsWith('NSNumber') || tname.startsWith('NSDecimalNumber')) 
  }

  /* Strip * suffix */
  static String baseTypeName(String tname) { 
    if (tname) { 
      int i = tname.indexOf('*')
      if (i > 0) { 
	tname = tname.substring(0, i).trim()
      }
    }
    return tname
  }

  static boolean isException(t) { 
    exceptions.contains(t)
  }

  static Map utilFileMap = [
    //'StringUtil' : 'NSString+Util',
    'ExceptionUtil' : 'NSException+Util',
  ]

  static def defaultImportList = [ 
    'Foundation/Foundation.h', 
    //'Foundation+Util.h'
  ]
  static def defaultSuperClass = 'NSObject'

  static def primitiveTypeMap = [ 
    'boolean' : 'BOOL',
    'int'     : 'int',
    'float'   : 'float',
    'double'  : 'double',
    'char'    : 'char',
    'void'    : 'void',
  ]

  static def packageMap = [ 
    'BigDecimal' : 'java.math', 
    'BigInteger' : 'java.math', 
    'List'       : 'java.util',
    'Map'        : 'java.util',

    'Range'      : 'groovy.lang',
  ]

  static def objectTypeMap = [ 
    'Object' : 'NSObject', 
    'String' : 'NSString', 

    'BigDecimal' : 'NSDecimalNumber',
    'BigInteger' : 'NSNumber',
    
    'Double'    : 'NSNumber',
    'Integer'   : 'NSNumber',

    'List'      : 'NSMutableArray',
    'Map'       : 'NSMutableDictionary',

    'Range'      : 'NSArray',

    'Exception' : 'NSException', 
    'NumberFormatException' : 'NSException', 
  ]

  static def exceptions = [
    'NSException'
  ] as Set  

  static def NSDecimalNumberCreators = [
    [ [ Number_TYPE ], "decimalNumberWithDecimal:{0}" ],
    [ [ STRING_TYPE ], "decimalNumberWithString:{0}" ],
    [ [ Number_TYPE, Number_TYPE, Boolean_TYPE ], 
      "decimalNumberWithMantissa:{0} exponent:{1} isNegative:{2}" ],
  ]

  static def RuntimeExceptionCreators = [
    [ [STRING_TYPE], "exceptionWithName:nil reason:{1} userInfo:nil"]
  ]

  static def creatorMap = [
    'NSDecimalNumber' : NSDecimalNumberCreators,
    //'RuntimeException' : RuntimeExceptionCreators,
  ]

  static def specialNameMap = [
    'id'   : 'id_',
    'this' : 'self',
  ]

}

class ObjectiveCVisitor implements GroovyClassVisitor { 

  ObjectiveCVisitor(ClassProcessor cp) { 
    owner = cp
  }
  
  ClassProcessor owner

  void visitClass(ClassNode node) { }
           
  void visitConstructor(ConstructorNode node) { }
           
  void visitField(FieldNode node) { }
           
  void visitMethod(MethodNode node) {
    info "[ObjectiveCVisitor] visitMethod: ${node.name}"
    
    //node?.code?.visit(typeVisitor)
  }
           
  void visitProperty(PropertyNode node) { }

}
