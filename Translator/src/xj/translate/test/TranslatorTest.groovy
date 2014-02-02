package xj.translate.test

import org.junit.*
import static org.junit.Assert.*

import xj.translate.Main
import xj.translate.Logger

/*
  To add:
  TestConstructor01.groovy	TestMethods02.groovy		TestMethods04.groovy		TestOperators01.groovy
  TestMethods01.groovy		TestMethods03.groovy		TestMethods05.groovy		TestOperators02.groovy
  TestOperators03.groovy
 */

class TranslatorTest { 

  //@Test 
  public void test0() {
    //println "ls -al".execute().text
    println 'bin/gt'.execute().text
    println 'diff ref/file1.txt ref/file2.txt'.execute().exitValue()
    println 'diff ref/file1.txt ref/file3.txt'.execute().exitValue()
    assertTrue(true)
  }

  @BeforeClass
  static void setup() { 
    Logger.logLevel = Logger.NONE
  }

  def dir1 = 'test/groovy/trans/simple';
  def fileset1j = [
    'Test1':  [ 'pkg1/pkg2/Test1.java' ],
    'Test2':  [ 'xj/pkg1/A.java', 'xj/pkg1/AA.java', 'xj/pkg1/B.java', 
	        'xj/pkg1/BB.java', 'xj/pkg1/C.java', 'xj/pkg1/C1.java', 
	        'xj/pkg1/CC.java', 'xj/pkg1/D.java', 'xj/pkg1/I1.java', 
	        'xj/pkg1/I2.java', 'xj/pkg1/Test2.java' ],
    'Test3':  [ 'xj/pkg2/A.java', 'xj/pkg2/B.java', 'xj/pkg2/E.java', 
	        'xj/pkg2/Test3.java' ],
    'Test4':  [ 'pkg1/pkg2/Test4.java', 'pkg1/pkg2/Test.java' ],
    'Test5':  [ 'A.java', 'B.java', 'Test5.java' ],
    'Test6':  [ 'Test6.java' ],    
    'Test7':  [ 'B.java', 'C.java', 'I.java', 'Test7.java' ],    
    'Test8':  [ 'A.java', 'E.java', 'Test8.java' ],
    'Test9':  [ 'A.java', 'Test9.java' ],
    'Test10': [ 'A.java', 'Test10.java' ],
    'Test11': [ 'A.java', 'B.java', 'Test11.java' ],
    'Test12': [ 'A.java', 'B.java', 'Test12.java' ],
    'Test13': [ 'Test13.java' ],
    'Test14': [ 'A.java', 'Test14.java' ],
    'Test15': [ 'A.java', 'B.java', 'Test15.java' ],

    'Test100' : [ 'Test100.java' ], 
    'Test140' : [ 'Test140.java' ], 
  ];

  def fileset1c = [
    'Test1':  [ 'Test1.h', 'Test1.m' ], 
    'Test2':  [ 'A.h', 'A.m', 'AA.h', 'AA.m', 'B.h', 'B.m', 'BB.h', 'BB.m', 
	        'C.h', 'C.m', 'C1.h', 'C1.m', 'CC.h', 'CC.m', 'D.h', 'D.m', 
	        'I1.h', 'I2.h', 'Test2.h', 'Test2.m' ],
    'Test3':  [ 'A.h', 'A.m', 'B.h', 'B.m', 'E.h', 'Test3.h', 'Test3.m' ], 
    'Test4':  [ 'Test4.h', 'Test4.m', 'Test.h', 'Test.m' ], 
    'Test5':  [ 'A.h', 'A.m', 'B.h', 'B.m', 'Test5.h', 'Test5.m' ], 
    'Test6':  [ 'Test6.h', 'Test6.m' ], 
    'Test7':  [ 'B.h', 'B.m', 'C.h', 'C.m', 'I.h', 'Test7.h', 'Test7.m' ], 
    'Test8':  [ 'A.h', 'A.m', 'E.h', 'Test8.h', 'Test8.m' ], 
    'Test9':  [ 'A.h', 'A.m', 'Test9.h', 'Test9.m' ], 
    'Test10': [ 'A.h', 'A.m', 'Test10.h', 'Test10.m' ], 
    'Test11': [ 'A.h', 'A.m', 'B.h', 'B.m', 'Test11.h', 'Test11.m' ], 
    'Test12': [ 'A.h', 'A.m', 'B.h', 'B.m', 'Test12.h', 'Test12.m' ], 
    'Test13': [ 'Test13.h', 'Test13.m' ], 
    'Test14': [ 'A.h', 'A.m', 'Test14.h', 'Test14.m' ], 
    'Test15': [ 'A.h', 'A.m', 'B.h', 'B.m', 'Test15.h', 'Test15.m' ], 

    'Test100' : [ 'Test100.h', 'Test100.m' ], 
    'Test140' : [ 'Test140.h', 'Test140.m' ], 
  ];

