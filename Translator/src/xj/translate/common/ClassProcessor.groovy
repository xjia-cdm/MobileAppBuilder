package xj.translate.common

import java.util.ArrayList;

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
import java.lang.reflect.Modifier

import static org.apache.commons.lang3.StringUtils.*

import xj.translate.*
import xj.translate.typeinf.TypeInference
import xj.translate.typeinf.TypeUtil

import static xj.translate.Logger.* 
import static xj.translate.common.Unparser.*
import static xj.translate.typeinf.MethodHelper.*
import static xj.translate.typeinf.TypeUtil.*
import static xj.translate.typeinf.TypeInference.*

import xj.translate.java.JavaClassProcessor
import xj.translate.objc.ObjectiveCClassProcessor

class ClassProcessor { 

  static ClassProcessor classProcessorForLanguage(Language lang,
						  Templates templates,
						  Unparser unparser,
						  ModuleNode moduleNode = null,
						  ClassNode classNode = null,
						  String infile = null) { 
    if (lang == Language.Java) { 
      return new JavaClassProcessor(moduleNode, classNode, infile, templates, unparser)
    } else if (lang == Language.ObjectiveC) { 
      return new ObjectiveCClassProcessor(moduleNode, classNode, infile, templates, unparser)
    }
    return new ClassProcessor(moduleNode, classNode, infile, templates, unparser, 
			      lang == Language.Groovy,                              // filtering for raw
			      false)
  }

  static class FieldInfo { 
    FieldNode field
    // additional field info 
  }

  static class MethodInfo { 
    MethodNode method

    boolean overloaded = false

    // additional method info 
    boolean isOriginalMethod = true //vs a copied method
    MethodNode originalMethodNode = null //For copies only, a reference back to its original method
    List<MethodNode> myCopies = [] //For original methods only, a list of its copies
  }
  
  ModuleNode moduleNode
  ClassNode classNode
  String infile
  Unparser unparser
  Templates templates

  Map<String, FieldInfo> fieldMap = [:];
  Map<String, List<MethodInfo>> methodMap = [:];

  Map<String, FieldInfo> staticFieldMap = [:];
  Map<String, List<MethodInfo>> staticMethodMap = [:];
  List<MethodInfo> constructors = [];

  List importFiles = []

  Set<ClassNode> hasTypeSet = [] as Set;
  Set<ClassNode> useTypeSet = [] as Set;      // types used in method signature
  Set<ClassNode> useTypeSetLocal = [] as Set; // types used in method body 
  Set<ClassNode> superTypeSet = [] as Set;

  Set<ClassNode> hasStaticTypeSet = [] as Set;
  Set<ClassNode> useStaticTypeSet = [] as Set;

  ImportResolver resolver;

  def engine = new GStringTemplateEngine()

  boolean isScript = false
  String name;
  String nameWithPackage;

  String enumMinValue = null
  String enumMaxValue = null
	
  boolean filterMethods = true 
  boolean filterFields = true 
  boolean addReturn = true

  ClassProcessor(ModuleNode moduleNode,
		 ClassNode classNode,
		 String infile,
		 Templates templates, 
		 Unparser unparser,
		 boolean filtering = true,
		 boolean addReturn= true) { 
    this.moduleNode = moduleNode
    this.classNode = classNode
    this.infile = infile
    this.templates = templates
    this.unparser = unparser
    filterMethods = filtering 
    filterFields = filtering
    this.addReturn = addReturn

    //unparser.owner = this
    
    resolver = new  ImportResolver(moduleNode, classNode)

    if (classNode) init()      
  }

