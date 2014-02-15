package xj.mobile.test

import org.junit.*
import static org.junit.Assert.*

import xj.mobile.Main
import static xj.mobile.model.impl.ViewControllerClass.*
import static xj.mobile.test.TestUtils.diff

class AppBuilderTest {  

  // test for platforms

  static USER_CONFIG = 'test/org.properties'

  static USER_CONFIG_IOS = 'test/org-ios.properties'
  static USER_CONFIG_IOS_AUTOLAYOUT = 'test/org-autolayout.properties'
  static USER_CONFIG_IOS_FORMAT1 = 'test/org-ios-format1.properties'

  static USER_CONFIG_ANDROID = 'test/org-android.properties'
  static USER_CONFIG_ANDROID_NOSUPPORT = 'test/org-android-nosupport.properties'
  static USER_CONFIG_ANDROID_FORMAT1 = 'test/org-android-format1.properties'

  void test_iOS(String name, param = null) { 
    println "\n\nTest Case iOS: ${name}" 
    test_iOS_Android(name, true, false, param)
  }

  void test_iOS_AutoLayout(String name) { 
    println "\n\nTest Case iOS (AutoLayout): ${name}" 
    test_iOS_Android(name, true, false, [ iosAutoLayout: true ])
  }

  void test_Android(String name, param = null) {  
    println "\n\nTest Case Android: ${name}" 
    test_iOS_Android(name, false, true, param)
  }

  void test_Android_NoSupport(String name) {  
    println "\n\nTest Case Android (NoSupport): ${name}" 
    test_iOS_Android(name, false, true, [ androidSupport: false ])
  }

  void test_iOS_Android(String fname, 
						boolean ios = true,
						boolean android = true,
						param = null) { 
	println "AppBuilderTest.test_iOS_Android() ${fname} param=${param}"

	if (ios || android) { 
	  String name = fname
	  int i = fname.lastIndexOf('/')
	  if (i > 0) { 
		name = fname[i+1 .. -1]
	  }
	  //println "test_iOS_Android ${fname} ${name}"

	  String appname, appid

	  def filemap = [:]
	  if (ios) { 
		// iOS files
		appname = iOSFileMap[name].name
		appid = appname.replaceAll('[ \\t]', '')
		def views = iOSFileMap[name].views
		//String srcdir = "${name}/${appname.replaceAll('\\+', ' ')}" 
		String srcdir = "${name}/${appname}" 
		def iosfiles = [ "${srcdir}/AppDelegate.h", "${srcdir}/AppDelegate.m",
						 "${srcdir}/${appname}-Info.plist", "${srcdir}/${appname}-Prefix.pch", 
						 "${srcdir}/en.lproj/InfoPlist.strings", "${srcdir}/main.m" ]
		views.each { v -> 
		  iosfiles << "${srcdir}/${getViewControllerName(v)}.h"
		  iosfiles << "${srcdir}/${getViewControllerName(v)}.m"
		}

		def custom = iOSFileMap[name].custom
		custom?.each { v -> 
		  iosfiles << "${srcdir}/${v}.h"
		  iosfiles << "${srcdir}/${v}.m"
		}

		def icon = iOSFileMap[name].icon ?: 'icon'
		iosfiles << "${srcdir}/${icon}.png"
		iosfiles << "${srcdir}/${icon}@2x.png"
		filemap['iOS'] = iosfiles

		//if (param == null) param = iOSFileMap[name].param 
	  }

	  if (android) { 
		// Android files
		appname = androidFileMap[name].name
		appid = appname.replaceAll('[ \\t]', '')
		def views = androidFileMap[name].views
		def layouts = androidFileMap[name].layouts
		def androidfiles = [ 
		  "${name}/AndroidManifest.xml",
		  "${name}/res/values/strings.xml",
		]
		layouts.each { l -> 
		  androidfiles << "${name}/res/layout/${l}.xml"
		}
		views.each { v -> 
		  androidfiles << "${name}/src/com/madl/${name.toLowerCase()}/${v}.java"
		}
		def custom = androidFileMap[name].custom
		custom?.each { v -> 
		  androidfiles << "${name}/src/com/madl/${name.toLowerCase()}/${v}.java"
		}
		filemap['Android'] = androidfiles

		//if (param == null) param = androidFileMap[name].param 
	  }


	  boolean iosAutoLayout = false;
	  boolean androidSupport = true;
	  boolean format1 = false
	  if (param) { 
		if (param.containsKey('androidSupport')) androidSupport = param['androidSupport']
		if (param.containsKey('iosAutoLayout')) iosAutoLayout = param['iosAutoLayout']
		//if (param.containsKey('format1')) 
		format1 = param['format1']
	  }

	  String userConfig = USER_CONFIG
	  String outdir = null
	  if (ios && !android) { 
		if (iosAutoLayout) { 
		  userConfig = USER_CONFIG_IOS_AUTOLAYOUT
		  outdir = 'AutoLayout'
		} else if (format1) { 
		  userConfig = USER_CONFIG_IOS_FORMAT1
		} else { 
		  userConfig = USER_CONFIG_IOS
		}
	  } else if (!ios && android) { 
		if (!androidSupport) {
		  userConfig = USER_CONFIG_ANDROID_NOSUPPORT
		} else if (format1) { 
		  userConfig = USER_CONFIG_ANDROID_FORMAT1
		} else {  
		  userConfig = USER_CONFIG_ANDROID
		}
	  }
	  testCase(fname, filemap, userConfig, outdir) 
	}
  }

