
package xj.mobile.test

import xj.mobile.lang.*

class AppBuilderTestInfo { 

  static void main(args) { 
    println "Test App Builder"
   
	println '=== Test 1 iOS ==='
	println new AppBuilderTest1().iOSFileMap.keySet().sort().join(' ') 

	println '=== Test 2 iOS ==='
	println new AppBuilderTest2().iOSFileMap.keySet().sort().join(' ') 

	println '=== Test 3 iOS ==='
	println new AppBuilderTest3().iOSFileMap.keySet().sort().join(' ') 

	println '=== Test 4 iOS ==='
	println new AppBuilderTest4().iOSFileMap.keySet().sort().join(' ') 

	println '=== Test 5 iOS ==='
	println new AppBuilderTest5().iOSFileMap.keySet().sort().join(' ') 

	println '=== Test 6 iOS ==='
	println new AppBuilderTest6().iOSFileMap.keySet().sort().join(' ') 

	println '=== Test 7 iOS ==='
	println new AppBuilderTest7().iOSFileMap.keySet().sort().join(' ') 

	//println '=== Test D1 Android ==='
	//println new AppBuilderTestD1().androidFileMap.keySet().sort().join(' ') 

	println '=== Test E1 iOS ==='
	println new AppBuilderTestE1().iOSFileMap.keySet().sort().join(' ') 

	println '=== Test T1 iOS ==='
	println new AppBuilderTestT1().iOSFileMap.keySet().sort().join(' ') 

	//println '=== Test TD Android ==='
	//println new AppBuilderTestTD().androidFileMap.keySet().sort().join(' ') 

	println '=== Test PS1 iOS ==='
	println new AppBuilderTestPS1().iOSFileMap.keySet().sort().join(' ') 




  }

}