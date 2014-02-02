//
//  FirstViewController.m
//  csc699iPhone
//
//  Created by Mick O'Dwyer on 5/27/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import "FirstViewController.h"

//#import "RKCommunicator.h"

#import <RestKit/RKRequestSerialization.h>

//#include <openssl/bio.h>
//#include <openssl/evp.h>
#import "NSDataAdditions.h"


@interface FirstViewController ()


@end

@implementation FirstViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        self.title = NSLocalizedString(@"First", @"First");
        self.tabBarItem.image = [UIImage imageNamed:@"first"];
    }
    return self;
}
							
- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.    
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPhone) {
        return (interfaceOrientation != UIInterfaceOrientationPortraitUpsideDown);
    } else {
        return YES;
    }
}


// Get all the users.
-(IBAction)btnGetUsersTouched {
    RKClient *client = [RKClient sharedClient];
    client.requestQueue.requestTimeout = 10;    
    client.authenticationType = RKRequestAuthenticationTypeHTTPBasic;    
    client.username = @"admin";
    client.password = @"password";

    [client.HTTPHeaders setObject:@"admin:password" forKey:@"Authorization"];
    [client.HTTPHeaders setObject:@"application/xml" forKey:@"Content-Type"];
        
    NSString *getResourcePath = @"/users";
    [client get:getResourcePath delegate:nil];   
}


// Add a user to the web service.
-(IBAction)btnAddUserTouched {
    RKClient *singleClient = [RKClient sharedClient];
    
    NSString *filterXML = [NSString stringWithContentsOfFile:
                           [[NSBundle mainBundle] pathForResource:@"addAlice" ofType:@"xml" inDirectory:@"UploadFiles"]                                                    encoding:NSUTF8StringEncoding error:nil];
        
    [singleClient post:@"/users" params:[RKRequestSerialization serializationWithData:[filterXML dataUsingEncoding:NSUTF8StringEncoding] MIMEType:RKMIMETypeXML]
              delegate:nil];    
}

// Delete a user account.
-(IBAction)btnDeleteUserTouched {
  [[RKClient sharedClient] delete:@"/users/4" delegate:nil];    
}


// Update a user account.
-(IBAction)btnUpdateUserTouched {
    RKClient *singleClient = [RKClient sharedClient];
    
    NSString *filterXML = [NSString stringWithContentsOfFile:
                           [[NSBundle mainBundle] pathForResource:@"updateJohnDoe" ofType:@"xml" inDirectory:@"UploadFiles"]
                                                    encoding:NSUTF8StringEncoding error:nil];
        
    // send the data
    [singleClient put:@"/users" params:[RKRequestSerialization serializationWithData:[filterXML dataUsingEncoding:NSUTF8StringEncoding] MIMEType:RKMIMETypeXML]
              delegate:nil];
}


// Get a listing of all the files from the web service.
-(IBAction)btnGetFilesTouched {
    RKClient *client = [RKClient sharedClient];
    client.requestQueue.requestTimeout = 10;
    client.authenticationType = RKRequestAuthenticationTypeHTTPBasic;    
    client.username = @"admin";
    client.password = @"password";
    
    [client.HTTPHeaders setObject:@"admin:password" forKey:@"Authorization"];   
    [client.HTTPHeaders setObject:@"application/json" forKey:@"Content-Type"];
        
    NSString *getResourcePath = @"/files";
    [client get:getResourcePath delegate:nil];  
}


// Add a new file to the web service.
-(IBAction)btnAddFileTouched {
    RKClient *client = [RKClient sharedClient];
    client.requestQueue.requestTimeout = 10;
    client.authenticationType = RKRequestAuthenticationTypeHTTPBasic;    
    client.username = @"admin";
    client.password = @"password";
    
    [client.HTTPHeaders setObject:@"admin:password" forKey:@"Authorization"];
        
    RKParams* params = [RKParams params];
    [params setValue:@"new code file" forParam:@"name"];
    [params setValue:@"this is a new note" forParam:@"note"];
    [params setValue:@"" forParam:@"fileURL"];
    [params setValue:@"" forParam:@"filename"];
    [params setValue:@"true" forParam:@"saveInWebServiceDirectory"];
    [params setValue:@"false" forParam:@"saveInWebServiceDatabase"];
    [params setValue:@"2012" forParam:@"createdDate_year"];
    [params setValue:@"5" forParam:@"createdDate_month"];
    [params setValue:@"17" forParam:@"createdDate_day"]; 
    [params setFile:@"/Users/mod/Documents/xml.xml" forParam:@"payload"];
    
    [client post:@"/files" params:params delegate:nil];    
}


