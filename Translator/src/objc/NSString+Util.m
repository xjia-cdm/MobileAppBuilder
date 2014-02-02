
#import "NSString+Util.h"

@implementation NSString ( Util )

// method definitions

- (NSString*) plus:(NSString*) other 
{
  return [self stringByAppendingString:other];
}

- (NSString*) minus:(NSString*) other 
{
  NSRange range = [self rangeOfString:other];
  if (range.length > 0) {
    return [self stringByReplacingCharactersInRange:range withString: @""];
  }
  return self; 
}

- (NSString*) multiply:(int) n
{
  if (n == 0) { 
    return @"";
  } else if (n > 1) {
    int len = [self length];
    NSMutableString *str = [NSMutableString stringWithCapacity:len * n];     
    while (n-- > 0) {
      [str appendString:self];
    }
    return str;
  }
  return self; 
}

-(NSString *) reverse
{
  NSMutableString *reversedStr;
  int len = [self length];
 
  // Auto released string
  reversedStr = [NSMutableString stringWithCapacity:len];     
 
  // Probably woefully inefficient...
  while (len > 0)
    [reversedStr appendString:
		   [NSString stringWithFormat:@"%C", [self characterAtIndex:--len]]];   
 
  return reversedStr;
}

- (NSString *) next 
{
  int len = [self length];
  if (len > 0) {
    NSMutableString *str = [NSMutableString stringWithCapacity:len];     
    [str setString:[self substringToIndex: len - 1]];
    unichar c = [self characterAtIndex: len - 1];
    [str appendString: [NSString stringWithFormat:@"%C", ++c]]; 
    return str;
  }
  return self; 
}

- (NSString *) previous
{
  int len = [self length];
  if (len > 0) {
    NSMutableString *str = [NSMutableString stringWithCapacity:len];     
    [str setString:[self substringToIndex: len - 1]];
    unichar c = [self characterAtIndex: len - 1];
    [str appendString: [NSString stringWithFormat:@"%C", --c]]; 
    return str;
  }
  return self; 
}
 
@end