  def fileset1g = [
    'Test1':  [ 'pkg1/pkg2/Test1.groovy' ],
    'Test2':  [ 'xj/pkg1/Test2.groovy' ],
    'Test3':  [ 'xj/pkg2/Test3.groovy' ],
    'Test4':  [ 'pkg1/pkg2/Test4.groovy' ],
    'Test5':  [ 'Test5.groovy' ],
    'Test6':  [ 'Test6.groovy' ],    
    'Test7':  [ 'Test7.groovy' ],    
    'Test8':  [ 'Test8.groovy' ],    
    'Test9':  [ 'Test9.groovy' ],    
    'Test10': [ 'Test10.groovy' ],    
    'Test11': [ 'Test11.groovy' ],    
    'Test12': [ 'Test12.groovy' ],    
    'Test13': [ 'Test13.groovy' ],    
    'Test14': [ 'Test14.groovy' ],    
    'Test15': [ 'Test15.groovy' ],    

    'Test100' : [ 'Test100.groovy' ], 
    'Test140' : [ 'Test140.groovy' ], 
  ];

  void testCaseJavaSimple(file) { 
    testCase(dir1, file, [ 'build.xml' ] + fileset1j[file], 'Java')
  }

  void testCaseObjcSimple(file) { 
    testCase(dir1, file, [ 'makefile' ] + fileset1c[file], 'Objc')
  }

  void testCaseGroovySimple(file) { 
    testCase(dir1, file, [ 'build.xml' ] + fileset1g[file], 'Groovy')
  }

  void testCaseRawSimple(file) { 
    testCase(dir1, file, [ 'build.xml' ] + fileset1g[file], 'Raw')
  }

  /// Java

  @Test(timeout=10000L)
  public void test1() {
    testCaseJavaSimple('Test1')
  }

  @Test(timeout=10000L) 
  public void test2() {
    testCaseJavaSimple('Test2')
  }

  @Test(timeout=10000L) 
  public void test3() {
    testCaseJavaSimple('Test3')
  }

  @Test(timeout=10000L) 
  public void test4() {
    testCaseJavaSimple('Test4')
  }

  @Test(timeout=10000L) 
  public void test5() {
    testCaseJavaSimple('Test5')
  }

  @Test(timeout=10000L) 
  public void test6() {
    testCaseJavaSimple('Test6')
  }

  @Test(timeout=10000L) 
  public void test7() {
    testCaseJavaSimple('Test7')
  }

  @Test(timeout=10000L) 
  public void test8() {
    testCaseJavaSimple('Test8')
  }

  @Test(timeout=10000L) 
  public void test9() {
    testCaseJavaSimple('Test9')
  }

  @Test(timeout=10000L) 
  public void test10() {
    testCaseJavaSimple('Test10')
  }

  @Test(timeout=10000L) 
  public void test11() {
    testCaseJavaSimple('Test11')
  }

  @Test(timeout=10000L) 
  public void test12() {
    testCaseJavaSimple('Test12')
  }

  @Test(timeout=10000L) 
  public void test13() {
    testCaseJavaSimple('Test13')
  }

  @Test(timeout=10000L) 
  public void test14() {
    testCaseJavaSimple('Test14')
  }

  @Test(timeout=10000L) 
  public void test15() {
    testCaseJavaSimple('Test15')
  }

  @Test(timeout=10000L) 
  public void test100() {
    testCaseJavaSimple('Test100')
  }

  @Test(timeout=10000L) 
  public void test140() {
    testCaseJavaSimple('Test140')
  }

  /// Obj-C

  @Test(timeout=10000L)
  public void test1c() {
    testCaseObjcSimple('Test1')
  }

  @Test(timeout=10000L) 
  public void test2c() {
    testCaseObjcSimple('Test2')
  }

  @Test(timeout=10000L) 
  public void test3c() {
    testCaseObjcSimple('Test3')
  }

  @Test(timeout=10000L) 
  public void test4c() {
    testCaseObjcSimple('Test4')
  }

  @Test(timeout=10000L) 
  public void test5c() {
    testCaseObjcSimple('Test5')
  }

