package xj.mobile.lang

import xj.mobile.model.ui.Widget

class WidgetMap {

  static String getPlatformWidgetName(String name, String platform) { 
	def pname = null
	if (name && _widgets[name]) { 
	  pname = _widgets[name][platform]
	  if (pname && pname instanceof List) { 
		pname = pname[0]
      }
	}
	if (!pname) pname = name
	return pname
  }

  static String getNativeWidgetName(String wname, String platform) { 
	if (wname && platform) { 
	  switch (platform) { 
	  case 'ios': return getIOSNativeWidgetName(wname);
	  case 'android': return getAndroidNativeWidgetName(wname);
	  }
	}
	return null
  }

  static getAllWidgetNames() { 
	_widgets.keySet()
  }

  static String mapWidget(Widget widget, String platform) { 
	String pname = null
	if (widget) {
	  platform = platform.toLowerCase()
	  pname = mapSpecialWidget(widget, platform)
	  if (!pname) { 
		String wname = widget.nodeType
		if (_widgets[wname]) { 
		  pname = _widgets[wname][platform]
		  if (pname instanceof List) { 
			pname = pname[0]
		  }
		}
		if (!pname) pname = wname
	  }
	  if (widget.subtype) 
		pname = (pname + '#' + widget.subtype) 
	}
	return pname
  }

  static String mapSpecialWidget(Widget widget, String platform) { 
	String pname = null
	if (widget) { 	
	  String wname = widget.nodeType
	  if (platform == 'ios' && wname == 'Text') { 
		if (widget.lines && (widget.lines as int) > 1) { 
		  pname = 'TextView'
		} else { 
		  pname = 'TextField'
		}
	  }
	}
	return pname
  }

  static String getPlatformNameForWidgetType(String widgetType, String platform) { 
	String pname = null
    if (widgetType) { 
	  String name0 = widgetType
	  String name1 = null
	  int i = widgetType.indexOf('#') 
	  if (i > 0) { 
		name0 = widgetType.substring(0, i) 
		name1 = widgetType.substring(i)
	  }
	  pname = _widgets[name0][platform]
	  if (pname instanceof List) { 
		pname = pname[0]
	  }
	  if (!pname) pname = name0
	  if (name1) pname += name1
	}
	return pname
  }

  private static _widgets = [
    Label :       [ ios     : 'Label', 
					android : 'TextView' ],
    Image :       [ ios     : 'ImageView', 
					android : 'ImageView' ],
    Button :      [ ios     : 'Button', 
					android : 'Button' ],
    Text :        [ ios     : [ 'TextField', 'TextView' ], 
					android : 'EditText' ],
    Switch :      [ ios     : 'Switch',
					android : 'ToggleButton' ],
    Slider :      [ ios     : 'Slider', 
					android : 'SeekBar' ], 
    Selection :   [ ios     : 'SegmentedControl',
					android : 'Spinner' ], 
	NumberStepper:[ ios     : 'Stepper',
					android : 'NumberStepper' ],
    ProgressBar : [ ios     : 'ProgressView', 
					android : 'ProgressBar' ],
    Picker :      [ ios     : 'PickerView', 
					android : 'Spinner' ],
    DatePicker :  [ ios     : 'DatePicker',
					android : 'DatePicker' ],
    TimePicker :  [ ios     : 'TimePicker', 
					android : 'TimePicker' ],
    Web :         [ ios     : 'WebView',
					android : 'WebView' ],
    Map :         [ ios     : 'MapView', 
					android : 'MapView' ],

	ImageButton:  [ ios     : 'Button',
					android : 'ImageButton' ],

    Popup :       [ ios     : 'ActionSheet', 
					android : 'AlertDialog' ],
    Alert :       [ ios     : 'AlertView', 
					android : 'AlertDialog'],

    Menu :        [ ios     : 'ActionSheet', 
					android : [ 'AlertDialog', 'Menu' ] ],  

    View :        [ ios     : 'Control', 
					android : 'View'],
  ]

  private static String getIOSNativeWidgetName(String wname) { 
	if (wname) { 
	  if (wname == 'MapView') { 
		return 'MK' + wname
	  } else { 
		return 'UI' + wname
	  }
	}
	return null
  }

  private static String getAndroidPackageName(String wname) { 
	def pkgnames = [
	  'Menu'        : 'android.view',
	  'View'        : 'android.view',
	  'AlertDialog' : 'android.app',
	  'WebView'     : 'android.webkit',
	  'MapView'     : 'com.google.android.maps',

	  'NumberStepper' : '__CUSTOM__',
	]
	if (wname) { 
	  def pkg = pkgnames[wname]
	  if (pkg) 
		return pkg
	  else  
		return 'android.widget'
	}
	return null
  }

  private static String getAndroidNativeWidgetName(String wname) { 
	if (wname) { 
	  return getAndroidPackageName(wname) + '.' + wname
	}
	return null
  }

  static widgets_ios = [
    Label :            [],
    ImageView :        [],
    Button :           [],
    TextField :        [],
    Switch :           [],
    Slider :           [],
    SegmentedControl : [],
	Stepper :          [],
    ProgressView :     [],
    PickerView :       [],
    DatePicker :       [],  // UIDatePicker + UIDatePickerModeDate
    TimePicker :       [],  // UIDatePicker + UIDatePickerModeTime
    WebView :          [],
    MapView :          [],

    ActionSheet :      [],
    AlertView :        [],
  ]

  static widgets_android = [
    TextView :     [],
    ImageView :    [],
    ImageButton :  [],
    Button :       [],
    EditText :     [],
    ToggleButton : [],
    Slider :       [],
    CheckBox :     [],
    RadioButton :  [],
    Spinner :      [],
    ProgressBar :  [],
    DatePicker :   [],   
    TimePicker :   [],  
    WebView :      [],
    MapView :      [],

    AlertDialog :  [],
  ]

}  