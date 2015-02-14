
package xj.mobile.tool

import org.htmlcleaner.*
import org.ho.yaml.Yaml
import sun.launcher.resources.launcher_es

/*
 * Run:
 *   groovy devbin/androiddocreader.sh lib
 *      fectch and analyze top level framework doc
 *      output: output/api/android/AllPackages.xml
 *              output/api/android/AllPackageList.yml
 *              output/api/android/${name}_package_summary.xml
 *   groovy devbin/androiddocreader.sh fetch
 *      analyze framework file, fetch and clean class API page 
 *      output: lib/api/android/${pkgname}_PackageRefs.yml
 *              output/api/android/${pkgname}/${name}.xml
 *   groovy devbin/androiddocreader.sh analyze
 *      analyze class API doc
 *      output: lib/api/android/${pkgname}/${name}_Def.yml
 *
 * Files:
 *   Intermediate files in output/api/android
 *      cleaned xml files based on html doc fetched from web
 *   Result files in lib/api/android
 *      result of analysis of API doc 
 *
 */
class AndroidDocReader2 {

  ////////////// utilities
  static final String XPATH_STATS = "nbsp";
  static readPage(address) {

	def cleaner = new HtmlCleaner()
	def node = cleaner.clean(address)

	// Convert from HTML to XML
	def props = cleaner.getProperties()
	def serializer = new SimpleXmlSerializer(props)

	def xml = serializer.getXmlAsString(node)

	// Parse the XML into a document we can work with
	return new XmlSlurper(false,false).parseText(xml)

  }

  static base = 'output/api/android'
  static output_base = 'lib/api/android'

  static writeXml(page, fname) {
	def d1= new File(base + '/' + fname).parentFile
	d1.mkdirs()
	def fw = new FileWriter(base + '/' + fname)

	groovy.xml.XmlUtil.serialize(page, fw)
	fw.close()
  }

  static readXml(fname) {
	new XmlSlurper(false,false).parse(new File(base + '/' + fname))
  }


  static String PROG_NAME = 'API Doc Reader [Android] v0.3'

  static api_base = 'http://developer.android.com'

  static main(args) {
	println PROG_NAME

	def outd = new File(output_base)
	outd.mkdirs()

	boolean lib = false
	boolean fetch = false
	boolean analyze = false
	boolean testing = false

	for (a in args) {
	  if (a == '-T') { 
		base = 'output2/api/android'
		output_base = 'output2/lib/api/android'
	  }
	  if (a.contains('test')) testing = true
	  if (a.contains('lib')) lib = true
	  if (a.contains('fetch')) fetch = true
	  if (a.contains('analyze')) analyze = true
	}

	if (testing) {
	  test()
	} else {
	  if (lib) {
		fetchLibs()
	  }
	  if (fetch) {
		fetchAPI()
	  }
	  if (analyze) {
		analyzeAPI()
	  }
	}

  }



  static fetchLibs() {
	println "Android Doc Reader Lib method running"

	def page = readPage((api_base + '/reference/packages.html').toURL())
	assert page.head.title == 'Package Index | Android Developers'
	println "Write AllPackages.xml"
	writeXml(page, 'AllPackages.xml')

	def allPkgs = [:]

	page.'**'.findAll { it.@class == 'jd-linkcol' }.each {
	  String title = it.a.text()
	  String href = it.a.@href
	  //println title
	  println ' href : \t' + href

	  allPkgs[title] = href
	  fetchPackageRef(href, title)
	}

	println "Write AllPackageList.yml"
	Yaml.dump(allPkgs, new File(base + '/AllPackageList.yml'), true)
  }

  static fetchPackageRef(url, name) {
	def address = api_base + url
	def page = readPage(address.toURL())
	println '  Fetch: ' + page.head.title



	writeXml(page, "${name}_package_summary.xml")
	if (page.head.title != "${name} | Android Developers") {
	  println "  !!! Page title mismatch: ${page.head.title}"
	}
  }