  @Test(timeout=10000L) 
  public void test6c() {
    testCaseObjcSimple('Test6')
  }

  @Test(timeout=10000L) 
  public void test7c() {
    testCaseObjcSimple('Test7')
  }

  @Test(timeout=10000L) 
  public void test8c() {
    testCaseObjcSimple('Test8')
  }

  @Test(timeout=10000L) 
  public void test9c() {
    testCaseObjcSimple('Test9')
  }

  @Test(timeout=10000L) 
  public void test10c() {
    testCaseObjcSimple('Test10')
  }

  @Test(timeout=10000L) 
  public void test11c() {
    testCaseObjcSimple('Test11')
  }

  @Test(timeout=10000L) 
  public void test12c() {
    testCaseObjcSimple('Test12')
  }

  @Test(timeout=10000L) 
  public void test13c() {
    testCaseObjcSimple('Test13')
  }

  @Test(timeout=10000L) 
  public void test14c() {
    testCaseObjcSimple('Test14')
  }

  @Test(timeout=10000L) 
  public void test15c() {
    testCaseObjcSimple('Test15')
  }

  @Test(timeout=10000L) 
  public void test100c() {
    testCaseObjcSimple('Test100')
  }

  @Test(timeout=10000L) 
  public void test140c() {
    testCaseObjcSimple('Test140')
  }

  /// Groovy

  @Test(timeout=10000L)
  public void test1g() {
    testCaseGroovySimple('Test1')
  }

  @Test(timeout=10000L) 
  public void test2g() {
    testCaseGroovySimple('Test2')
  }

  @Test(timeout=10000L) 
  public void test3g() {
    testCaseGroovySimple('Test3')
  }

  @Test(timeout=10000L) 
  public void test4g() {
    testCaseGroovySimple('Test4')
  }

  @Test(timeout=10000L) 
  public void test5g() {
    testCaseGroovySimple('Test5')
  }

  @Test(timeout=10000L) 
  public void test6g() {
    testCaseGroovySimple('Test6')
  }

  @Test(timeout=10000L) 
  public void test7g() {
    testCaseGroovySimple('Test7')
  }

  @Test(timeout=10000L) 
  public void test8g() {
    testCaseGroovySimple('Test8')
  }

  @Test(timeout=10000L) 
  public void test9g() {
    testCaseGroovySimple('Test9')
  }

  @Test(timeout=10000L) 
  public void test10g() {
    testCaseGroovySimple('Test10')
  }

  @Test(timeout=10000L) 
  public void test11g() {
    testCaseGroovySimple('Test11')
  }

  @Test(timeout=10000L) 
  public void test12g() {
    testCaseGroovySimple('Test12')
  }

  @Test(timeout=10000L) 
  public void test13g() {
    testCaseGroovySimple('Test13')
  }

  @Test(timeout=10000L) 
  public void test14g() {
    testCaseGroovySimple('Test14')
  }

  @Test(timeout=10000L) 
  public void test15g() {
    testCaseGroovySimple('Test15')
  }

  @Test(timeout=10000L) 
  public void test100g() {
    testCaseGroovySimple('Test100')
  }

  @Test(timeout=10000L) 
  public void test140g() {
    testCaseGroovySimple('Test140')
  }

  /// Raw

  @Test(timeout=10000L)
  public void test1r() {
    testCaseRawSimple('Test1')
  }

  @Test(timeout=10000L) 
  public void test2r() {
    testCaseRawSimple('Test2')
  }

  @Test(timeout=10000L) 
  public void test3r() {
    testCaseRawSimple('Test3')
  }

  @Test(timeout=10000L) 
  public void test4r() {
    testCaseRawSimple('Test4')
  }

  @Test(timeout=10000L) 
  public void test5r() {
    testCaseRawSimple('Test5')
  }

  @Test(timeout=10000L) 
  public void test6r() {
    testCaseRawSimple('Test6')
  }

  @Test(timeout=10000L) 
  public void test7r() {
    testCaseRawSimple('Test7')
  }

  @Test(timeout=10000L) 
  public void test8r() {
    testCaseRawSimple('Test8')
  }

  @Test(timeout=10000L) 
  public void test9r() {
    testCaseRawSimple('Test9')
  }

  @Test(timeout=10000L) 
  public void test10r() {
    testCaseRawSimple('Test10')
  }

  @Test(timeout=10000L) 
  public void test11r() {
    testCaseRawSimple('Test11')
  }

  @Test(timeout=10000L) 
  public void test12r() {
    testCaseRawSimple('Test12')
  }