  def init() { 
    info "[ClassProcessor] init()"

    if (classNode) { 
      
      name = classNode.nameWithoutPackage 
      nameWithPackage = classNode.name
      //boolean isInterface = classNode.isInterface()
      //boolean isAbstract = false
      //boolean isEnum = classNode.isEnum()

      if (classNode.outerClass) { 
	def outerName = classNode.outerClass.nameWithoutPackage
	if (name.startsWith(outerName + '$')) { 
	  name = name.substring(outerName.length() + 1)
	}
      }

      if (classNode.superClass?.name == 'groovy.lang.Script') { 
	isScript = true
      }

      info "[ClassProcessor] Process class: ${name}  package: ${classNode.packageName}" 

      if (classNode.superClass) { 
	superTypeSet.add(classNode.superClass)
      }
      if (classNode.allInterfaces) { 
	classNode.allInterfaces.each { inf -> superTypeSet.add(inf) }
      }

      classNode.fields.each { f -> 
	info "[ClassProcessor]  Field: ${f.name} : ${f.type} : static=${f.static} : enum=${f.enum} @ ${f.lineNumber} " +
	     "${f.initialExpression?.text} ${f.initialValueExpression?.text}" + 
	     "  primitive=${ClassHelper.isPrimitiveType(f.type)}"

	if (!isPrimitiveType(f.type.name)) { 
	  String typeName = resolver.resolveClassName(f.type.name)
	  info "[ClassProcessor]  Resolve: ${f.type.name} ==> ${typeName}"
	}

	if (classNode.interface) { 	
	  f.setModifiers(f.modifiers | Modifier.STATIC | Modifier.FINAL)
	  info "[ClassProcessor]  constant ${f.name} static=${f.static} final=${f.final}"
	}

      }

      classNode.fields.each { f -> 
	info "[ClassProcessor]  Field: ${f.name} : ${f.type} : ${f.static} " 

	String newName = normalizeVariableName(f.name)
	if (newName != f.name) { 
	  info "[ClassProcessor]  rename field ${f.name} --> ${newName}"
	  f.rename(newName)
	}

	//!f.synthetic
	if (!isFilteredField(f)) { 
	  if (f.static) { 
	    staticFieldMap[f.name] = new FieldInfo(field : f)
	    if (classNode.isEnum()) { 
	      if (enumMinValue == null) enumMinValue = f.name
	      enumMaxValue = f.name
	    }
	    if (!classNode.isEnum() || !f.isEnum())
	      hasStaticTypeSet.add(f.type)
	  } else {  
	    fieldMap[f.name] = new FieldInfo(field : f)
	    hasTypeSet.add(f.type)
	  }

	  transformField(f, this, addReturn)
	}
      }

      if (!isScript && !classNode.isEnum()) { 
	classNode.declaredConstructors.each { c -> 
	  // ignore generated constructors, except for inner classes 
	  if (c.lineNumber > 0 || isInnerClass(classNode)) { 
	    info "[ClassProcessor]  Constructor: ${c.name} " 	
	    constructors << new MethodInfo(method : c)
	    Parameter[] params = c.getParameters()  
	    if (params) 
	      for (p in params) useTypeSet.add(p.type)	  	
	  }
	}
      }

      classNode.methods.each { m -> 
	//info "  Method: ${m.name} : static=${m.static} " 	
	//!m.synthetic
	if (!isFilteredMethod(m)) { 
	  if (m.static) { 
	    def mlist = staticMethodMap[m.name]
	    if (mlist) { 
	      mlist << new MethodInfo(method : m)
	    } else { 
	      mlist = [ new MethodInfo(method : m) ]
	    }
	    staticMethodMap[m.name] = mlist 
	    if (m.name == 'main' && m.parameters?.length == 1) { 
	      // normalize signature of main
	      m.returnType = ClassHelper.VOID_TYPE
	      m.parameters[0].type = ClassHelper.STRING_TYPE.makeArray()
	    }
	  } else {  
	    def mlist = methodMap[m.name]
	    if (mlist) { 
	      mlist << new MethodInfo(method : m)
	    } else { 
	      mlist = [ new MethodInfo(method : m) ]
	    }
	    methodMap[m.name] = mlist
	  }
	  
	  transformMathod(m, this, addReturn)

	  useTypeSet.add(m.returnType)
	  Parameter[] params = m.getParameters()  
	  if (params) 
	    for (p in params) useTypeSet.add(p.type)	  
	}
      }	

      if (hasMain()) { 
	ModuleProcessor.mainClass = classNode.name
      }

      classNode.visitContents(new SanityChecker(this))

      info "[ClassProcessor] hasMain: ${hasMain()}"
      info "[ClassProcessor] useTypeSet: ${useTypeSet}"
      info "[ClassProcessor] hasTypeSet: ${hasTypeSet}"
      info "[ClassProcessor] useStaticTypeSet: ${useStaticTypeSet}"
      info "[ClassProcessor] hasStaticTypeSet: ${hasStaticTypeSet}"

      // cause StackOverflowError
      // 	at org.codehaus.groovy.ast.ClassNode.toString(ClassNode.java:1122)
      //        at org.codehaus.groovy.ast.ClassNode.toString(ClassNode.java:1127)
      //        at org.codehaus.groovy.ast.GenericsType.toString(GenericsType.java:60)
      //info "superTypeSet: ${superTypeSet}"  
    }
  }