  // invoke the app builder directly (in the same JVM for all involcation)  
  void testCase(infile, fileMap, userConfig, dir = null) { 
    println "Test Case args: ${infile} ${userConfig} ${dir}" 
    /*
	  try { 
      def args = [ "-nodate", "test/${infile}.madl", "test/org.properties" ] as String[]
      Main.main(args)
	  } catch (Exception e) { 
      e.printStackTrace()
      fail()  // fail if there is an exception 
	  }
    */
    String[] args 
	if (dir) { 
	  args = ([ '-nodate', '-d', dir, "test/${infile}.madl", userConfig ]) as String[]
	} else { 
	  args = ([ '-nodate', "test/${infile}.madl", userConfig ]) as String[]
	}
    Main.main(args)  // error if there is an exception
	assertTrue("Test Case (${infile})", Main.success)

    boolean pass = true
    fileMap.each { target, files ->
      def outdir = "gen/Platform.${target}"
      def refdir = "gen/Platform.${target}-Ref"
	  if (dir) { 
		outdir = "gen/Platform.${target}/${dir}"
		refdir = "gen/Platform.${target}-${dir}-Ref"
	  }

	  println "Test Case compare files: outdir=${outdir}  refdir=${refdir}"

      files.each { file -> 
		def okay = diff(outdir, refdir, file)
		def result = okay ? 'Same' : '!!! Different'
		println "Compare file ${file}: ${result}"
		pass &= okay
      }
    }
    assertTrue("Test Case (${infile})", pass)
  }

  // run the app builder in an external process (separate JVM for each invocation) 
  void testCaseExec(dir, infile, outfiles, target) { 
    println "\n\nTest Case (${target}): ${infile} -> ${outfiles}"
    try { 
      def p = "bin/appbuilder -nodate test/${infile}.madl ${USER_CONFIG}".execute()
      //p.waitFor()
      p.text
    } catch (Exception e) { 
      e.printStackTrace()
    }

    def outdir = "gen/Platform.${target}"
    def refdir = "gen/Platform.${target}-Ref"

    boolean pass = true
    outfiles.each { file -> 
      println "Compare file ${file}"
      pass &= diff(outdir, refdir, file)
    }
    assertTrue("Test Case (${target})", pass)
  }

}