  @Test(timeout=10000L) 
  public void test13r() {
    testCaseRawSimple('Test13')
  }

  @Test(timeout=10000L) 
  public void test14r() {
    testCaseRawSimple('Test14')
  }

  @Test(timeout=10000L) 
  public void test15r() {
    testCaseRawSimple('Test15')
  }

  @Test(timeout=10000L) 
  public void test100r() {
    testCaseRawSimple('Test100')
  }

  @Test(timeout=10000L) 
  public void test140r() {
    testCaseRawSimple('Test140')
  }

  def dir2 = 'test/groovy/trans'
  def fileset2j = [
    'TestClosure01'    : [ 'TestClosure01.java' ],
    'TestClosure02'    : [ 'TestClosure02.java' ],
    'TestNumbers01'    : [ 'TestNumbers01.java' ],
    'TestProperties01' : [ 'A.java', 'TestProperties01.java' ],
    'TestReturns01'    : [ 'TestReturns01.java' ], 
    'TestReturns02'    : [ 'TestReturns02.java' ],
    'TestStrings01'    : [ 'TestStrings01.java' ], 
    'TestDependencies01' : [ 'A.java', 'B.java', 'C.java', 'D.java', 'TestDependencies01.java' ], 
    'TestDependencies02' : [ 'A.java', 'B.java', 'C.java', 'D.java', 'TestDependencies02.java' ],  
    'TestDependencies03' : [ 'A.java', 'B.java', 'C.java', 'D.java', 'TestDependencies03.java' ],  
    'TestMethods01'      : [ 'A.java', 'TestMethods01.java' ],  
    'TestTypes'        : [ 'TestTypes.java' ], 
    'TestTypes01'      : [ 'TestTypes01.java' ], 
    'TestTypes02'      : [ 'TestTypes02.java' ], 
    'TestTypes03'      : [ 'TestTypes03.java' ], 
    'TestTypes04'      : [ 'TestTypes04.java' ], 
    'TestTypes05'      : [ 'TestTypes05.java' ], 
    'TestConstants01'  : [ 'C.java', 'TestConstants01.java' ],  
    'TestStatic01'     : [ 'C.java', 'TestStatic01.java' ],  
    'TestStatic02'     : [ 'C.java', 'TestStatic02.java' ],  
    'TestNonBinary'    : [ 'TestNonBinary.java' ],
    'TestDynamicReturns' : [ 'TestDynamicReturns.java' ],
    'TestPolymorphism01' : [ 'A.java', 'B.java', 'C.java', 'D.java', 'TestPolymorphism01.java' ],
    'TestPolymorphism02' : [ 'A.java', 'B.java', 'C.java', 'D.java', 'TestPolymorphism02.java' ],
  ];

  def fileset2c = [
    'TestClosure01'    : [ 'TestClosure01.h', 'TestClosure01.m' ], 
    'TestClosure02'    : [ 'TestClosure02.h', 'TestClosure02.m' ], 
    'TestNumbers01'    : [ 'TestNumbers01.h', 'TestNumbers01.m' ],
    'TestProperties01' : [ 'A.h', 'A.m', 'TestProperties01.h', 'TestProperties01.m' ],
    'TestReturns01'    : [ 'TestReturns01.h', 'TestReturns01.m' ],  
    'TestReturns02'    : [ 'TestReturns02.h', 'TestReturns02.m' ],
    'TestStrings01'    : [ 'TestStrings01.h', 'TestStrings01.m' ],  
    'TestDependencies01' : [ 'A.h', 'A.m', 'B.h', 'B.m', 'C.h', 'C.m', 'D.h', 'D.m', 
			     'TestDependencies01.h', 'TestDependencies01.m' ], 
    'TestDependencies02' : [ 'A.h', 'A.m', 'B.h', 'B.m', 'C.h', 'C.m', 'D.h', 'D.m', 
			     'TestDependencies02.h', 'TestDependencies02.m' ],  
    'TestDependencies03' : [ 'A.h', 'A.m', 'B.h', 'B.m', 'C.h', 'C.m', 'D.h', 'D.m', 
			     'TestDependencies03.h', 'TestDependencies03.m' ],  
    'TestMethods01'      : [ 'A.h', 'A.m', 'TestMethods01.h', 'TestMethods01.m' ],  
    'TestTypes'        : [ 'TestTypes.h', 'TestTypes.m' ], 
    'TestTypes01'      : [ 'TestTypes01.h', 'TestTypes01.m' ], 
    'TestTypes02'      : [ 'TestTypes02.h', 'TestTypes02.m' ], 
    'TestTypes03'      : [ 'TestTypes03.h', 'TestTypes03.m' ], 
    'TestTypes04'      : [ 'TestTypes04.h', 'TestTypes04.m' ], 
    'TestTypes05'      : [ 'TestTypes05.h', 'TestTypes05.m' ],
    'TestConstants01'  : [ 'C.h', 'C.m', 'TestConstants01.h', 'TestConstants01.m' ],  
    'TestStatic01'     : [ 'C.h', 'C.m', 'TestStatic01.h', 'TestStatic01.m' ],   
    'TestStatic02'     : [ 'C.h', 'C.m', 'TestStatic02.h', 'TestStatic02.m' ],   
    'TestNonBinary'    : [ 'TestNonBinary.h', 'TestNonBinary.m' ],
    'TestDynamicReturns' : [ 'TestDynamicReturns.h', 'TestDynamicReturns.m' ],
    'TestPolymorphism01' : [ 'A.h', 'A.m', 'B.h', 'B.m', 'C.h', 'C.m', 'D.h', 'D.m', 
			     'TestPolymorphism01.h', 'TestPolymorphism01.m' ],
    'TestPolymorphism02' : [ 'A.h', 'A.m', 'B.h', 'B.m', 'C.h', 'C.m', 'D.h', 'D.m', 
			     'TestPolymorphism02.h', 'TestPolymorphism02.m' ],
  ];