// Delete a file from the web service.
-(IBAction)btnDeleteFileTouched {
    [[RKClient sharedClient] delete:@"/files/1" delegate:nil];    
}





// Update the content of a file saved on the web service.
-(IBAction)btnUpdateFileTouched {
    RKClient *client = [RKClient sharedClient];
    client.requestQueue.requestTimeout = 10;
    client.authenticationType = RKRequestAuthenticationTypeHTTPBasic;    
    client.username = @"admin";
    client.password = @"password";
    [client.HTTPHeaders setObject:@"admin:password" forKey:@"Authorization"];
    
    NSDictionary* paramsDictionary = [NSDictionary dictionaryWithObjectsAndKeys:
                                      @"new code file", @"name",
                                      @"this is CHANGED note text", @"note",
                                      @"/userUploads/admin/xml.xml", @"fileURL",
                                      @"xml.xml", @"filename",
                                      @"false", @"saveInWebServiceDirectory",
                                      @"true", @"saveInWebServiceDatabase",
                                      @"2012", @"createdDate_year",
                                      @"5", @"createdDate_month",
                                      @"17", @"createDate_day",
                                      @"/Users/mod/Documents/xml.xml", @"payload",
                                      nil];
   
    [client post:@"/files/1" params:paramsDictionary delegate:nil];
}


//  Download a file from the web sevice and save it to Documents folder.
-(IBAction)downloadFile {
    // allocate data buffer
    _responseData = [[NSMutableData alloc] init];
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:[NSURL URLWithString:@"http://localhost:8080/share-space/files/1"]];
   
    [request setValue:@"Basic admin:password" forHTTPHeaderField:@"Authorization"];
    
    nzbConnection = [[NSURLConnection alloc] initWithRequest:request delegate:self];
}


// didReceiveResponse from NSURLConnection request.
- (void)connection:(NSURLConnection *)connection didReceiveResponse:(NSURLResponse *)response {
    filename = response.suggestedFilename;
}


// didReceiveData from NSURLConnection request.
- (void)connection:(NSURLConnection *)connection didReceiveData:(NSData *)data
{
    // Store the data received
    [_responseData appendData:data];
    
    // Get path to Documents folder for iPhone.
    NSString *applicationDocumentsDir = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) objectAtIndex:0];
    
    NSLog(@"Download File Saved at: %@", applicationDocumentsDir);
    
    // Set full path with name of the file culled from Content-Disposition.
    NSString *storePath = [applicationDocumentsDir stringByAppendingPathComponent:filename];    
        
    NSError* error;
    
    // Write file to the directory.
    [_responseData writeToFile:storePath options:NSDataWritingAtomic error:&error];    
    
    if(error != nil) {
        NSLog(@"write error %@", error);
    }

}

- (void)connection:(NSURLConnection *)connection didFailWithError:(NSError *)error
{
    // Show error
    NSLog(@"NSURLConnection Failed: %@", error);
}

- (void)connectionDidFinishLoading:(NSURLConnection *)connection
{
    // Once this method is invoked, "responseData" contains the complete result
    NSLog(@"%@", _responseData);
}


// didLoadResponse for RestKit requests.
- (void)request:(RKRequest*)request didLoadResponse:(RKResponse*)response {  
    if ([request isGET]) { 
        
        if ([response isOK]) { 
            //NSLog(@"Retrieved response: %@", [response bodyAsString]);  
        }  
        
    } else if ([request isDELETE]) {  
         
        if ([response isNotFound]) {  
            NSLog(@"The resource path '%@' was not found.", [request resourcePath]);  
        }  
    }  
}  

- (void)request:(RKRequest *)request didFailLoadWithError:(NSError *)error
{
    NSLog(@"Failure of GET with error %@.", error);
}

@end
