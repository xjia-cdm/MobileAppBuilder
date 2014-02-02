
package xj.translate

import org.codehaus.groovy.ast.*

class ImportResolver { 

  List<String> defaultImports = [ 
    "java.lang.", "java.io.", "java.net.", "java.util.", 
    "groovy.lang.", "groovy.util."
  ]

  List<ModuleNode> modulesInPackage = []
  ModuleNode moduleNode
  ClassNode classNode

  List<String> importList, staticImportList;

  ImportResolver(ModuleNode m, ClassNode c) { 
    moduleNode = m
    classNode = c

  }
  
  /**
   * resolve the class name 
   * return the fully qualified class name (binary name) 
   * @param name      a class name
   */
  String resolveClassName(String name) { 
    if (name) { 

      // check class name
      if (name == classNode?.nameWithoutPackage) { 
	return classNode.name
      }

      // type name within the current class 
      def fname = hasInnerClass(name, classNode)
      if (fname) return fname

      // type name within the current module 
      fname = hasClassInModule(name, moduleNode)
      if (fname) return fname
      
      modulesInPackage?.each { m -> 
	// type name within the current package
	fname = hasClassInModule(name, m)
	if (fname) return fname
      }

      // check defaul import
      for (String imp : defaultImports) { 
	fname = imp + name
	if (hasSystemClass(fname))
	  return fname 	
      }

      // type name in import list 
      if (moduleNode?.imports) {
	for (String imp : moduleNode.imports) { 
	  if (imp.className.endsWith("." + name)) {
	    return imp.className 
	  }
	}	
      }

      moduleNode?.starImports?.each { i -> 
	//importList << (i.packageName + "*");
	for (String imp : moduleNode.starImports) { 
	  if (imp.className.startsWith("java.") ||
	      imp.className.startsWith("javax.") ||
	      imp.className.startsWith("groovy.")) { 
	    fname = i.packageName + "." + name
	    if (hasSystemClass(fname)) 
	      return fname
	  } else { 



	  }
	}	
      }

      moduleNode?.staticImports?.each { i -> 

      }
      moduleNode?.staticStarImports?.each { i -> 

      }

    }
    return null;
  }

  static String hasInnerClass(String name, ClassNode c) {  
    if (name && c) { 
      for (ClassNode ic : c.innerClasses) { 
	//println "  innerClass: ${ic.nameWithoutPackage}"
	def iname = c.nameWithoutPackage + '$' + name
	if (iname == ic.nameWithoutPackage) { 
	  return ic.name
	}
      }
    }
    return null 
  }

  static String hasClassInModule(String name, ModuleNode m) { 
    if (name && m) { 
      List<ClassNode> clist = m.classes
      if (clist) { 
	for (ClassNode c : clist) { 
	  //println "  classInModule: ${c.nameWithoutPackage}"
	  if (name == c.nameWithoutPackage) { 
	    return c.name
	  }
	}
      }
    }
    return null
  }

  /* name : binary name of a class */
  static boolean hasSystemClass(String name, ClassLoader classLoader = null) { 
    try { 
      if (!classLoader) { 
	classLoader = ClassLoader.getSystemClassLoader()
      }
      Class.forName(name, false, classLoader);
      return true; 
    } catch (ClassNotFoundException e) {
    } catch (LinkageError e) {
    }
    return false;
  }

  /*
   * check if a user defined class exists
   * only checks if a filename based on the package and class name exits 
   */
  static boolean hasUserClass(String pkgName, String className, String srcPath = null) { 
    String pkgPath = pkgName.replaceAll('\\.', File.separator); 
    String filePath = pkgPath; 
    if (srcPath) { 
      filePath = srcPath + File.separator + pkgPath;
    }
    filePath += (File.separator + className + '.groovy');
    File f = new File(filePath);
    if (f.exists()) { 
      return pkgName + '.' + className
    }
    return null
  }

}