  def fileset2g = [
    'TestClosure01'    : [ 'TestClosure01.groovy' ],
    'TestClosure02'    : [ 'TestClosure02.groovy' ],
    'TestNumbers01'    : [ 'TestNumbers01.groovy' ],
    'TestProperties01' : [ 'TestProperties01.groovy' ],
    'TestReturns01'    : [ 'TestReturns01.groovy' ], 
    'TestReturns02'    : [ 'TestReturns02.groovy' ],
    'TestStrings01'    : [ 'TestStrings01.groovy' ], 
    'TestDependencies01' : [ 'TestDependencies01.groovy' ], 
    'TestDependencies02' : [ 'TestDependencies02.groovy' ],  
    'TestDependencies03' : [ 'TestDependencies03.groovy' ],  
    'TestMethods01'      : [ 'TestMethods01.groovy' ],  
    'TestTypes'        : [ 'TestTypes.groovy' ], 
    'TestTypes01'      : [ 'TestTypes01.groovy' ], 
    'TestTypes02'      : [ 'TestTypes02.groovy' ], 
    'TestTypes03'      : [ 'TestTypes03.groovy' ], 
    'TestTypes04'      : [ 'TestTypes04.groovy' ], 
    'TestTypes05'      : [ 'TestTypes05.groovy' ], 
    'TestConstants01'  : [ 'TestConstants01.groovy' ],  
    'TestStatic01'     : [ 'TestStatic01.groovy' ],  
    'TestStatic02'     : [ 'TestStatic02.groovy' ],  
    'TestNonBinary'    : [ 'TestNonBinary.groovy' ],
    'TestDynamicReturns' : [ 'TestDynamicReturns.groovy' ],
    'TestPolymorphism01' : [ 'TestPolymorphism01.groovy' ],
    'TestPolymorphism02' : [ 'TestPolymorphism02.groovy' ],
  ];

  void testCaseJava(file) { 
    testCase(dir2, file, [ 'build.xml' ] + fileset2j[file], 'Java')
  }

  void testCaseObjc(file) { 
    testCase(dir2, file, [ 'makefile' ] + fileset2c[file], 'Objc')
  }

  void testCaseGroovy(file) { 
    testCase(dir2, file, [ 'build.xml' ] + fileset2g[file], 'Groovy')
  }

  void testCaseRaw(file) { 
    testCase(dir2, file, [ 'build.xml' ] + fileset2g[file], 'Raw')
  }

  /// Java
  
  @Test(timeout=10000L) 
  public void test1001() {
    testCaseJava('TestClosure01')
  }

  @Test(timeout=10000L) 
  public void test1002() {
    testCaseJava('TestNumbers01')
  }

  @Test(timeout=10000L) 
  public void test1003() {
    testCaseJava('TestProperties01')
  }

  @Test(timeout=10000L) 
  public void test1004() {
    testCaseJava('TestReturns01')
  }

  @Test(timeout=10000L) 
  public void test1005() {
    testCaseJava('TestReturns02')
  }

  @Test(timeout=10000L) 
  public void test1006() {
    testCaseJava('TestStrings01')
  }