  void inferTypes(TypeInference.DynamicTypesHelper typeHelper) {
    //TODO check into FieldCode generation
    //TODO look into closures
    //Infer static types first

    staticMethodMap.each { mname, mlist -> 
      if (!isScript || mname != 'main') { 
	mlist?.each { MethodInfo minfo -> 
	  inferMethodTypes(minfo.method, nameWithPackage, typeHelper)
	}
      }
    }

    //First try to get as many types as we can in the methods
    methodMap.each { mname, mlist -> 
      mlist?.each { MethodInfo minfo -> 
	inferMethodTypes(minfo.method, nameWithPackage, typeHelper)
      }
    }

    constructors.each { minfo ->
      boolean overloaded = constructors.size() > 1
      minfo.overloaded = overloaded
    }

    methodMap.each { mname, mlist -> 
      int msize = mlist.findAll{ m -> 
	!m.isOriginalMethod || m.myCopies.isEmpty() }.size()
      boolean overloaded = msize > 1
      mlist.each { MethodInfo minfo -> 
	minfo.overloaded = overloaded
      }
    }
    staticMethodMap.each { mname, mlist -> 
      if (!isScript || mname != 'main') { 
	int msize = mlist.findAll{ m -> 
	  !m.isOriginalMethod || m.myCopies.isEmpty() }.size()
	boolean overloaded = msize > 1
	mlist.each { MethodInfo minfo -> 
	  minfo.overloaded = overloaded 
	}
      }
    }

  }

  String defaultClassVisibility() { 
    ''
  }

  String fieldVisibility(FieldNode f) { 
    if (f) { 
      int mod = f.modifiers
      if (java.lang.reflect.Modifier.isPrivate(mod)) return 'private'
      if (java.lang.reflect.Modifier.isProtected(mod)) return 'protected'	 
    }
    return ''
  }

  boolean getterNeeded(FieldNode f) { 
    false
  }

  boolean setterNeeded(FieldNode f) { 
    false
  }

  String getterName(String fname) { 
    "get${capitalize(fname)}"
  }

  String setterName(String fname) { 
    "set${capitalize(fname)}"
  }

  String getPackageDeclaration() { 
    if (classNode.packageName) { 
      def binding = [ 'name' : classNode.packageName ]
      def template = engine.createTemplate(templates.packageDeclaration).make(binding)
      return template.toString()
    } else { 
      return ''
    }
  }

  void addImportFile(f) { 
    if (f && !importFiles.contains(f)) { 
      importFiles << f 
    }
    //println "addImportFile ${f} -> ${importFiles}"
  }

  def getImportList() { 
    importFiles
  }

