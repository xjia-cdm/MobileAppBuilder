//
//  FirstViewController.h
//  csc699iPhone
//
//  Created by Mick O'Dwyer on 5/27/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "RestKit/RestKit.h"

@interface FirstViewController : UIViewController {
// Variables used in download of file.
@protected
    NSMutableURLRequest* req;
    NSMutableData* _responseData;
    NSURLConnection* nzbConnection;
    NSString* filename;
}
-(IBAction)downloadFile;

// "User" interaction with web service
-(IBAction)btnGetUsersTouched;
-(IBAction)btnAddUserTouched;
-(IBAction)btnDeleteUserTouched;
-(IBAction)btnUpdateUserTouched;

// "File" interaction with web service
-(IBAction)btnGetFilesTouched;
-(IBAction)btnAddFileTouched;
-(IBAction)btnDeleteFileTouched;
-(IBAction)btnUpdateFileTouched;

@end