  @Test(timeout=10000L) 
  public void test1007() {
    testCaseJava('TestDependencies01')
  }

  @Test(timeout=10000L) 
  public void test1008() {
    testCaseJava('TestDependencies02')
  }

  @Test(timeout=10000L) 
  public void test1009() {
    testCaseJava('TestMethods01')
  }

  @Test(timeout=10000L) 
  public void test1010() {
    testCaseJava('TestTypes')
  }

  @Test(timeout=10000L) 
  public void test1011() {
    testCaseJava('TestClosure02')
  }

  @Test(timeout=10000L) 
  public void test1012() {
    testCaseJava('TestDependencies03')
  }

  @Test(timeout=10000L) 
  public void test1013() {
    testCaseJava('TestTypes01')
  }

  @Test(timeout=10000L) 
  public void test1014() {
    testCaseJava('TestTypes02')
  }

  @Test(timeout=10000L) 
  public void test1015() {
    testCaseJava('TestTypes03')
  }
  
  @Test(timeout=10000L) 
  public void test1016() {
    testCaseJava('TestTypes04')
  }
  
  @Test(timeout=10000L) 
  public void test1017() {
    testCaseJava('TestTypes05')
  }

  @Test(timeout=10000L) 
  public void test1018() {
    testCaseJava('TestConstants01')
  }

  @Test(timeout=10000L) 
  public void test1019() {
    testCaseJava('TestStatic01')
  }

  @Test(timeout=10000L) 
  public void test1020() {
    testCaseJava('TestStatic02')
  }

  @Test(timeout=10000L) 
  public void test1021() {
    testCaseJava('TestNonBinary')
  }

  @Test(timeout=10000L) 
  public void test1022() {
    testCaseJava('TestDynamicReturns')
  }

  @Test(timeout=10000L) 
  public void test1023() {
    testCaseJava('TestPolymorphism01')
  }

  @Test(timeout=10000L) 
  public void test1024() {
    testCaseJava('TestPolymorphism02')
  }

 
  /// Obj-C
  
  @Test(timeout=10000L) 
  public void test1001c() {
    testCaseObjc('TestClosure01')
  }

  @Test(timeout=10000L) 
  public void test1002c() {
    testCaseObjc('TestNumbers01')
  }

  @Test(timeout=10000L) 
  public void test1003c() {
    testCaseObjc('TestProperties01')
  }

  @Test(timeout=10000L) 
  public void test1004c() {
    testCaseObjc('TestReturns01')
  }

  @Test(timeout=10000L) 
  public void test1005c() {
    testCaseObjc('TestReturns02')
  }

  @Test(timeout=10000L) 
  public void test1006c() {
    testCaseObjc('TestStrings01')
  }

  @Test(timeout=10000L) 
  public void test1007c() {
    testCaseObjc('TestDependencies01')
  }

  @Test(timeout=10000L) 
  public void test1008c() {
    testCaseObjc('TestDependencies02')
  }

  @Test(timeout=10000L) 
  public void test1009c() {
    testCaseObjc('TestMethods01')
  }

  @Test(timeout=10000L) 
  public void test1010c() {
    testCaseObjc('TestTypes')
  }

  @Test(timeout=10000L) 
  public void test1011c() {
    testCaseObjc('TestClosure02')
  }

  @Test(timeout=10000L) 
  public void test1012c() {
    testCaseObjc('TestDependencies03')
  }

  @Test(timeout=10000L) 
  public void test1013c() {
    testCaseObjc('TestTypes01')
  }

  @Test(timeout=10000L) 
  public void test1014c() {
    testCaseObjc('TestTypes02')
  }

  @Test(timeout=10000L) 
  public void test1015c() {
    testCaseObjc('TestTypes03')
  }

  @Test(timeout=10000L) 
  public void test1016c() {
    testCaseObjc('TestTypes04')
  }

  @Test(timeout=10000L) 
  public void test1017c() {
    testCaseObjc('TestTypes05')
  }

  @Test(timeout=10000L) 
  public void test1018c() {
    testCaseObjc('TestConstants01')
  }

  @Test(timeout=10000L) 
  public void test1019c() {
    testCaseObjc('TestStatic01')
  }

  @Test(timeout=10000L) 
  public void test1020c() {
    testCaseObjc('TestStatic02')
  }

  @Test(timeout=10000L) 
  public void test1021c() {
    testCaseObjc('TestNonBinary')
  }

  @Test(timeout=10000L) 
  public void test1022c() {
    testCaseObjc('TestDynamicReturns')
  }

