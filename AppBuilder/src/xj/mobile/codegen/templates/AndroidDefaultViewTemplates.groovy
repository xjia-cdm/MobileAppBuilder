package xj.mobile.codegen.templates

import xj.mobile.model.properties.PropertyType

import static xj.mobile.codegen.CodeGenerator.InjectionPoint.*

import static org.codehaus.groovy.ast.ClassHelper.*

class AndroidDefaultViewTemplates { 

  static templates = [

	//
	// handling attributes
	//
	setAttribute: [
	  code: 'set${capitalize(attribute)}(${value})',
	],

    getAttribute: [
	  code: 'get${capitalize(attribute)}()',
	],

	//
	// handle local declaration
	//

	localDecl: [
	  [
		when: { init == null },
		declaration: '${type} ${name};' 
	  ],
	  [
		when: { init != null },
		declaration: '${type} ${name} = ${init};' 
	  ],
	],

	transitionData: [
	  [
		declaration: '${type} ${name};'
	  ],
	  [
		binding: [
		  typeName: { type.endsWith('[]') ? (type[0 .. -3] + 'Array') : type }
		], 
		creation: '${name} = getIntent().getExtras().get${typeName}(\"${viewid.toUpperCase()}_DATA\");'
	  ], 
	],  

	//
	// handle closure declaration
	//

	closureParam: [
	  code: { params.collect{ p -> "${p.typeName} ${p.name}" }.join(', ') }
	],

	closureDecl: [
	  method: '''${type} ${name}(${params}) {
${indent(body)}
}
'''
	],

	//
	// handle keyboard 
	// 

	keyboard: [
	  [ 
		import: 'android.view.inputmethod.InputMethodManager'
	  ],
	  [
		creation: '''
findViewById(android.R.id.content).setOnTouchListener(new View.OnTouchListener() {
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        return false;
    }
});
'''
	  ],
	],

	//
	// handle spinner group 
	//

	spinnerg1: [
	  //when: { actionCode != null || selectCode != null }, 
	  creation: '''AdapterView.OnItemSelectedListener selectionListener = new AdapterView.OnItemSelectedListener() {

    String[] items = { ${items} };

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
${indent(selectCode, 2, '    ')}
${indent(actionCode, 2, '    ')}
    }

    public void onNothingSelected(AdapterView parent) { }

};
'''
	],

	spinnerg2: [
	  creation: '${name}.setOnItemSelectedListener(selectionListener);'
	], 
	
	//
	// handle spinner 
	//

	spinner: [
	  [
		declaration: '''private static final String[] ${name}Data = {
${indent(values, 1, '    ')}
};'''
	  ],
	  [
		creation: '''ArrayAdapter<String> ${name}Adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, ${name}Data);
${name}Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
${name}.setAdapter(${name}Adapter);
'''
	  ]
	],

	//
	// handle slider 
	//

	slider: [
	  when: { actionCode != null }, 
	  creation: '''${name}.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
${indent(actionCode, 2, '    ')}     
    }

    public void onStartTrackingTouch(SeekBar seekBar) {}
    public void onStopTrackingTouch(SeekBar seekBar) {}
});
'''
	],

	//
	//  handle DatePicker 
	//

	datePicker: [
	  [
		when: { actionCode != null }, 
		import: [ 'java.text.DateFormat', 'java.util.Calendar', 'java.util.Date' ],
	  ], 
	  [
		when: { actionCode != null }, 
		creation: '''final Calendar c = Calendar.getInstance();
final int year = c.get(Calendar.YEAR);
final int month = c.get(Calendar.MONTH);
final int day = c.get(Calendar.DAY_OF_MONTH);
${name}.init(year, month, day, new DatePicker.OnDateChangedListener() {

    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        String date = DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date(year, monthOfYear, dayOfMonth));
${indent(actionCode, 2, '    ')}
  }

});
'''
	  ], 
	], 

	//
	//  handle TimePicker 
	//

	timePicker: [
	  [
		when: { actionCode != null }, 
		import: [ 'java.text.DateFormat', 'java.util.Calendar', 'java.util.Date' ],
	  ], 
	  [
		when: { actionCode != null }, 
		creation: '''final Calendar c = Calendar.getInstance();
final int year = c.get(Calendar.YEAR);
final int month = c.get(Calendar.MONTH);
final int day = c.get(Calendar.DAY_OF_MONTH);
${name}.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {

    public void onTimeChanged(TimePicker view, int hour, int minute) {
        String time = DateFormat.getTimeInstance(DateFormat.MEDIUM).format(new Date(year, month, day, hour, minute));
${indent(actionCode, 2, '    ')}
  }

});
'''
	  ],
	],

	//
	// handle interface orientations
	//

	onOrientationChange: [
	  [ 
		method: '''
public void onConfigurationChanged(Configuration config) {
    super.onConfigurationChanged(config);
${indent(code, 1)}
}''',
		parameters: [ orientation: new PropertyType('InterfaceOrientations') ]
	  ],
	  [
		import: 'android.content.res.Configuration'
	  ],
	  [
		do: { attributes[ 'android:configChanges'] = 'orientation|keyboardHidden' }
	  ]
	],

	//
	// Handle gestures 
	// 

	onTap: [
	  [ 
		onTap: '${code}',
		parameters: [ x: float_TYPE, y: float_TYPE ]
	  ],
	  /*
	  [
		when: { touches != null },
		onTap: '''if (event.getPointerCount() >= ${touches}) {
${indent(code)}
}'''
	  ],
	  */
	],

	onDoubleTap: [
	  [ 
		onDoubleTap: '${code}',
		parameters: [ x: float_TYPE, y: float_TYPE ]
	  ]
	],

	afterAction1: [
	  code: '''findViewById(android.R.id.content).postDelayed(new Runnable() {
    public void run() {
${indent(code, 2)}
	}										
}, ${delay.getValueInMilli()});'''
	],

	onSwipe: [
	  [ 
		onFling: '''if ((direction & (${direction ?: \'SwipeGestureDirectionRight\'})) != 0) {
${indent(code)}
}'''

	  ]
	], 

	onPinch: [
	  onScale: '${code}',
	  parameters: [ scale: float_TYPE,
					focusX: float_TYPE, focusY: float_TYPE ]
	],

	onRotation: [
	  onRotate: '${code}',
	  parameters: [ rotation: float_TYPE,
					focusX: float_TYPE, focusY: float_TYPE  ]
	],

	onLongPress: [
	  onLongPress: '${code}',
	  parameters: [ x: float_TYPE, y: float_TYPE ]
	],

	onDrag: [
	  [
		when: { touches == null },
		onDrag: '${code}',
		parameters: [ x: float_TYPE, y: float_TYPE, 
					  distanceX: float_TYPE, distanceY: float_TYPE ]
	  ],
	  [
		when: { touches != null },
		onDrag: '''if (event2.getPointerCount() >= ${touches}) {
${indent(code)}
}''',
		parameters: [ x: float_TYPE, y: float_TYPE, 
					  distanceX: float_TYPE, distanceY: float_TYPE ]
	  ],
	],

  ]

}