  static fetchAPI() {
	println '=== fetch package API'

	new File(output_base).mkdirs()

	def allpkgs = Yaml.load(new File(base + '/AllPackageList.yml').text)
	allpkgs.keySet().each { pkg -> 
	  if (pkg.startsWith('android.'))
		analyzePackageRef(pkg, true)
	}

	/*
	analyzePackageRef('android.view', true) //false)
	analyzePackageRef('android.widget', true) //false)
	analyzePackageRef('android.app', true) //false)
	analyzePackageRef('android.webkit', true) //false)
	analyzePackageRef('android.content', true) //false)
	analyzePackageRef('android.animation', true) //false)
	*/
  }

  static analyzePackageRef(pkgname, fetch = true) {
	println "=== Analyze Package ${pkgname}"
	def page = readXml("${pkgname}_package_summary.xml")

	def refs = [:]

	def node1 = page.'**'.find { it.@id == 'jd-content' }
	String kind = ''
	node1.children().each { sec ->
	  if (sec.name() == 'h2') {
		//println sec.name() + '  ' +  sec.text()
		kind = sec.text()
		refs[kind] = []
	  } else if (sec.name() == 'div' && sec.@class == 'jd-sumtable') {
		sec.'**'.findAll { it.@class == 'jd-linkcol' }.each {
		  def node2 = it.a[0]
		  //if (node2 instanceof List) node2 = node2[0]
		  String name = node2.text()
		  String href = node2.@href


		  refs[kind] << [ name: name, href: href ]
		}
	  }
	}


	Yaml.dump(refs, new File(output_base + "/${pkgname}_PackageRefs.yml"), true)


	if (fetch) {
	  refs.each { k, sec ->
		sec.each {
		  String href = it.href
		  //println "  fetch: ${it.name} ${href}"
		  fetchClassRef(it.name, pkgname, href)
		}
	  }
	}

  }

  static fetchClassRef(name, pkgname, href) {
	def address = api_base + href
	def page = readPage(address.toURL())
	assert page.head.title == "${name} | Android Developers"
	writeXml(page, "${pkgname}/${name}.xml")
  }

  static analyzeAPI() {
	def allpkgs = Yaml.load(new File(base + '/AllPackageList.yml').text)
	allpkgs.keySet().each { pkg -> 
	  if (pkg.startsWith('android.'))
		analyzeAPI(pkg)
	}

	/*
	analyzeAPI('android.view')
	analyzeAPI('android.widget')
	analyzeAPI('android.app')
	analyzeAPI('android.webkit')
	analyzeAPI('android.content')
	analyzeAPI('android.animation')
	*/
  }

  static test() {
	analyzeClassRef('android.widget', 'SeekBar')
  }

  static analyzeAPI(pkgname) {
	println "=== Analyze Package ${pkgname}"

	def refs = Yaml.load(new File(output_base + "/${pkgname}_PackageRefs.yml").text)
	refs.Classes.each {
	  analyzeClassRef(pkgname, it.name)
	}
	/*
	  analyzeClassRef('android.widget', 'Button')
	  analyzeClassRef('android.widget', 'TextView')

	  analyzeClassRef('android.view', 'View')
	*/
  }

  static methodPattern1 = ~/(\w+)\s*\(((?:[\w\.]+)(?:,(?:[\w\.]+))*)\)/

  static filteredAttributeNames = xj.mobile.lang.AttributeMap.filteredAttributes.Android