  @Test(timeout=10000L) 
  public void test1023c() {
    testCaseObjc('TestPolymorphism01')
  }

  @Test(timeout=10000L) 
  public void test1024c() {
    testCaseObjc('TestPolymorphism02')
  }

 

  /// Groovy

  @Test(timeout=10000L) 
  public void test1001g() {
    testCaseGroovy('TestClosure01')
  }

  @Test(timeout=10000L) 
  public void test1002g() {
    testCaseGroovy('TestNumbers01')
  }

  @Test(timeout=10000L) 
  public void test1003g() {
    testCaseGroovy('TestProperties01')
  }

  @Test(timeout=10000L) 
  public void test1004g() {
    testCaseGroovy('TestReturns01')
  }

  @Test(timeout=10000L) 
  public void test1005g() {
    testCaseGroovy('TestReturns02')
  }

  @Test(timeout=10000L) 
  public void test1006g() {
    testCaseGroovy('TestStrings01')
  }

  @Test(timeout=10000L) 
  public void test1007g() {
    testCaseGroovy('TestDependencies01')
  }

  @Test(timeout=10000L) 
  public void test1008g() {
    testCaseGroovy('TestDependencies02')
  }

  @Test(timeout=10000L) 
  public void test1009g() {
    testCaseGroovy('TestMethods01')
  }

  @Test(timeout=10000L) 
  public void test1010g() {
    testCaseGroovy('TestTypes')
  }

  @Test(timeout=10000L) 
  public void test1011g() {
    testCaseGroovy('TestClosure02')
  }

  @Test(timeout=10000L) 
  public void test1012g() {
    testCaseGroovy('TestDependencies03')
  }

  @Test(timeout=10000L) 
  public void test1013g() {
    testCaseGroovy('TestTypes01')
  }

  @Test(timeout=10000L) 
  public void test1014g() {
    testCaseGroovy('TestTypes02')
  }

  @Test(timeout=10000L) 
  public void test1015g() {
    testCaseGroovy('TestTypes03')
  }

  @Test(timeout=10000L) 
  public void test1016g() {
    testCaseGroovy('TestTypes04')
  }

  @Test(timeout=10000L) 
  public void test1017g() {
    testCaseGroovy('TestTypes05')
  }

  @Test(timeout=10000L) 
  public void test1018g() {
    testCaseGroovy('TestConstants01')
  }

  @Test(timeout=10000L) 
  public void test1019g() {
    testCaseGroovy('TestStatic01')
  }

  @Test(timeout=10000L) 
  public void test1020g() {
    testCaseGroovy('TestStatic02')
  }

  @Test(timeout=10000L) 
  public void test1021g() {
    testCaseGroovy('TestNonBinary')
  }

  @Test(timeout=10000L) 
  public void test1022g() {
    testCaseGroovy('TestDynamicReturns')
  }

  @Test(timeout=10000L) 
  public void test1023g() {
    testCaseGroovy('TestPolymorphism01')
  }

  @Test(timeout=10000L) 
  public void test1024g() {
    testCaseGroovy('TestPolymorphism02')
  }

 

  /// Raw 

  @Test(timeout=10000L) 
  public void test1001r() {
    testCaseRaw('TestClosure01')
  }

  @Test(timeout=10000L) 
  public void test1002r() {
    testCaseRaw('TestNumbers01')
  }

  @Test(timeout=10000L) 
  public void test1003r() {
    testCaseRaw('TestProperties01')
  }

  @Test(timeout=10000L) 
  public void test1004r() {
    testCaseRaw('TestReturns01')
  }

  @Test(timeout=10000L) 
  public void test1005r() {
    testCaseRaw('TestReturns02')
  }

  @Test(timeout=10000L) 
  public void test1006r() {
    testCaseRaw('TestStrings01')
  }

  @Test(timeout=10000L) 
  public void test1007r() {
    testCaseRaw('TestDependencies01')
  }

  @Test(timeout=10000L) 
  public void test1008r() {
    testCaseRaw('TestDependencies02')
  }

  @Test(timeout=10000L) 
  public void test1009r() {
    testCaseRaw('TestMethods01')
  }

  @Test(timeout=10000L) 
  public void test1010r() {
    testCaseRaw('TestTypes')
  }

  @Test(timeout=10000L) 
  public void test1011r() {
    testCaseRaw('TestClosure02')
  }

  @Test(timeout=10000L) 
  public void test1012r() {
    testCaseRaw('TestDependencies03')
  }