  String getExtendsString() { 
    if (classNode.superClass && !isScript) {	
      if (classNode.isEnum() && classNode.superClass.name != 'java.lang.Enum' ||
	  !classNode.isEnum() && classNode.superClass.name != 'java.lang.Object') { 
	return 'extends ' + normalizeTypeName(classNode.superClass) + ' '      
      }
    }
    return ''
  }

  String getImplementsString() { 
    if (!classNode.isInterface() && 
	classNode.allInterfaces) { 
      return 'implements ' + classNode.allInterfaces.collect { inf -> normalizeTypeName(inf) }.join(', ') + ' '
    }
    return ''
  }

  // parts of a class 

  def enumValueScrap       = ''
  def fieldDeclScrap       = ''
  def staticFieldDeclScrap = ''
  def constructorDefScrap  = ''
  def methodDefScrap       = ''
  def staticMethodDefScrap = ''
  def innerClassDefScrap   = ''

  void buildScraps() { 
    if (classNode) { 
      staticFieldMap.each { String fname, FieldInfo finfo -> 
	if (finfo) { 
	  FieldNode f = finfo.field 
	  if (classNode.isEnum() && f.isEnum()) { 
	    handleEnumValue(finfo)
	  } else { 
	    handleStaticField(finfo)
	  }
	}
      }
      if (enumValueScrap) { 
	enumValueScrap += '\n\n'
      }

      fieldMap.each { String fname,FieldInfo finfo -> 
	if (finfo) { 
	  handleField(finfo)
	}
      }
      
      /*
      // ignore generated constructors 
      if (!isScript && !classNode.isEnum()) { 
	classNode.declaredConstructors.each { c -> 
	  if (c.lineNumber > 0)
	    handleConstructor(c)
	}
      }
      */


      constructors.each { minfo ->
	def c = minfo.method
	handleConstructor(c, minfo.overloaded)
      }

      methodMap.each { mname, mlist -> 
	mlist.each { MethodInfo minfo -> 
	  if (!minfo.isOriginalMethod ||
	      minfo.myCopies.isEmpty()) { 
	    handleMethod(minfo, minfo.overloaded)
	  }
	}
      }
      staticMethodMap.each { mname, mlist -> 
	if (!isScript || mname != 'main') { 
	  mlist.each { MethodInfo minfo -> 
	    if (!minfo.isOriginalMethod ||
		minfo.myCopies.isEmpty()) {
	      handleStaticMethod(minfo, minfo.overloaded)
	    }
	  }
	}
      }

      if (isScript) { 
	handleScript()
      }

      handleInnerClasses()
    }
  }

  boolean hasInnerClass(ClassNode c) { 
    classNode.innerClasses?.any { ic -> ic.name == c.name }  
  }

  def handleInnerClasses() { 
    classNode.innerClasses?.each { ic -> 
      def cp = ModuleProcessor.getClassProcessor(ic.nameWithoutPackage)
      ModuleProcessor.currentClassProcessor = cp
      innerClassDefScrap += cp.generateCode()	
      ModuleProcessor.currentClassProcessor = this
    }
  }

  String generateCode() { 
    if (classNode) { 
      def visibility = defaultClassVisibility()
      if (visibility.length() > 0) visibility += ' '
      def mod = ''
      def kind = classNode.isEnum() ? 'enum' : 
                   (classNode.isInterface() ? 'interface' : 'class')
      if (classNode.outerClass) { 
	if (java.lang.reflect.Modifier.isStatic(classNode.modifiers)) { 
	  mod = 'static '
	}
      }

      buildScraps()

      def fields = indent(enumValueScrap + fieldDeclScrap + staticFieldDeclScrap, 1)
      def methods = indent(constructorDefScrap + methodDefScrap + staticMethodDefScrap, 1)
      def binding = [ 'modifiers'     : mod, 
		      'visibility'    : visibility,
		      'kind'          : kind,
		      'name'          : name,
		      'extend'        : extendsString,
		      'implement'     : implementsString,
		      'fieldDecl'     : fields, 
		      'methodDef'     : methods, 
		      'innerClassDef' : indent(innerClassDefScrap, 1)
		    ]
      def template = engine.createTemplate(templates.classDef).make(binding)
      return template.toString()

      //return (isInnerClass(classNode) ? '' : headerTemplate) + classTemplate
    }
    return null
  }

