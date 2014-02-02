
package xj.translate

import java.io.*
import org.codehaus.groovy.control.Phases

@Singleton
class Config {  
  
  enum EnumMode { Default, Full }

  String options = null;

  Language target = Language.Java; // default

  def phase; 

  //EnumMode enumMode = EnumMode.Full
  EnumMode enumMode = EnumMode.Default; 

  String outdir = null;

  boolean header = true; 

  void init() { 
    options = null;
    target = Language.Java; // default
    enumMode = EnumMode.Default; 
    outdir = null;
    header = true; 
  }

  void addOption(String opt) { 
    if (opt) { 
      if (options) 
	options += (' ' + opt)
      else 
	options = opt
    }
  }

  def setHeaderOption(opt) { 
    if ('yes' == opt || 'true' == opt) { 
      header = true
    } else { 
      header = false
    }
  }

  String getPhaseName() { 
    switch (phase) { 
    case Phases.INITIALIZATION:        'Initialization'; break;
    case Phases.PARSING:               'Parsing'; break;
    case Phases.CONVERSION:            'Conversion'; break; 
    case Phases.SEMANTIC_ANALYSIS:     'Semantic Analysis'; break;  
    case Phases.CANONICALIZATION:      'Canonicalization'; break;  
    case Phases.INSTRUCTION_SELECTION: 'Instruction Selection'; break;
    case Phases.CLASS_GENERATION:      'Class Generation'; break;
    case Phases.OUTPUT:                'Output'; break;
    case Phases.FINALIZATION:          'Finalization'; break;
    }
  }

  String getExtension(boolean headerFile = false) { 
    switch (target) { 
    case Language.Raw: 
    case Language.Groovy: return 'groovy'
    case Language.Java: return 'java'
    case Language.Scala: return 'scala'
    case Language.ObjectiveC: return headerFile ? 'h' : 'm'
    case Language.Cpp: return headerFile ? 'h' : 'cpp'
    }
  }

  String getBuildFilename() { 
    switch (target) { 
    case Language.Raw: 
    case Language.Groovy: 
    case Language.Java: 
    case Language.Scala: return 'build.xml'
    case Language.ObjectiveC: return 'makefile'
    //case Language.ObjectiveC: return 'GNUmakefile'
    case Language.Cpp: return 'Makefile'
    }
  }
  
  String getSourceFilename(String pkg, String name, 
			   boolean headerFile = false,
			   boolean relative = false) { 
    String path = ''
    String ext = getExtension(headerFile)
    if (!relative && outdir) { 
      path = outdir + File.separator
    }
    path += (getPackagePath(pkg) + name + '.' + ext)
    return path
  }

  String getPackagePath(String pkg) { 
    if (usePackage() && pkg) { 
      if (pkg.endsWith('.')) pkg = pkg.substring(0, pkg.length() - 1) 
      return (pkg.replaceAll('\\.', File.separator) + File.separator)
    }
    ''
  }

  File getOutputFile(String pkg, String name, boolean headerFile = false) { 
    File outfile = new File(getSourceFilename(pkg, name, headerFile))
    if (!outfile.parentFile.exists()) {  
      outfile.parentFile.mkdirs()
    }
    return outfile
  }

  File getOutputFile(String name) { 
    String path = ''
    if (outdir) { 
      path = outdir + File.separator
    }    
    path += name
    File file = new File(path)
    if (!file.parentFile.exists()) {  
      file.parentFile.mkdirs()
    }
    return file
  }

  File getBuildFile() { 
    String path = ''
    if (outdir) { 
      path = outdir + File.separator
    }    
    path += getBuildFilename()
    File outfile = new File(path)
    if (!outfile.parentFile.exists()) {  
      outfile.parentFile.mkdirs()
    }
    return outfile
  }

  boolean usePackage() { 
    return (target != Language.ObjectiveC) && (target != Language.Cpp)
  }

}