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
class AppBuilderTestTD extends AppBuilderTest {  

  static androidFileMap = [
    'Widgets'  : [ name: 'Widgets', 
				   views: [ 'Widgets' ], 
				   layouts: [ 'main' ],
				   param: [ design: 'radiogroup' ] ], 

	'EuropeanCountries' : [ name: 'European Countries',
							views: [ 'EuropeanCountries' ],
							layouts: [ 'main', 'list_item_listview1', 'list_header_listview1' ],
							param: [ design: 'expandable' ] ], 
  ]

  String testName

  public AppBuilderTestTD(String testName) { 
	this.testName = testName
  }

  @Parameters
  public static Collection data() {
	androidFileMap.keySet().collect { [ it ] as String[] }
  }							 

  @Test(timeout=80000L)
  public void testAndroid() {
	println "testAndroid ${testName}"
    test_Android('Tutorials/' + testName, androidFileMap[testName].param)
  }

}