  def generateFileHeader(String outfile, String message = null) { 
    def binding = [ 'outfile' : outfile,
		    'infile'  : infile,
		    'message' : message ? ('\n *  ' + message + '\n *') : '',
		    'options' : Config.instance.options ?: '(none)', 
		    'config'  : "target=${Config.instance.target} phase=${Config.instance.phaseName}(${Config.instance.phase})"
		  ]
    def template = engine.createTemplate(templates.fileHeader).make(binding)
    return template.toString()
  }

  def generatePreamble(String outfile, 
		       String message = null) { 
    return generatePreamble(outfile, message, 
			    importList, null)
  }

  def generatePreamble(String outfile, 
		       String message, 
		       List impfiles,
		       List forwardDecls) { 
    //println 'generatePreamble impfiles=' + impfiles
    Config config = Config.instance
    def header = config.header ? generateFileHeader(outfile, message) : ''
    def imports = generateImports(impfiles)
    def forwardDecl = generateForwardDecl(forwardDecls)
    def binding = [ 'header'      : header,
		    'pkg'         : packageDeclaration,
		    'imports'     : imports,
		    'forwardDecl' : forwardDecl
		  ]
    def template = engine.createTemplate(templates.preamble).make(binding)
    return template.toString()
  }

  String generateImports(List impfiles = null) { 
    if (impfiles && !impfiles.isEmpty()) { 
      return impfiles.sort().collect { f -> "import ${f};" }.join('\n')
    } 
    ''
  }

  String generateForwardDecl(List forwardDecls = null) { '' }

  void handleScript() { }

  void handleEnumValue(FieldInfo finfo) { 
    if (finfo) { 
      def fname = finfo.field.name
      if (enumValueScrap) { 
	enumValueScrap += ", ${fname}"
      } else { 
	enumValueScrap = "  ${fname}"
      }
    }
  }

  void handleStaticField(FieldInfo finfo) { 
    if (finfo) { 
      staticFieldDeclScrap += generateFieldCode(finfo)

      FieldNode f = finfo.field 
      def fname = f.name
      def ftype = typeName(f.type)

      if (getterNeeded(f)) { 
	def binding = [ 'type'       : ftype, 
			'name'       : fname,
			'mod'        : 'static ',
			'self'       : name,
			'methodName' : getterName(fname)
		      ]
	def template = engine.createTemplate(templates.getterDef).make(binding)
	def getterTemplate = template.toString()
	staticMethodDefScrap += getterTemplate
      }
      
      if (setterNeeded(f)) { 
	def binding = [ 'type'       : ftype, 
			'name'       : fname,
			'mod'        : 'static ',
			'self'       : name,
			'methodName' : setterName(fname)
		      ]
	def template = engine.createTemplate(templates.setterDef).make(binding)
	def setterTemplate = template.toString()
	staticMethodDefScrap += setterTemplate
      }
    }
  }

  void handleField(FieldInfo finfo) { 
    if (finfo) { 
      fieldDeclScrap += generateFieldCode(finfo)

      FieldNode f = finfo.field 
      def fname = f.name
      def ftype = typeName(f.type)

      if (getterNeeded(f)) { 
	def binding = [ 'type'       : ftype, 
			'name'       : fname,
			'mod'        : '',
			'self'       : 'this',
			'methodName' : getterName(fname)
		      ]
	def template = engine.createTemplate(templates.getterDef).make(binding)
	def getterTemplate = template.toString()
	methodDefScrap += getterTemplate
      }
      
      if (setterNeeded(f)) { 
	def binding = [ 'type'       : ftype, 
			'name'       : fname,
			'mod'        : '',
			'self'       : 'this',
			'methodName' : setterName(fname)
		      ]
	def template = engine.createTemplate(templates.setterDef).make(binding)
	def setterTemplate = template.toString()
	methodDefScrap += setterTemplate
      }
    }
  }