  static analyzeClassRef(pkgname, name) {
	println "=== Analyze class ${pkgname} ${name}"
	def filename = pkgname ? "${pkgname}/${name}.xml" : "${name}.xml"
	def page = readXml(filename)

	assert page.head.title == "${name} | Android Developers"

	def classDef = [:]

	//Inheritance
	//println '---- Inheritance ----'
	def inheritbox = page.'**'.find{ it.@class == 'jd-inheritance-table' }
	//println '-- Inherits from --'
	def inherit = []
	inheritbox.'**'.findAll { it.name() == 'tr' }.each {

	  inherit << it.td[-1].text().replace("?"," ")

	}
	inherit = inherit[0 .. -2].reverse()
	classDef['inherit'] = inherit


	//Nested Classes - This method extracts the nested classes detail from each page in the package
	//println '---- Nested Classes ----'
	def NestedClasses = [:]
	def nestedClassbox = page.'**'.find{ it.name() == 'table' && it.@id == 'nestedclasses' }
	if (nestedClassbox) {

	  //handleNestedClasses(nestedClassbox, "${pkgname}.${name}", null, NestedClasses)

	}
	classDef['NestedClasses'] = NestedClasses


	//XML Attributes
	//println '-- XML Attributes --'
	def attributes = [:]
	def attrbox = page.'**'.find{ it.name() == 'table' && it.@id == 'lattrs' }
	if (attrbox) {

	  handleXMLAttributes(attrbox, "${pkgname}.${name}", null, attributes)

	}


	//Inherited XML Attributes
	//println '-- Inherited XML Attributes --'
	attrbox = page.'**'.find{ it.name() == 'table' && it.@id == 'inhattrs' }
	if (attrbox) {
	  //println '-- Inherited XML Attributes --'
	  //println 'pkgname name : ' + pkgname + ' '+ name
	  attrbox.tbody.tr.each {
		if (it.@class.toString().contains('api')) {
		  String from = it.td.a[-1].text()

		  handleXMLAttributes(it.'**'.find { t -> t.name() == 'table'}, "${pkgname}.${name}", from, attributes)

		}
	  }
	}
	classDef['attributes'] = attributes


	//Constants - This method extracts the constant detail from each page in the package
	//println '---- Constants ----'
	def Constants = [:]
	def consta = page.'**'.find { it.name() == 'table' && it.@id == 'constants' }
	if (consta) {
	  consta.tbody.tr.each {

		handleConstants(consta, "${pkgname}.${name}", null, Constants)

	  }
	}


	//Inherited Constants - This method extracts the constant detail from each page in the package
	//println '---- Inherited Contants ----'
	consta = page.'**'.find{ it.name() == 'table' && it.@id == 'inhconstants'}
	if (consta) {
	  consta.tbody.tr.each {
		if (it.@class.toString().contains('api')) {
		  String fromPkg = it.td.a[-1].text()

		  handleConstants(it.'**'.find { t -> t.name() == 'table'}, "${pkgname}.${name}", fromPkg, Constants)

		}
	  }
	}
	classDef['Constants'] = Constants


	//Fields - This method extracts the fields detail from each page in the package
	//println '---- Fields ----'
	def Fields = [:]
	def fieldBox = page.'**'.find { it.name() == 'table' && it.@id == 'lfields' }
	if (fieldBox) {
	  fieldBox.tbody.tr.each {
		if (it.@class.toString().contains('api')) {

		  handleFields(fieldBox, "${pkgname}.${name}", null, Fields)

		}
	  }
	}


	//Inherited Fields - This method extracts the inherited fields detail from each page in the package
	//println '---- Inherited Fields ----'
	fieldBox = page.'**'.find{ it.name() == 'table' && it.@id == 'inhfields'}
	if (fieldBox) {
	  fieldBox.tbody.tr.each {
		if (it.@class.toString().contains('api')) {
		  String fromPkg = it.td.a[-1].text()

		  handleFields(it.'**'.find { t -> t.name() == 'table'}, "${pkgname}.${name}", fromPkg, Fields)

		}
	  }
	}
	classDef['Fields'] = Fields


	//Public Constructors - This method extracts the public constructor detail from each page in the package
	//println '---- Public Constructors ----'
	def PublicConstructors = [:]
	def publicConstbox = page.'**'.find{ it.name() == 'table' && it.@id == 'pubctors' }
	if (publicConstbox) {

	  handlePublicConst(publicConstbox, "${pkgname}.${name}", null, PublicConstructors)

	}
	classDef['PublicConstructors'] = PublicConstructors


	//Protected Constructors - This method extracts the protected constructor detail from each page in the package
	//println '---- Protected Constructors ----'
	def ProtectedConstructors = [:]
	def proConstbox = page.'**'.find{ it.name() == 'table' && it.@id == 'proctors' }
	if (proConstbox) {

	  handleProtectedConst(proConstbox, "${pkgname}.${name}", null, ProtectedConstructors)

	}
	classDef['ProtectedConstructors'] = ProtectedConstructors


	//Public Methods - This method extracts the public method detail from each page in the package
	//println '---- Public Methods ----'
	def PublicMethods = [:]
	def publicMethodbox = page.'**'.find{ it.name() == 'table' && it.@id == 'pubmethods' }
	if (publicMethodbox) {

	  handlePublicMethods(publicMethodbox, "${pkgname}.${name}", null, PublicMethods)

	}
	classDef['PublicMethods'] = PublicMethods


	//Protected Methods - This method extracts the protected method detail from each page in the package
	//println '---- Protected Methods ----'
	def ProtectedMethods = [:]
	def protectedMethodbox = page.'**'.find{ it.name() == 'table' && it.@id == 'promethods' }
	if (protectedMethodbox) {

	  handleProtectedMethods(protectedMethodbox, "${pkgname}.${name}", null, ProtectedMethods)

	}
	classDef['ProtectedMethods'] = ProtectedMethods


	//Inherited Methods - This method extracts the inherited method detail from each page in the package
	//println '---- Inherited Methods  ----'
	def InheritedMethods = [:]
	def inheritMethodBox = page.'**'.find{ it.name() == 'table' && it.@id == 'inhmethods'}
	if (inheritMethodBox) {
	  inheritMethodBox.tbody.tr.each {
		if (it.@class.toString().contains('api')) {
		  String fromPkg = it.td.a[-1].text()

		  handleInheritedMethods(it.'**'.find { t -> t.name() == 'table'}, "${pkgname}.${name}", fromPkg, InheritedMethods)

		}
	  }
	}
	classDef['InheritedMethods'] = InheritedMethods


	//Attribute get set methods - This method extracts the attribute set get method detail from each page in the package
	//println '---- Attribute get set methods  ----'
	def AttributeGetSetMethods = [:]
	def attBox = page.'**'.find{it.name() == 'table' && it.@id == 'lattrs' }
	if (attBox) {
	  def pmethod = page.'**'.find{it.name() == 'table' && it.@id == 'pubmethods' }

	  handleAttributeGetSet(attBox, "${pkgname}.${name}", null, AttributeGetSetMethods, pmethod)

	}
	classDef['AttributeGetSetMethods'] = AttributeGetSetMethods


	String[] virtualAttName = ['CheckBox', 'ToggleButton']

	//Attribute get set methods - This method extracts the virtual attribute set get method detail from each page in the package
	//println '---- Virtual attributes get set method  ----'
	def VirtualAttribute = [:]
	def VirtualAttBox = page.'**'.find{ it.name() == 'table' && it.@id == 'inhmethods'}
	if (VirtualAttBox) {
	  VirtualAttBox.tbody.tr.each {
		if (it.@class.toString().contains('api')) {

		  String fromPkg = it.td.a[-1].text()

		  for (int i = 0; i < virtualAttName.length; i++)
		  {
			if(name == virtualAttName[i]){

			  handleVirtualAtt(it.'**'.find { t -> t.name() == 'table'}, name, fromPkg, VirtualAttribute)

			}
		  }
		}
	  }
	}
	classDef['VirtualAttribute'] = VirtualAttribute


	def d1= new File(output_base + '/' + pkgname)
	d1.mkdirs()
	Yaml.dump(classDef, new File(output_base + "/${pkgname}/${name}_Def.yml"), true)

  }

