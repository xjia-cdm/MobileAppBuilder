//
//  RKCommunicator.m
//  csc699iPhone
//
//  Created by Mick O'Dwyer on 5/28/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import "RKCommunicator.h"


@implementation RKCommunicator

- (void)sendRequests { 
    
    NSLog(@"RKCommunicator: sendRequests: hit");
    // Perform a simple HTTP GET and call me back with the results  
    //[[RKClient sharedClient] get:@"/users" delegate:self];  
    
    // Send a POST to a remote resource. The dictionary will be transparently  
    // converted into a URL encoded representation and sent along as the request body  
    //NSDictionary* params = [NSDictionary dictionaryWithObject:@"RestKit" forKey:@"Sender"];  
    //[[RKClient sharedClient] post:@"/other.json" params:params delegate:self];  
    
    // DELETE a remote resource from the server  
    //[[RKClient client] delete:@"/missing_resource.txt" delegate:self]; 
    //RKClient *client = [RKClient sharedClient];
    RKClient *client = [RKClient clientWithBaseURLString:@"http://localhost:8080/share-space"];
    client.requestQueue.requestTimeout = 10;
    
    client.authenticationType = RKRequestAuthenticationTypeHTTPBasic;
    client.username = @"admin";
    client.password = @"password";
    
    [client.HTTPHeaders setObject:@"admin:password" forKey:@"Authorization"];
    [client.HTTPHeaders setObject:@"application/xml" forKey:@"Content-Type"];
    
    NSLog(@"HTTP headers added: %@.", client.HTTPHeaders);
    
    RKLogConfigureByName("RestKit/Network", RKLogLevelTrace);
    
    //NSDictionary *queryParameters = [NSDictionary dictionaryWithObjectsAndKeys:
    //                                 @"admin:password", @"Authorization",
    //                                 nil];
    //NSString *getResourcePath = RKPathAppendQueryParams(@"/users", queryParameters);
    NSString *getResourcePath = @"/users";
    NSLog(@"about to request users");
    RKRequest *request = [client get:getResourcePath delegate:self];
    //request.username = @"admin";
    //request.password = @"password";
    

}  

- (void)request:(RKRequest*)request didLoadResponse:(RKResponse*)response {  
    if ([request isGET]) {  
        // Handling GET /foo.xml  
        
        if ([response isOK]) {  
            // Success! Let's take a look at the data  
            NSLog(@"Retrieved XML: %@", [response bodyAsString]);  
        }  
        
    } else if ([request isPOST]) {  
        
        // Handling POST /other.json  
        if ([response isJSON]) {  
            NSLog(@"Got a JSON response back from our POST!");  
        }  
        
    } else if ([request isDELETE]) {  
        
        // Handling DELETE /missing_resource.txt  
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
