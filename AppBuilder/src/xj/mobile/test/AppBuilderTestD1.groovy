package xj.mobile.test

import org.junit.*
import static org.junit.Assert.*

import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collection;

import xj.mobile.Main

@RunWith(Parameterized.class)
class AppBuilderTestD1 extends AppBuilderTest {  

  static androidFileMap = [
    app02  : [ name: 'App Button', 
				 views: [ 'AppButton' ], 
				 layouts: [ 'main' ],
				 param: [ design: 'radiogroup' ] ], 
    app07  : [ name: 'Countries', 
				 views: [ 'Countries' ], 
				 layouts: [ 'main', 'list_header_list1', 'list_item_list1' ],
				 param: [ design: 'expandable' ] ], 
  ]

  String testName

  public AppBuilderTestD1(String testName) { 
	this.testName = testName
  }

  @Parameters
  public static Collection data() {
	androidFileMap.keySet().collect { [ it ] as String[] }
  }							 

  @Test(timeout=80000L)
  public void testAndroid() {
	println "testAndroid ${testName}"
    test_Android(testName, androidFileMap[testName].param)
  }

}