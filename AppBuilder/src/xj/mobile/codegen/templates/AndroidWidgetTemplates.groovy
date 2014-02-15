package xj.mobile.codegen.templates

import xj.mobile.lang.WidgetMap
import xj.mobile.android.*

import static xj.mobile.model.impl.ClassModel.getCustomViewName

class AndroidWidgetTemplates extends WidgetTemplates { 

  ///// Templates

  static String declarationTemplate = '${uiclass} ${name};'

  static String findViewTemplate = '${name} = (${uiclass}) findViewById(R.id.${name});'

  AndroidWidgetTemplates(target) { 
    super(target)
  }

  ////// Widget Templates  

  def CommonWidgetTemplate = [

    setAttribute: '${name}.set${capitalize(attribute)}(${value})',
    getAttribute: '${name}.get${capitalize(attribute)}()',
    getBooleanAttribute: '${name}.is${capitalize(attribute)}()',
    getIndexedAttribute: '${name}.get${capitalize(attribute)}()[${index}]',

    action: '''public void ${actionName}(View view) {
${indent(actionBody, 1, '    ')}
}
''',

  ]

  def widgetMap = WidgetMap.widgets_android

  def widgetTemplates = [
    TextView:  [
      uiclass: 'TextView',
    ],

    Button: [
      uiclass: 'Button',
      xevent: 'onClick',
    ],

    EditText: [
      uiclass: 'EditText',

      defaultAttributes: { widget ->
		def attr = [:]
		if (widget.inputType) { 
		  attr['android:inputType'] = widget.inputType
		}
		if (widget.prompt) { 
		  attr['android:hint'] = widget.prompt
		}
		if (widget.lines && widget.lines > 1) {

		} else {  
		  attr['android:singleLine'] = 'true'
		}
		return attr
      },

	  get_text: '${name}.getText().toString()',

	  actionListener: '''
${name}.setOnEditorActionListener(new TextView.OnEditorActionListener() {
    @Override
    public boolean onEditorAction (TextView v, int actionId, KeyEvent event) {
${indent(actionBody, 2, '    ')}
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        return true;
    }
}); 
''',
    ],

    SeekBar: [
      uiclass: 'SeekBar',

	  template: 'Default:slider',
	  skip_action: true, 
    ],

    CheckBox: [
      uiclass: 'CheckBox',
      xevent: 'onClick',
      get_checked: '${name}.isChecked()',
    ],

    RadioButton: [
      uiclass: 'RadioButton',
      xevent: 'onClick',
    ],

    RadioGroup: [
      get_selected: 'selectedRadioButton_${name}()'
    ],

    SpinnerGroup: [
      get_value: 'items',
      get_value_indexed: 'items[${index}]'
    ], 

    ToggleButton: [
      uiclass: 'ToggleButton',
      xevent: 'onClick',
    ],

    Spinner: [
      uiclass: 'Spinner',

      defaultAttributes: [
		'android:drawSelectorOnTop': 'true' 
      ],

	  template: 'Default:spinner',
	  templateVars: [
		values: { widget -> widget.options.collect{ "\"${it}\"" }.join(',\n') }
	  ],

	  actionListener: '''
${name}.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
${indent(actionBody, 2, '    ')}
    }
    public void onNothingSelected(AdapterView parent) { }
});
''',

      //get_value: 'item'
	  get_value: '${name}.getSelectedItem().toString()'

    ],

	NumberStepper: [
	  uiclass: 'NumberStepper',

	  custom: true,
	  superclass: 'LinearLayout',
	  xevent: 'onAction',

	  styleable: [ value: 'float',
				   minValue: 'float',
				   maxValue: 'float',
				   step: 'float',
				   autoRepeat: 'boolean',
				   intValue: 'boolean',
				   onAction: 'string' ],

	  get_value: '${name}.get${capitalize(attribute)}()'

	],

	'NumberStepper#Int': [
	  uiclass: 'NumberStepper',

	  custom: true,
	  superclass: 'LinearLayout',
	  xevent: 'onAction',

	  styleable: [ value: 'float',
				   minValue: 'float',
				   maxValue: 'float',
				   step: 'float',
				   autoRepeat: 'boolean',
				   intValue: 'boolean',
				   onAction: 'string' ],

	  get_value: '(int) ${name}.get${capitalize(attribute)}()'
	],

    TimePicker: [
      uiclass: 'TimePicker',

	  template: 'Default:timePicker',
	  skip_action: true, 

      get_time: 'time'
    ],

    DatePicker: [
      uiclass: 'DatePicker',

	  template: 'Default:datePicker',
	  skip_action: true, 

      get_date: 'date'
    ],

    ProgressBar: [
      uiclass: 'ProgressBar',

      defaultAttributes: [
		'style': '@android:style/Widget.ProgressBar.Horizontal'
      ]
    ],

    ImageView: [
      uiclass: 'ImageView',

      defaultAttributes: [
		'android:src': '@drawable/${xj.mobile.util.CommonUtils.getFileName(file.toLowerCase())}'
      ],

	  initWithAttributes: [ 'file' ],

	  //set_file: '${name}.setImageResource(R.drawable.${value})',
	  set_file: '${name}.setImageResource(getResources().getIdentifier(${xj.mobile.util.CommonUtils.getAndroidResourceName(value)}, \"drawable\", getPackageName()))',

	  layoutProcessor: new ImageLayoutProcessor()
    ],

    ImageButton: [
      uiclass: 'ImageButton',
      xevent: 'onClick',

      defaultAttributes: [
		'android:src': '@drawable/${xj.mobile.util.CommonUtils.getFileName(image.toLowerCase())}'
      ],

	  layoutProcessor: new ImageLayoutProcessor()
    ],

	ListView: [
	  uiclass: 'ListView',
	],

    WebView: [
      uiclass: 'WebView',

      permission: 'INTERNET',

      set_url: '''${name}.loadUrl(${value})''',

      initialAttributes: [ 'url' ],

	  template: 'Web:web1'
    ],

    MapView: [
      uiclass: 'com.google.android.maps.MapView',

      activity: 'MapActivity',

      library: 'com.google.android.maps',

      permission: [ 'INTERNET', 'ACCESS_FINE_LOCATION', 'ACCESS_COARSE_LOCATION' ],

      defaultAttributes: [
		'android:clickable': 'true',
		'android:apiKey': AndroidAppGenerator.androidConfig.defaults.MapView.apiKey, 
      ],

      initialAttributes: [ [ 'latlon', 'span' ] ],

      set_latlon_span: '''MapController mapCtrl = ${name}.getController();
GeoPoint point = new GeoPoint(${Eval.me(\'(int) (\' + latlon[0] + \'*1e6)\')}, ${Eval.me(\'(int) (\' + latlon[1] + \'*1e6)\')});
mapCtrl.setCenter(point);
mapCtrl.zoomToSpan(${Eval.me(\'(int) (\' + span[0] + \'*1e6)\')}, ${Eval.me(\'(int) (\' + span[1] + \'*1e6)\')});''', 


	  template: 'Map:map1'
    ],

	Canvas: [
	  uiclass: { widget -> getCustomViewName(widget.id) },

	  custom: true,

	  processor: new CanvasProcessor()
	],


  ]

}