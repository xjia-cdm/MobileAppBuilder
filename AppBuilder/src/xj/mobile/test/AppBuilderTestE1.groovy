package xj.mobile.test

import org.junit.*
import static org.junit.Assert.*

import xj.mobile.Main

class AppBuilderTestE1 extends AppBuilderTest {  

  def iOSFileMap = [
    'app_e01'  : [ name: 'App Test 01', 
				   views: [ 'Top', 'View1' ] ], 
  ];

  def androidFileMap = [
    'app_e01'  : [ name: 'App Test 01', 
				   views: [ 'AppTest01', 'View1' ], 
				   layouts: [ 'main', 'view1' ] ], 
  ];

  @Test(timeout=80000L)
  public void test_e01_iOS() {
    test_iOS('app_e01')
  }

  @Test(timeout=80000L)
  public void test_e01_Android() {
    test_Android('app_e01')
  }

}