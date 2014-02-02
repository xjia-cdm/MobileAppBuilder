package xj.translate.common

import xj.translate.Language

class Templates { 

  static Templates templatesForLanguage(Language lang) { 
    if (lang == Language.Java) { 
      return new TemplatesJava()
    } else if (lang == Language.ObjectiveC) { 
      return new TemplatesObjectiveC()
    }
    return new Templates()
  }

  def antFileGroovy =
'''<?xml version="1.0"?>
<project name="$appname" default="compile" basedir=".">
	
  <property name="src" value="."/>
  <property name="classes" value="classes"/>
  <property environment="env"/>

  <path id="compile.classpath">
    <fileset dir="\\${env.GROOVY_HOME}/lib">
      <include name="*.jar" />
    </fileset>
  </path>

  <taskdef name="groovyc"
	   classname="org.codehaus.groovy.ant.Groovyc"
	   classpathref="compile.classpath"/>

  <target name="prepare">
    <mkdir dir="\\${classes}"/>
  </target>

  <target name="compile" depends="prepare">
    <groovyc srcdir="\\${src}"
             destdir="\\${classes}">
      <classpath>
	<pathelement path="\\${classes}"/>
	<path refid="compile.classpath"/>
      </classpath>
      <include name="$filelist"/>
      <javac source="1.5" target="1.5" debug="on" />
    </groovyc>
  </target>

  <target name="run" depends="compile">
    <java classname="$mainclass"
          dir="\\${classes}"
          fork="yes" >
      <classpath location="\\${classes}" />
     </java>
  </target>

</project>
'''

  def antFileJava = 
'''<?xml version="1.0"?>
<project name="$appname" default="compile" basedir=".">
	
  <property name="src" value="."/>
  <property name="classes" value="classes"/>

  <target name="prepare">
    <mkdir dir="\\${classes}"/>
  </target>

  <target name="compile" depends="prepare">
    <javac includeantruntime="false"
           srcdir="\\${src}"
           destdir="\\${classes}"
           includes="$filelist">
    </javac>
  </target>

  <target name="run" depends="compile">
    <java classname="$mainclass"
          dir="\\${classes}"
          fork="yes" >
      <classpath location="\\${classes}" />
     </java>
  </target>

</project>
'''

  def fileHeader = 
'''/*
 *  Filename: $outfile
 *  $message
 *  Generated from $infile
 *  Generated on ${new Date()}
 *
 *  Options: $options
 *  Config: $config
 */
'''

  def packageDeclaration = 
'''package $name;
'''

  def preamble =
'''$header
$pkg$imports$forwardDecl
'''

  def classDef = 
'''
$modifiers$visibility$kind $name $extend$implement{
$fieldDecl$methodDef$innerClassDef
}
'''

  def fieldDeclaration =
'''
$modifiers$visibility$type $name$init;
'''

  def getterDef = 
'''
public ${mod}$type $methodName() {
  return $name; 
}
'''

  def setterDef = 
'''
public ${mod}void $methodName($type $name) {
  $self.$name = $name; 
}
'''

  def methodParameter = '$type $name'

  def methodSignature = '$modifiers$visibility$returnType$name($params)'

  def methodDeclaration =
'''
$mod$signature;
'''

  def methodDefinition =
'''
$signature {
$body
}
'''

  def scriptMain = ''

}

class TemplatesJava extends Templates {  

  def scriptMain = 
'''
static public void main(String[] args) {
  new $name().run();
}
'''

  def closureDef =
'''
class $name {
  public $type call($params) { 
$code 
  } 
}
'''

}

class TemplatesObjectiveC extends TemplatesJava { 

  def makefile =
'''
CC=clang
APP_NAME=${appname}
FILES=${filelist}
FLAGS=-I. -framework Foundation

all	: 
	\\$(CC) -o \\$(APP_NAME) \\$(FILES) \\$(FLAGS)

clean	:
	rm \\$(APP_NAME)
'''

  def gnustepMakefile = 
'''include \\$(GNUSTEP_MAKEFILES)/common.make

APP_NAME = ${appname}
${appname}_OBJC_FILES = ${filelist}

include \\$(GNUSTEP_MAKEFILES)/application.make
'''

  def classDef = 
'''
$staticVar
@implementation $name 

$propertyImpl
$methodImpl
@end
$innerClass
'''

  def classInf =
'''
$constantDecl$innerClass
@interface $name $extend$implement
{
$instanceVar
}
$propertyDecl
$methodDecl 
@end
'''

  def protocol =
'''
$constantDecl$innerClass
@protocol $name $implement
$methodDecl 
@end
'''

  def enumDef =
'''
typedef enum {
  $values
} $name; 
'''

  def variableDeclaration =
'''
${mod}$type $name$init;
'''

  def propertyDeclaration =
'''
@property$attribute $type $name;
'''

  def propertyImplementation =
'''
@synthesize $name;
'''

  def getterDef = 
'''
${mod}($type) $methodName {
  return $name; 
}
'''

  def setterDef = 
'''
${mod}(void) $methodName:($type) _$name {
  $name = _$name; 
}
'''
  def getterDecl = 
'''
${mod}($type) $methodName;
'''

  def setterDecl = 
'''
${mod}(void) $methodName:($type) _$name;
'''

  def methodSignature = '$modifiers($returnType) $name$params'

  def methodParameter = '($type) $name'

  def scriptMain = 
'''
int main(int argc, char *argv[]) 
{
  NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
  [[[$name alloc] init] run];
  [pool release];
  return 0;
}
'''

  def closureDef =
'''^$type ($params) { 
$code 
}'''

}
