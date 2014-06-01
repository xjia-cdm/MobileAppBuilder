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
    'ImplicitActions' : [ name: 'Implicit Actions', 
				   views: [ 'View1' ] ], 

	'Form'     : [ name: 'Form', 
				   views: [ 'View1' ] ], 
	'FormAction' : [ name: 'Form Action', 
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

	'WorldCities' : [ name: 'World Cities',
					  views: [ 'Top', 'ListView1' ] ],
	'EuropeanCountries' : [ name: 'European Countries',
					  views: [ 'Top', 'ListView1' ] ],
	'EuropeanUnion' : [ name: 'European Union',
						 views: [ 'Top', 'ListView1', 'Country' ] ],

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
    'ImplicitActions' : [ name: 'Implicit Actions', 
				   views: [ 'ImplicitActions' ], 
				   layouts: [ 'main' ] ], 

	'Form'     : [ name: 'Form', 
				   views: [ 'Form' ],
				   layouts: [ 'main' ] ], 
	'FormAction': [ name: 'Form Action', 
				   views: [ 'FormAction' ],
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

	'WorldCities' : [ name: 'World Cities',
					  views: [ 'WorldCities' ],
					  layouts: [ 'main', 'list_item_listview1' ] ],
	'EuropeanCountries' : [ name: 'European Countries',
							 views: [ 'EuropeanCountries' ],
							 layouts: [ 'main', 'list_item_listview1', 'list_header_listview1' ] ],
	'EuropeanUnion' : [ name: 'European Union',
						 views: [ 'EuropeanUnion', 'Country' ],
						 layouts: [ 'main', 'list_item_listview1', 'country' ] ],

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