  static AttributeDef = [
	'android.widget.ToggleButton': [
	  checked: 'boolean',
	],

	'android.widget.Spinner': [
	  selectedItemPosition: 'int',
	],

	'android.widget.ProgressBar': [
	  animationResolution: 'int',
	  indeterminate: 'boolean',
	  indeterminateBehavior: 'int',
	  indeterminateDrawable: 'String',
	  indeterminateDuration: 'boolean',
	  indeterminateOnly: 'boolean',
	  interpolator: 'int',
	  max: 'int',
	  maxHeight: 'String',
	  maxWidth: 'String',
	  minHeight: 'String',
	  minWidth: 'String',
	  progress: 'int',
	  progressDrawable: 'String',
	  secondaryProgress: 'int',
	],
  ]

  static publicMethodPattern1 = ~/(\w*)\s*\(\)/
  static publicMethodPattern2 = ~/\((((\w*\.\w*)\s*(\w*))|((\w*)\s*(\w*)))\)/

  static handleVirtualAtt(table, className, fromPkg, VirtualAttribute){
	def vsetmethod = 'setChecked'
	def vgetmethod = 'isChecked'
	def triple, imType, imName, imMethodDes, vset, vget, smdef, gmdef

	table.tbody.tr.each {
	  if (it.@class.toString().contains('api')) {

		triple = it.td
		imType = triple[0].text().trim()
		imType = imType.replaceAll("\\r|\\n", "").replaceAll("\\s+", " ")
		imName = it.td[1].nobr.text()

		def index = imName.lastIndexOf('(')
		def methodName = imName.substring(0,index)


		if(methodName.toLowerCase().contains(vsetmethod.toLowerCase())){
		  vset = imName
		}
		if(methodName.toLowerCase().contains(vgetmethod.toLowerCase())){
		  vget = imName
		}

	  }
	}
	if(vset != null)
	  {
		smdef = matchMethods(vset)
	  }
	if(vget != null)
	  {
		gmdef = matchMethods(vget)
	  }

	VirtualAttribute[className] = [VirtualAttribute : className, SetMethod : smdef, GetMethod :gmdef]

  }

