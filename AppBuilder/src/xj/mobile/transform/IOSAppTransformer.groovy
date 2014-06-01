
package xj.mobile.transform

import xj.mobile.*
import xj.mobile.common.*
import xj.mobile.model.*
import xj.mobile.model.ui.*

class IOSAppTransformer extends AppTransformer {  

  static textFieldTypeMap = [ 'decimal': [ 'UIKeyboardTypeNumbersAndPunctuation',  'decimal' ],
							  'number':  [ 'UIKeyboardTypeNumbersAndPunctuation', 'number' ],
							  'email':   [ 'UIKeyboardTypeEmailAddress', 'email' ],
							  'phone':   [ 'UIKeyboardTypePhonePad', 'phone' ], 
							  'tel':     [ 'UIKeyboardTypePhonePad', 'tel' ], 
							  'url':     [ 'UIKeyboardTypeURL', 'url' ] 
							];

  def widgetTransformationRules
  def graphicsTransformationRules

  void init() { 
	widgetTransformationRules = [

	  View: [
		[ true, 
		  { 
			if (Main.userConfig.platform.ios.auto_layout) { 
			  it['#autoLayout'] = true
			}
		  }
		]
	  ],

	  Popup: [
		[ { lhs -> lhs.message != null },
		  { lhs ->
			lhs['#type'] = 'Alert'
		  }
		]
	  ],

	  Text: [
		[ true,
		  { 
			if (it.type != null) {  
			  def defval = textFieldTypeMap[it.type]
			  if (defval) { 
				if (it.keyboardType == null) it.keyboardType = defval[0]
				if (it.prompt == null) it.prompt = defval[1]
			  }
			}
			if (it.placeholder == null && it.prompt != null) { 
			  it.placeholder = it.prompt 
			}
		  }
		]
	  ],

	  onDoubleTap: [
		[ true,
		  { lhs -> 
			lhs['#type'] = 'onTap'
			lhs.taps = 2
		  }
		]
	  ],

	]

	graphicsTransformationRules = [
	  Arc: [ 
		[ true,
		  { lhs -> 
			if (lhs.startAngle != null) lhs.startAngle = (lhs.startAngle as float) / 180f * Math.PI
			if (lhs.endAngle != null) lhs.endAngle = (lhs.endAngle as float) / 180f * Math.PI
		  }
		]
	  ]
	]
	transformationRules	= widgetTransformationRules + graphicsTransformationRules
  }

  // by-pass transformation 
  //void transform(Application app) { }

}