  def generateFieldCode(FieldInfo finfo) { 
    if (finfo) { 
      FieldNode f = finfo.field 
      def fmod = ''
      if (f.static) { 
	fmod += 'static '
      }
      if (f.final) { 
	fmod += 'final '
      }
      def fvis = fieldVisibility(f)
      if (fvis.length() > 0) fvis += ' '
      def fname = f.name
      def ftype = typeName(f.type)
      def finit = ''
      if (f.initialExpression) {
	def exp = unparser.unparse(f.initialExpression) 
	finit = " = ${exp}"
      } 
	
      def binding = [ 'type'       : ftype, 
		      'name'       : fname,
		      'modifiers'  : fmod,
		      'visibility' : fvis,
		      'init'       : finit
		    ]
      def template = engine.createTemplate(templates.fieldDeclaration).make(binding)
      return template.toString()
    }
    ''
  }

  void handleStaticMethod(MethodInfo minfo, boolean overloaded) { 
    if (minfo) { 
      staticMethodDefScrap += generateMethodCode(minfo, overloaded)      
    }
  }

  void handleMethod(MethodInfo minfo, boolean overloaded) { 
    if (minfo) { 
      methodDefScrap += generateMethodCode(minfo, overloaded)      
    }
  }

  void handleConstructor(ConstructorNode c, boolean overloaded) { 
    if (c && !isFilteredConstructor(c)) { 
      constructorDefScrap += generateMethodDef(c, overloaded)
    }
  }

  def generateMethodSignature(MethodNode m, boolean overloaded) { 
    if (m) { 
      def mst = (m.static ? 'static ' : '')
      def mvis = (m.public ? 'public ' : (m.protected ? 'protected ' : (m.private ? 'private ' : '')))
      def mtype = m instanceof ConstructorNode ? '' : (typeName(m.returnType) + ' ')
      def mname = m instanceof ConstructorNode ? name : m.name
      mname = normalizeMethodName(mname)
      def binding = [ 'modifiers'  : mst,
		      'visibility' : mvis, 
		      'returnType' : mtype,
		      'name'       : mname,
		      'params'     : unparser.parameters(m.parameters)
		    ]
      def template = engine.createTemplate(templates.methodSignature).make(binding)
      return template.toString()
    }
    return ''
  }

  def generateMethodDecl(MethodNode m, boolean overloaded) { 
    if (m) { 
      def methodSig = generateMethodSignature(m, overloaded)
      def mod = m.abstract && !classNode.interface ? 'abstract ' : ''
      def binding = [ 'mod'       : mod, 
		      'signature' : methodSig
		    ]
      def template = engine.createTemplate(templates.methodDeclaration).make(binding)
      return template.toString()
    }
    return ''
  }

  def generateMethodDef(MethodNode m, boolean overloaded) { 
    if (m) { 
      def methodSig = generateMethodSignature(m, overloaded)
      def binding = [ 'signature' : methodSig,
		      'body'      : unparser.unparse(m.code, 1) ?: ''
		    ]
      def template = engine.createTemplate(templates.methodDefinition).make(binding)
      return template.toString()
    }
    return ''
  }

  def generateMethodCode(MethodInfo minfo, boolean overloaded) { 
    if (minfo) { 
      MethodNode m = minfo.method
      if (classNode.interface || m.abstract) { 
	return generateMethodDecl(m, overloaded)
      } else { 
	return generateMethodDef(m, overloaded)
      } 
    }
    return ''
  }

  def generateHeaderCode() { '' }