  static handleXMLAttributes(table, classname, from, attributes) {
	table.tbody.tr.each {
	  if (it.@class.toString().contains('api')) {
		def triple = it.td
		def pname = triple[0].text()

		def shortName = pname
		int i = pname.lastIndexOf(':')
		if (i >= 0) {
		  shortName = pname.substring(i + 1)
		}

		if (classname == 'android.view.View' ||
			!(shortName in filteredAttributeNames)) {

		  def method = triple[1].text()
		  def explanation = triple[2].text()
		  def mname = ''
		  def type = ''
		  if (method) {
			def match = method =~ methodPattern1
			if(match && match[0].size > 1) {
			  mname = match[0][1]
			  type = match[0][2]
			}
		  }

		  if (!type && AttributeDef[classname]) {
			type = AttributeDef[classname][shortName]
		  }
		  if (!type && from && AttributeDef[from]) {
			type = AttributeDef[from][shortName]
		  }

		  if (type) {
			def pdef = [name: pname, method: method, methodName: mname, type: type,
						explanation: explanation]
			if (from) {
			  pdef['from'] = from
			}
			attributes[shortName] = pdef
		  }
		}

		if (AttributeDef[classname]) {
		  AttributeDef[classname].keySet().each { a ->
			if (attributes[a] == null) {
			  attributes[a] = [ name: a, type: AttributeDef[classname][a] ]
			}
		  }
		}
	  }
	}
  }

  static handleNestedClasses(table, className, from, nestedClasses){
	def triple, nType, nClass, nDec
	table.tbody.tr.each {
	  if (it.@class.toString().contains('api')) {
		triple = it.td
		nType = triple[0].text().trim() //nested class type
		nClass = triple[1].text()   //nested class name
		nDec = triple[2].text().trim().replaceAll("\\?","").replaceAll("\\r|\\n", "").replaceAll("\\s+", " ") //nested class description

		nestedClasses[nClass] = [ NestedClassType: nType, NestedClassName :nClass, Description :nDec ]
	  }
	}
  }

  static handleConstants(table, className, fromPkg, constants){
	def triple, inhConstType, inhConst, inhConstDes
	table.tbody.tr.each {
	  if (it.@class.toString().contains('api')) {
		triple = it.td
		inhConstType = triple[0].text()    //constant type
		inhConst = triple[1].text()    //constant name
		inhConstDes = triple[2].text().trim().replaceAll("\\r|\\n", " ").replaceAll("\\s+", " ").replaceAll("\\\">", " ").replaceAll("\\?", "") //constant description

		constants[inhConst] = [ ConstantType : inhConstType, ConstatntName : inhConst, Decription : inhConstDes ]
	  }
	}
  }

