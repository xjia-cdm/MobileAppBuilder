//
//  RKCommunicator.h
//  csc699iPhone
//
//  Created by Mick O'Dwyer on 5/28/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <RestKit/RestKit.h> 

@interface RKCommunicator : NSObject <RKRequestDelegate>

- (void)sendRequest;

@end