  void generateBuildFile(appname, mainclass) {   
    File buildfile = Config.instance.getBuildFile()
    def files = ModuleProcessor.outputFiles.findAll { f -> f.endsWith('.groovy') }.join(',')
    def binding = [ 'appname'   : appname,
		    'mainclass' : mainclass,
		    'filelist'  : files
		  ] 
    def template = engine.createTemplate(templates.antFileGroovy).make(binding)
    def code = template.toString()

    println "Write build file to ${buildfile}"
    println '=========================================================='
    println code
    println '==========================================================\n'

    buildfile.write(code)

  }

  String normalizeMethodName(String name) { 
    /*
    // for groovy generated names, such as '<clinit>' 
    if (name) { 
      name = name.replaceAll('<', '__')
      name = name.replaceAll('>', '__')
    }
    */
    return name
  }

  String normalizeVariableName(String name) { 
    info "normalizeVariableName ${name}"
    return name
  }

  boolean hasMain() { 
    return staticMethodMap['main'] != null
  }

  String typeName(ClassNode c) { 
    String tname = ''
    if (c) {
      tname = typeName(c.name, c.enum) 
      GenericsType[] gt = c.genericsTypes
      if (gt && !TypeUtil.allObjectTypes(gt)) {  
	int k = tname.indexOf('<')
	if (k > 0) { 
	  tname = tname.substring(0, k) 
	}
	tname = tname + '<' + gt.collect { t -> genericsTypeName(t) }.join(',') + '>'
      }
    }
    return tname 
  }

  String genericsTypeName(GenericsType gt) { 
    String tname = ''
    if (gt) { 
      tname = typeName(gt.name, false)
      ClassNode[] upper = gt.getUpperBounds() 
      if (upper && upper.length > 0) { 
	tname += (' extends ' + upper.collect{ t -> typeName(t) }.join(', '))
      }
    }
    return tname
  }

  String typeName(String tname) { 
    return typeName(tname, ModuleProcessor.isEnum(tname))
  }

  String typeName(String tname, boolean isEnum) { 
    return normalizeTypeName(tname, isEnum)
  }

  String normalizeTypeName(ClassNode c) { 
    String tname = ''
    if (c) { 
      tname = normalizeTypeName(c.name, c.enum)
      GenericsType[] gt = c.genericsTypes
      if (gt && !TypeUtil.allObjectTypes(gt)) { 
	int k = tname.indexOf('<')
	if (k > 0) { 
	  tname = tname.substring(0, k) 
	}
	tname = tname + '<' + gt.collect { t -> genericsTypeName(t) }.join(',') + '>'
      }
    }
    return tname
  }

  String normalizeTypeName(String tname) { 
    return normalizeTypeName(tname, ModuleProcessor.isEnum(tname))
  }

  String normalizeTypeName(String tname, boolean isEnum) { 
    if (tname) { 
      String suffix = ''    
      while (tname[0] == '[') { 
	tname = tname.substring(1)
	suffix += '[]'
      }
      if (tname[tname.length() - 1] == ';' && tname[0] == 'L') { 
	tname = tname.substring(1, tname.length() - 1)
      }

      if (tname.startsWith('java.lang.')) { 
	tname = tname.substring(10)
      } 

      tname = mapTypeName(tname)

      def pkgname = classNode.packageName
      if (pkgname && tname.startsWith(pkgname)) 
	tname = tname.substring(pkgname.length() + 1)

      def outerName = classNode.nameWithoutPackage
      if (tname.startsWith(outerName + '$')) { 
	return tname.substring(outerName.length() + 1)
      }

      if (isEnum) { 
	tname = tname.replaceAll('\\$', '.')
      }

      return tname + suffix 
    } 
    return ''
  }

  String mapTypeName(String tname) { 
    return tname
  }

  boolean isFilteredConstructor(ConstructorNode c) { 
    if (filterMethods) { 
      return c.lineNumber <= 0
    }
    return false
  }

