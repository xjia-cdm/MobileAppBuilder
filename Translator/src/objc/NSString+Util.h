
#import <Foundation/NSString.h>

@interface NSString ( Util )

// method declarations

- (NSString*) plus:(NSString*) other;
- (NSString*) minus:(NSString*) other;
- (NSString*) multiply:(int) n;

- (NSString *) reverse;
- (NSString *) next;
- (NSString *) previous;

@end

