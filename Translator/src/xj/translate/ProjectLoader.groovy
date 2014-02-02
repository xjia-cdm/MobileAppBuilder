package xj.translate

import java.io.*;
import java.util.*;

class ProjectLoader { 

  static final String FILE_EXT = '.groovy'

  Project project;

  def loadProject(String path) {
    println 'loadProject ' + path;

    project = new Project(path);
    if (path) { 
      File dir = new File(path); 
      String base = dir.getAbsolutePath();
      if (!base.endsWith(File.separator)) 
	base += File.separator;      
      if (dir.exists() &&
	  dir.isDirectory()) {  
	println 'Loading files in ' + dir; 

	loadFolder(path, project);    
      }
    }
  }

  void loadFolder(String path, Folder parent = null) { 
    if (path) { 
      File dir = new File(path); 
      if (dir.exists() &&
	  dir.isDirectory()) {  
	println('Loading pkg in ' + dir); 

	dir.listFiles()?.each { f -> 
	  if (f.name.charAt(0) != '.') { 
	    if (f.isDirectory()) { 
	      Folder pkg = new Folder(f.name);
	      parent?.addFolder(pkg);
	      loadFolder(f.absolutePath, pkg);
	    } else { 
	      if (f.name.endsWith(FILE_EXT)) { 
		parent?.items.add(new Item(name : f.name, file: f));
		if (processFile)
		  processFile(f.absolutePath)
	      }
	    }
	  }
	}
      }
    }
  }

  Closure processFile = null

} 

class Project extends Folder { 

  Project(String name = null) {
    super(name);
  }

}

class Folder { 

  String name;
  Folder parent;
  List<Folder> folders = [];
  List<Item> items = [];

  Folder(String name = null) {
    this.name = name
  }

  def addFolder(Folder fdl) { 
    if (fdl) { 
      folders.add(fdl);
      fdl.parent = this;
    }
  }

  String getFullname() { 
    parent && !(parent instanceof Project) ? (parent.fullname + "." + name) : name
  }
  
  def visit(Closure action) { 
    println "visit package " + fullname
    items.each { c -> action(this, c) } 
    folders.each { p -> p.visit(action) }
  }

}

class Item {
  String name; 
  File file; 
  
  String getFullPath() { 
    file?.absolutePath
  }
}
