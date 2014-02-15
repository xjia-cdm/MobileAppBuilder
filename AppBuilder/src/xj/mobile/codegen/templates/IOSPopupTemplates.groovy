package xj.mobile.codegen.templates

import static xj.translate.Logger.info 

class IOSPopupTemplates extends PopupTemplates {

  IOSPopupTemplates(String target) { 
	super(target)
  }

  static popupTemplates = [
    ActionSheet : [ 
      uiclass : 'UIActionSheet',
 
      action : '[self showActionSheet_${name}];',
      actionMenu : '[self showActionSheet_${name}:indexPath];',
      actionData : '[self showActionSheet_${name}_withData:${data}];',
      actionMenuData : '[self showActionSheet_${name}:indexPath withData:${data}];',

      create :  '''${name} = [[UIActionSheet alloc] initWithTitle:${title}
\t\t\t\tdelegate:self
\t\t\t\tcancelButtonTitle:${cancel} 
\t\t\t\tdestructiveButtonTitle:${affirm} 
\t\t\t\totherButtonTitles:${other}];
${name}.actionSheetStyle = UIActionSheetStyleDefault;''', 

      show : '''
- (void)showActionSheet_${name}${arg}
{
${indent(body)}
	[${name} showInView:self.view];
}
''',

      delegate : 'UIActionSheetDelegate',
      delegate_action_pre : 'NSString *${param} = [actionSheet buttonTitleAtIndex:buttonIndex];'
    ],

    AlertView : [ 
      uiclass : 'UIAlertView',

      action : '[self showAlert_${name}];',
      actionMenu : '[self showAlert_${name}:indexPath];',
      actionData : '[self showAlert_${name}_withData:${data}];',
      actionMenuData : '[self showAlert_${name}:indexPath withData:${data}];',

      create : '''${name} = [[UIAlertView alloc] initWithTitle:${title}
\t\t\t\tmessage:${message}
\t\t\t\tdelegate:self
\t\t\t\tcancelButtonTitle:${cancel} 
\t\t\t\totherButtonTitles:${other}];''',

      show : '''
- (void)showAlert_${name}${arg}
{
${indent(body)}
	[${name} show];	
}
''',

      delegate : 'UIAlertViewDelegate',	
      delegate_action_pre : 'NSString *${param} = [alertView buttonTitleAtIndex:buttonIndex];'
    ],
  ]

}