  @Test(timeout=10000L) 
  public void test1013r() {
    testCaseRaw('TestTypes01')
  }

  @Test(timeout=10000L) 
  public void test1014r() {
    testCaseRaw('TestTypes02')
  }

  @Test(timeout=10000L) 
  public void test1015r() {
    testCaseRaw('TestTypes03')
  }

  @Test(timeout=10000L) 
  public void test1016r() {
    testCaseRaw('TestTypes04')
  }

  @Test(timeout=10000L) 
  public void test1017r() {
    testCaseRaw('TestTypes05')
  }

  @Test(timeout=10000L) 
  public void test1018r() {
    testCaseRaw('TestConstants01')
  }

  @Test(timeout=10000L) 
  public void test1019r() {
    testCaseRaw('TestStatic01')
  }

  @Test(timeout=10000L) 
  public void test1020r() {
    testCaseRaw('TestStatic02')
  }

  @Test(timeout=10000L) 
  public void test1021r() {
    testCaseRaw('TestNonBinary')
  }

  @Test(timeout=10000L) 
  public void test1022r() {
    testCaseRaw('TestDynamicReturns')
  }

  @Test(timeout=10000L) 
  public void test1023r() {
    testCaseRaw('TestPolymorphism01')
  }

  @Test(timeout=10000L) 
  public void test1024r() {
    testCaseRaw('TestPolymorphism02')
  }

 



  // invoke the translator directly 
  void testCase(dir, infile, outfiles, target) { 
    println "\n\nTest Case (${target}): ${infile} -> ${outfiles}"
    try { 
      def args = [ "-target=${target}", "-header=no", "${dir}/${infile}.groovy" ] as String[] 
      Main.main(args)
    } catch (Exception e) { 
      e.printStackTrace()
    }
    def refdir = "${dir}/ref/${infile}"
    refdir += ('-' + target.toLowerCase())
    /*
    if (target.toLowerCase() == 'objc') { 
      refdir += '-objc'
    }
    */

    boolean pass = true
    outfiles.each { file -> 
      println "Compare file ${file}"
      //assertTrue("Test Case (${target}): ${infile}; Compare ${file}", 
      pass &= diff("${dir}/out", refdir, file)
    }
    assertTrue("Test Case (${target})", pass)
  }

  // run the translator in an external process 
  void testCaseExec(dir, infile, outfiles, target) { 
    println "\n\nTest Case (${target}): ${infile} -> ${outfiles}"
    try { 
      def p = "bin/gt -target=${target} -header=no ${dir}/${infile}.groovy".execute()
      //p.waitFor()
      p.text
    } catch (Exception e) { 
      e.printStackTrace()
    }
    def refdir = "${dir}/ref/${infile}"
    if (target.toLowerCase() == 'objc') { 
      refdir += '-objc'
    }

    outfiles.each { file -> 
      println "Compare file ${file}"
      assertTrue("Test Case (${target}): ${infile}; Compare ${file}", diff("${dir}/out", refdir, file))
    }
  }

  // run the translator in an external process, Java 
  void testCaseJava(dir, infile, outfiles) { 
    println "\n\nTest Case: ${infile} -> ${outfiles}"
    try { 
      def p = "bin/gt -header=no ${dir}/${infile}.groovy".execute()
      //p.waitFor()
      p.text
    } catch (Exception e) { 
      e.printStackTrace()
    }
    outfiles.each { file -> 
      println "Compare file ${file}"
      assertTrue("Test Case: ${infile}; Compare ${file}", 
		 diff("${dir}/out", "${dir}/ref/${infile}", file))
    }
  }

  // run the translator in an external process, Obj-C
  void testCaseObjc(dir, infile, outfiles) { 
    println "\n\nTest Case (Obj-C): ${infile} -> ${outfiles}"
    def p = "bin/gt -header=no -target=objc ${dir}/${infile}.groovy".execute()
    //p.waitFor()
    p.text
    //println p.text

    outfiles.each { file -> 
      println "Compare file ${file}"      
      assertTrue("Test Case (Obj-C): ${infile}; Compare ${file}", 
		 diff("${dir}/out", "${dir}/ref/${infile}-objc", file))
    }
  }

  /* return true if the files are the same*/ 
  static boolean diff(dir1, dir2, file) { 
    def cmd = "diff ${dir1}/${file} ${dir2}/${file}"
    def p = cmd.execute()
    p.waitFor()
    if (p.exitValue() != 0) 
      println p.text
    return p.exitValue() == 0  
  }

}