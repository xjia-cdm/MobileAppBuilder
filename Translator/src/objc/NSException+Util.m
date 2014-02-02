
#import <Foundation/Foundation.h>
#import "NSException+Util.h"

@implementation NSException ( Util )

// method definitions

- (void) printStackTrace
{
  NSLog(@"Caught exception %@: %@", self.name, self.reason);
  NSArray *stack = [self callStackSymbols];
  for(int i = 0; i < stack.count; i++) {
    //NSLog(@"  %@", [stack objectAtIndex:i]);
    printf("  %s\n", [[stack objectAtIndex:i] UTF8String]);
  } 

}


@end
