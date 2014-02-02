package xj.mobile.builder

import org.codehaus.groovy.control.*
import xj.mobile.Main

enum MessageType { Error, Warning }

class ErrorMessage { 
  MessageType type = MessageType.Error
  String file
  int line = 0, col = 0
  String message 
  String extra 

  static ErrorMessage parseMessage(CompilationFailedException cfe) { 
	if (cfe) { 
	  String msg = cfe.message
	  if (msg) { 
		
		String[] lines = msg.split('\n')
		if (lines[0].startsWith('startup failed:')) { 
		  //println '=== lines[1]: ' + lines[1]

		  int i = lines[1].indexOf(':')
		  int j = lines[1].indexOf(':', i + 1)
		  int k = lines[1].lastIndexOf('@ line')
		  int exline = 2
		  String file = lines[1][0 ..< i]
		  String message = lines[1][j+1 .. (k >= 0 ? k-1 : -1)]		  
		  String loc = null
		  if (k > 0) { 
			loc = lines[1].substring(k+1) 
		  } else { 
			k = lines[2].lastIndexOf('@ line')
			if (k > 0) { 
			  loc = lines[2].substring(k+1)
			  exline = 3
			}
		  }

		  String extra = null
		  if (lines.length >= exline + 2) { 
			extra = lines[exline] + '\n' + lines[exline+1]
		  }

		  //println '=== file: ' + file 
		  //println '=== message: ' + message 
		  //println '=== loc: ' + loc

		  int line = 0, col = 0
		  if (loc) { 
			try { 
			  i = loc.indexOf(',')
			  line = loc[6 ..< i] as int
			  col = loc[i+9 .. -2] as int
			} catch (NumberFormatException nfe) { }
		  }
		  
		  if (file == 'work/app.groovy') { 
			file = Main.scriptFile 
			if (line > 0) { 
			  line -= Main.SCRIPT_HEADER_LINE
			}
		  }
		  return new ErrorMessage(file: file, message: message, extra: extra,
								  line: line, col: col)
		}
	  }
	}
	return null
  }

  String toString() { 
	def list = [ "[${type}]" ]
	if (file) list << file
	String loc = ''
	if (line > 0) { 
	  loc = "Line ${line}"
	  if (col > 0) { 
		loc += " Col ${col}"
	  }
	}
	if (loc) list << loc
	if (message) list << message
	String result = list.join(' ')
	if (extra) { 
	  result += ('\n' + extra)
	}
	return result 
  }

}

class ErrorMessages { 

  List<ErrorMessage> errorMessages = []

  boolean isOkay() { 
    errorMessages.size() == 0
  }

  def leftShift(ErrorMessage err) { 
	if (err)
	  errorMessages << err
  }

  void printMessages() { 
	errorMessages.each { e -> println e}
  }

  void addAll(ErrorMessages errors) { 
	if (errors) 
	  errorMessages.addAll(errors.errorMessages)
  }

  void clear() { 
	errorMessages.removeAll()
  }

}