  static handleFields(table, className , from, fields){
	def triple,fType, fName, fDes
	table.tbody.tr.each {
	  if (it.@class.toString().contains('api')) {
		triple = it.td
		fType = triple[0].text().trim() //field type
		fType = fType.replaceAll("\\r|\\n", "").replaceAll("\\s+", " ").replaceAll("\\?","")    //field type

		def fVisi, sFinal
		String[] tokens = fType.split(" ");
		if(tokens.length == 4)
		  {
			fVisi = tokens[0]
			sFinal = tokens[1] + " " + tokens[2]
			fType = tokens[3]
		  }
		else if (tokens.length == 2)
		{
		  fVisi = tokens[0]
		  sFinal = ''
		  fType = tokens[1]
		}

		fName = triple[1].text()    //field name
		fDes = triple[2].text().replaceAll("\\r|\\n", " ").replaceAll("\\s+", " ")  //field description

		fields[fName] = [Visibility :fVisi, StaicFinal : sFinal, FieldType : fType, FieldName : fName, Decription : fDes]
	  }
	}
  }

  static handlePublicConst(table, className, from, PublicConstructors){
	table.tbody.tr.each {
	  def triple, pConst, pConstDes
	  if (it.@class.toString().contains('api')) {
		triple = it.td
		pConst = triple[1].nobr.text()  //public constructor
		pConstDes = triple[1].div.text().trim().replaceAll("\\r|\\n", "").replaceAll("\\s+", " ")   //public constructor description

		def mdef = matchMethods(pConst)
		def pcName = mdef['methodName']

		PublicConstructors[pConst] = [ PublicConst : mdef, Description : pConstDes ]

	  }
	}
  }

  static handleProtectedConst(table, className, from, proConst){
	def triple, proCont, proConstDes, mdef, proName
	table.tbody.tr.each {
	  if (it.@class.toString().contains('api')) {
		triple = it.td
		proCont = triple[1].nobr.text()     //protected constructor name
		proConstDes = triple[1].div.text()  //protected constructor description

		mdef = matchMethods(proCont)
		proName = mdef['methodName']

		proConst[proCont] = [ ProtectedConstructor : mdef, Description :  proConstDes]

	  }
	}
  }

  static handlePublicMethods(table, className, from, pMethods){
	def pMethodType, pMethod, pMethodDes, opMethod
	table.tbody.tr.each {
	  if (it.@class.toString().contains('api')) {
		def triple = it.td
		pMethodType = triple[0].text().stripMargin().trim() //public method type
		pMethodType = pMethodType.replaceAll("\\r|\\n", "").replaceAll("\\s+", " ")
		def tarray = pMethodType.split(' ')
		if (tarray.length > 1) { 
		  pMethodType = tarray[-1]
		}
		pMethod = it.td[1].nobr.text().trim()   //public method name

		opMethod = pMethod
		def pmname
		if(pMethod != null) {
		  pmname = removeFromMethod(pMethod)
		  pMethod = pmname
		}

		pMethodDes = triple[1].div.text().trim().replaceAll("\\r|\\n", "").replaceAll("\\s+", " ")  //public method description

		def mdef = matchMethods(opMethod)
		def pmName = mdef['methodName']

		pMethods[pMethod] = [ MethodType : pMethodType, Method : mdef, Decription : pMethodDes ]

	  }
	}
  }