  boolean isFilteredMethod(MethodNode m) { 
    if (filterMethods) { 
      if (classNode.isEnum() && 
	  Config.instance.enumMode == Config.EnumMode.Default) { 
	if (ENUM_FILTERED_METHODS.contains(m.name)) return true      
      }
      
      if (FILTERED_METHODS.contains(m.name)) return true
      for (p in FILTERED_METHOD_PREFIXES) {  
	if (m.name.startsWith(p)) return true
      }     
    }
    return false 
  }

  boolean isFilteredField(FieldNode f) { 
    if (filterFields) { 
      if (classNode.isEnum() &&
	  Config.instance.enumMode == Config.EnumMode.Default) { 
	if (ENUM_FILTERED_FIELDS.contains(f.name)) return true
	for (p in ENUM_FILTERED_FIELD_PREFIXES) {  
	  if (f.name.startsWith(p)) return true
	}     
      }
      
      for (p in FILTERED_FIELD_PREFIXES) {  
	if (f.name.startsWith(p)) return true
      }     
    }
    return false 
  }
  

  //
  // utility methods 
  //

  static boolean isPrimitiveType(String type) { 
    if (type) { 
      return GROOVY_PRIMITIVE_TYPES.contains(type)
    }
    return false 
  }

  static boolean isInnerClass(ClassNode c) { 
    return c?.getOuterClass() != null
  }

  static Set<String> GROOVY_PRIMITIVE_TYPES = [ 
    'boolean', 'byte', 'char', 'double', 'float', 'int', 'long', 'short'
  ] as Set

  static Set<String> FILTERED_METHODS = [
    'methodMissing', 'propertyMissing'
  ] as Set

  static Set<String> FILTERED_METHOD_PREFIXES = [
    'this$dist$', 
  ] as Set

  static Set<String> FILTERED_FIELD_PREFIXES = [
    'this$', '$const$', 
  ] as Set

  static Set<String> ENUM_FILTERED_METHODS = [
    'next', 'previous', 'getAt', 'values', 'valueOf', '$INIT', '<clinit>'
  ] as Set

  static Set<String> ENUM_FILTERED_FIELDS = [
    'MIN_VALUE', 'MAX_VALUE'
  ] as Set

  static Set<String> ENUM_FILTERED_FIELD_PREFIXES = [
    '$VALUES', 
  ] as Set

} 

class SanityChecker implements GroovyClassVisitor { 

  SanityChecker(ClassProcessor cp) { 
    owner = cp
    typeVisitor = new TypeVisitor(cp)
  }
  
  ClassProcessor owner
  TypeVisitor typeVisitor

  void visitClass(ClassNode node) { }
           
  void visitConstructor(ConstructorNode node) { }
           
  void visitField(FieldNode node) { }
           
  void visitMethod(MethodNode node) {
    //info "visitMethod: ${node.name}"    
    node?.code?.visit(typeVisitor)
  }
           
  void visitProperty(PropertyNode node) { }

}

class TypeVisitor extends CodeVisitorSupport { 

  TypeVisitor(ClassProcessor cp) { 
    owner = cp
  }
  
  ClassProcessor owner
  
  void visitConstantExpression(ConstantExpression expression) { 
    //info "visitConstantExpression: ${expression}"
    if (ClassHelper.isNumberType(expression.type) && 
	!ClassHelper.isPrimitiveType(expression.type)) { 
      // convert const number to unwrapped type 
      expression.type = ClassHelper.getUnwrapper(expression.type)
    }
  }
  
  /*
  void visitDeclarationExpression(DeclarationExpression expression) { 
    //println "visitDeclarationExpression: ${expression} type = ${expression.variableExpression.type}"
    super.visitDeclarationExpression(expression) 
    owner.useTypeSet.add(expression.variableExpression.type)
    //println "owner.useTypeSet: ${owner.useTypeSet}"
  }
  */

  void visitPropertyExpression(PropertyExpression expression) { 
    super.visitPropertyExpression(expression)
    if (expression.objectExpression instanceof ClassExpression) { 
      owner.useTypeSetLocal.add(expression.objectExpression.type)      
    }
  }

}
