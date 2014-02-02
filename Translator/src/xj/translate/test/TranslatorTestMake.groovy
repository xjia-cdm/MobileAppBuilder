package xj.translate.test

import org.junit.*
import static org.junit.Assert.*

class TranslatorTestMake { 

  def dir1 = 'test/groovy/trans/simple/ref/'

  @Test(timeout=10000L)
  public void test1() {
    testMakeJava(dir1 + 'Test1')
  }

  @Test(timeout=10000L)
  public void test2() {
    testMakeJava(dir1 + 'Test2')
  }

  @Test(timeout=10000L)
  public void test3() {
    testMakeJava(dir1 + 'Test3')
  }

  @Test(timeout=10000L)
  public void test4() {
    testMakeJava(dir1 + 'Test4')
  }

  @Test(timeout=10000L)
  public void test5() {
    testMakeJava(dir1 + 'Test5')
  }

  @Test(timeout=10000L)
  public void test6() {
    testMakeJava(dir1 + 'Test6')
  }

  @Test(timeout=10000L)
  public void test100() {
    testMakeJava(dir1 + 'Test100')
  }

  @Test(timeout=10000L)
  public void test140() {
    testMakeJava(dir1 + 'Test140')
  }

  void testMakeJava(dir) { 
    assertTrue("Test case - Make Jave: ${dir}", makeJava(dir))
  }

  void testMakeObjC(dir) { 
    assertTrue("Test case - Make Jave: ${dir}", makeObjC(dir))
  }

  static boolean makeJava(dir) { 
    println "Make Java ${dir}"
    /*
    def cmd = "pwd"
    def p = cmd.execute()
    println p.text
    cmd = "cd ${dir}"
    p = cmd.execute()
    p.waitFor()
    println p.text
    cmd = "pwd"
    p = cmd.execute()
    println p.text
    */
    def cmd = "pwd ; cd ${dir} ; pwd " //ant"
    def p = cmd.execute()
    p.waitFor()

    //if (p.exitValue() != 0) 
      println p.text
    return p.exitValue() == 0  
  }

  static boolean makeObjC(dir) { 
    def cmd = "cd ${dir} ; make"
    def p = cmd.execute()
    p.waitFor()
    if (p.exitValue() != 0) 
      println p.text
    return p.exitValue() == 0  
  }
 
} 