  static handleProtectedMethods(table, className, from, pMethods){
	def pMethodType, pMethod, pMethodDes, opMethod
	table.tbody.tr.each {
	  if (it.@class.toString().contains('api')) {
		def triple = it.td
		pMethodType = triple[0].text().trim().replaceAll("\\r|\\n", "").replaceAll("\\s+", " ") //protected method type
		def tarray = pMethodType.split(' ')
		if (tarray.length > 1) { 
		  pMethodType = tarray[-1]
		}
		pMethod = it.td[1].nobr.text()  //protected method name

		opMethod = pMethod

		def pmname
		if(pMethod != null) {
		  pmname = removeFromMethod(pMethod)
		  pMethod = pmname
		}

		def mdef = matchMethods(opMethod)

		pMethodDes = triple[1].div.text().replaceAll("\\r|\\n", "").replaceAll("\\s+", " ").replaceAll("\\)\\\">", ' ') //protected method description

		pMethods[pMethod] = [ MethodType : pMethodType, Method : mdef, Decription : pMethodDes ]
	  }
	}
  }

  static handleInheritedMethods(table, className, fromPkg, InheritedMethods){
	table.tbody.tr.each {
	  def triple, imType, imName, imMethodDes, opMethod
	  if (it.@class.toString().contains('api')) {
		triple = it.td
		imType = triple[0].text().trim()    //inherited method type
		imType = imType.replaceAll("\\r|\\n", "").replaceAll("\\s+", " ")
		def tarray = imType.split(' ')
		if (tarray.length > 1) { 
		  imType = tarray[-1]
		}

		imName = it.td[1].nobr.text()   //inherited method name

		opMethod = imName

		def pmname
		if(imName != null)
		  {
			pmname = removeFromMethod(imName)
			imName = pmname
		  }

		def mdef = matchMethods(opMethod)

		imMethodDes = triple[1].div.text().trim().replaceAll("\\r|\\n", "").replaceAll("\\s+", " ").replaceAll("\\)\\\">", ' ') //inherited method description

		def inmName = mdef['methodName']

		InheritedMethods[imName] = [ MethodType : imType, Method : mdef, MethodDes : imMethodDes ]
	  }
	}
  }

  static handleAttributeGetSet(table, className, from, att, pmethod){
	def triple, attributeName,methodName, attributeSetMethod,attributeGetMethod, attributeDec
	def shortName, getValue = false
	String pMethodName
	table.tbody.tr.each {
	  if (it.@class.toString().contains('api')) {
		triple = it.td
		attributeName = triple[0].text()    //attribute name
		methodName = triple[1].text()   //attribute method name
		attributeSetMethod= triple[1].text()

		attributeGetMethod= triple[1].text()

		attributeDec = triple[2].text()


		int i = attributeName.lastIndexOf(':')
		shortName =  attributeName.substring(i + 1)

		if ( methodName != '')
		  {
			int j = methodName.lastIndexOf('(')
			def k = methodName.substring(0,3)

			if(methodName.substring(0,3) == 'set'){
			  attributeSetMethod = methodName
			  attributeGetMethod = ''
			}
			else if(methodName.substring(0,3) == 'get'){
			  getValue = true
			  attributeGetMethod = methodName
			  attributeSetMethod = ''
			}
			else if(methodName.substring(0,2) == 'is'){
			  attributeGetMethod = methodName
			  attributeSetMethod = ''
			}

			pmethod.tbody.tr.each {
			  if (it.@class.toString().contains('api')) {
				pMethodName = it.td[1].nobr.text()

				if(attributeGetMethod != '')
				  {def method, containMethod

					int m1 = attributeGetMethod.lastIndexOf('(')
					if(getValue == true)
					  {
						method = attributeGetMethod.substring(3,m1)

						containMethod = 'set'+  method

						if(pMethodName.toLowerCase().contains(containMethod.toLowerCase())){
						  attributeSetMethod = pMethodName
						}
					  }
					else
					  {
						method = attributeGetMethod.substring(2,m1)
						containMethod = 'set'+  method

						if(pMethodName.toLowerCase().contains(containMethod.toLowerCase())){
						  attributeSetMethod = pMethodName
						}
					  }
				  }
				else if (attributeSetMethod != '')
				{
				  def containMethodGet, containMethodIs, methodGet

				  int m2 = attributeSetMethod.lastIndexOf('(')

				  methodGet = attributeSetMethod.substring(3,m2)
				  containMethodGet = 'get'+  methodGet

				  containMethodIs = 'is'+  methodGet

				  if(pMethodName.toLowerCase().contains(containMethodGet.toLowerCase())){
					attributeGetMethod = pMethodName
				  }
				  else if(pMethodName.toLowerCase().contains(containMethodIs.toLowerCase()))
				  {
					attributeGetMethod = pMethodName
				  }

				}
			  }
			}

			if (attributeSetMethod != ''){
			  def mdefSet = matchMethods(attributeSetMethod)
			  attributeSetMethod = mdefSet
			}

			if (attributeGetMethod != ''){
			  def mdefGet = matchMethods(attributeGetMethod)
			  attributeGetMethod = mdefGet
			}
		  }
		else
		  {
			def vset, vget, vis
			vset = 'set' + shortName
			vget = 'get' + shortName
			vis = 'is' + shortName

			if(pmethod != null)
			  {

				pmethod.tbody.tr.each {

				  if (it.@class.toString().contains('api')) {


					pMethodName = it.td[1].nobr.text()

					if(pMethodName.toLowerCase().contains(vget.toLowerCase())){
					  attributeGetMethod = pMethodName
					}
					if(pMethodName.toLowerCase().contains(vset.toLowerCase())){
					  attributeSetMethod = pMethodName
					}
					if(pMethodName.toLowerCase().contains(vis.toLowerCase())){
					  attributeGetMethod = pMethodName
					}
				  }

				}
				if(attributeGetMethod != null)
				  {
					def mdefGet = matchMethods(attributeGetMethod)
					attributeGetMethod = mdefGet
				  }
				if(attributeSetMethod != null)
				  {
					def mdefSet = matchMethods(attributeSetMethod)
					attributeSetMethod = mdefSet
				  }
			  }
			else {

			  attributeSetMethod = ''
			  attributeGetMethod = ''
			  return
			}

		  }

		att[attributeName] = [ Attributename : shortName, setMethod : attributeSetMethod, getMethod : attributeGetMethod ]
	  }
	}
  }

