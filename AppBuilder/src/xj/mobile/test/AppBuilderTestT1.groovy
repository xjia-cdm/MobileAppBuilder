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
class AppBuilderTestT1 extends AppBuilderTest {  

  static iOSFileMap = [
    'Hello'    : [ name: 'Hello World', 
				   views: [ 'View1' ] ], 
    'Widgets'  : [ name: 'Widgets', 
				   views: [ 'View1' ] ], 
    'Actions'  : [ name: 'Actions', 
				   views: [ 'View1' ] ], 
    'Actions2' : [ name: 'Actions 2', 
				   views: [ 'View1' ] ], 

	'Table'    : [ name: 'Table', 
				   views: [ 'View1' ] ], 
	'Tabs'     : [ name: 'Tabs', 
				   views: [ 'TabbedView1', 'View1', 'View2', 'View3' ] ], 
	'Navigation' : [ name: 'Navigation',
					 views: [ 'Top', 'V1', 'V2', 'V3', 'V4', 'V5', 'V6', 'V7', 'V8' ] ],
	'Navigation2' : [ name: 'Navigation2',
					  views: [ 'V1', 'V2', 'V3', 'V4', 'V5', 'V6', 'V7', 'V8' ] ],
	'TipCalculator' : [ name: 'Tip Calculator', 
						views: [ 'View1' ],
						param: [ format1: true ] ], 
	'TipCalculator2' : [ name: 'Tip Calculator 2', 
						 views: [ 'View1' ],
						 param: [ format1: true ] ], 
	'TipCalculator3' : [ name: 'Tip Calculator 3', 
						 views: [ 'View1' ],
						 param: [ format1: true ] ], 
  ];

  static androidFileMap = [
    'Hello'    : [ name: 'Hello World', 
				   views: [ 'HelloWorld' ], 
				   layouts: [ 'main' ] ], 
    'Widgets'  : [ name: 'Widgets', 
				   views: [ 'Widgets' ], 
				   layouts: [ 'main' ] ], 
    'Actions'  : [ name: 'Actions', 
				   views: [ 'Actions' ], 
				   layouts: [ 'main' ] ], 
    'Actions2' : [ name: 'Actions 2', 
				   views: [ 'Actions2' ], 
				   layouts: [ 'main' ] ], 

	'Table'    : [ name: 'Table', 
				   views: [ 'Table' ],
				   layouts: [ 'main' ] ], 
	'Tabs'     : [ name: 'Tabs', 
				   views: [ 'Tabs', 'View1', 'View2', 'View3' ],
				   layouts: [ 'main', 'view1', 'view2', 'view3' ] ], 
	'Navigation' : [ name: 'Navigation',
					 views: [ 'Navigation', 'V2', 'V3', 'V4', 'V5', 'V6', 'V7', 'V8' ],
					 layouts: [ 'main', 'v2', 'v3', 'v4', 'v5', 'v6', 'v7', 'v8'  ] ],
	'Navigation2' : [ name: 'Navigation2',
					  views: [ 'Navigation2', 'V2', 'V3', 'V4', 'V5', 'V6', 'V7', 'V8' ],
					  layouts: [ 'main', 'v2', 'v3', 'v4', 'v5', 'v6', 'v7', 'v8' ] ],
	'TipCalculator' : [ name: 'Tip Calculator', 
						views: [ 'TipCalculator' ],
						layouts: [ 'main' ],
						param: [ format1: true ] ], 
	'TipCalculator2' : [ name: 'Tip Calculator 2', 
						 views: [ 'TipCalculator2' ],
						 layouts: [ 'main' ],
						 param: [ format1: true ] ], 
	'TipCalculator3' : [ name: 'Tip Calculator 3', 
						 views: [ 'TipCalculator3' ],
						 layouts: [ 'main' ],
						 param: [ format1: true ] ], 
  ];


  String testName

  public AppBuilderTestT1(String testName) { 
	this.testName = testName
  }

  @Parameters
  public static Collection data() {
	iOSFileMap.keySet().collect { [ it ] as String[] }
  }							 

  @Test(timeout=80000L)
  public void testiOS() {
	println "testiOS ${testName}"
    test_iOS('Tutorials/' + testName, iOSFileMap[testName].param)
  }

  @Test(timeout=80000L)
  public void testAndroid() {
	println "testAndroid ${testName}"
    test_Android('Tutorials/' + testName, androidFileMap[testName].param)
  }

}