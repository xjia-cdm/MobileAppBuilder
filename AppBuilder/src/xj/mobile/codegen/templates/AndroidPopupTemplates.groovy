package xj.mobile.codegen.templates

import static xj.translate.Logger.info 

class AndroidPopupTemplates extends PopupTemplates {

  AndroidPopupTemplates(String target) { 
	super(target)
  }

  static popupTemplates = [
    AlertDialog : [
      action : '''alertAction${capitalize(name)}().show();''',
      actionMenu : '''alertAction${capitalize(name)}().show();''',
      actionData : '''alertAction${capitalize(name)}(${data}).show();''',
      actionMenuData : '''alertAction${capitalize(name)}(${data}).show();''',

      create : '''
AlertDialog alertAction${capitalize(name)}(${arg}) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);${setters}
    builder.setCancelable(${cancellable});${indent(buttons, 1, '    ')}
    AlertDialog alert = builder.create();
    return alert;
}
''',

      listdata : '''
static final String[] ${name}Data = {
${indent(list, 1, '    ')}
};
''',

      items : 'builder.set${type}Items(${items}, ${listener});',

      button : 'builder.set${type}Button(${text}, ${listener});',

      onclick : '''new DialogInterface.OnClickListener() {
    public void onClick(DialogInterface dialog, int id) {
${indent(actionCode, 2, '    ')}
    }
}''',
    ],
   
  ]

}