  static matchMethods(String s){
	def oParaIndex, cParaIndex
	def result = [:]
	def MethodParameters = []

	if(s != null)
	  {
		oParaIndex = s.lastIndexOf('(')

		if(oParaIndex != -1)
		  {
			if(s.substring(0,oParaIndex).contains('>') == true)
			  {
				def mname = s.substring(0,oParaIndex)
				def i = mname.lastIndexOf('>')

				def a = mname.substring(i,oParaIndex)

				result['methodName'] = mname.substring(i + 1,oParaIndex)
			  }
			else
			  {
				result['methodName'] = s.substring(0,oParaIndex)
			  }
		  }

		cParaIndex = s.lastIndexOf(')')

		def diff = cParaIndex   - (oParaIndex + 1 )

		if(diff > 0)
		  {
			def paraList = s.substring(oParaIndex + 1,cParaIndex)

			String delims = ",";
			String[] tokens = paraList.split(delims);

			String paraTypes, parameters

			for (int i = 0; i < tokens.length; i++){

			  if(tokens[i].contains(" ")) {

				def pType = tokens[i].substring(0, tokens[i].lastIndexOf(' ')).trim()

				def p = tokens[i].substring( tokens[i].lastIndexOf(' '), tokens[i].length()).trim()

				MethodParameters <<  [ParameterType :pType,
									  Parameter : p]
			  }else
				{
				  def pType = tokens[i].trim()

				  def p = ''

				  MethodParameters <<  [ParameterType :pType,
										Parameter : p]
				}
			}
			result['MethodParameters'] = MethodParameters
		  }
		return result
	  }
  }

  static removeFromMethod(pMethod){
	def pmname
	def i = pMethod.lastIndexOf('(')
	def a = pMethod.substring(0,i)
	def c = a.contains('>')

	if(c == true)
	  {   def j = a.lastIndexOf('>')
		pmname = pMethod.substring(j+1,i-1)
		pMethod = pmname
	  }

	return pMethod
  }

}

