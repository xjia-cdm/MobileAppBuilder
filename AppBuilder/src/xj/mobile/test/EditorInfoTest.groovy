package xj.mobile.test

import xj.mobile.EditorInfo
import xj.mobile.model.Application

class EditorInfoTest { 

  static void main(args) { 
    println "Test App Builder"
	
	for (f in [ 'app01', 'test01' ]) { 
	  println "============== Compile test/${f}.madl =============="
	  Application app = EditorInfo.compile("test/${f}.madl", 'test/org.properties')
	  if (app) { 
		println app.print()
	  } else { 
		EditorInfo.errors.printMessages()
	  }
	}
  }

}