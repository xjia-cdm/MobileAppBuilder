
#import "NSMutableArray+Util.h"

@implementation NSMutableArray ( Util )

// method definitions

- (NSMutableArray*) plus:(id) other 
{
  int len = [self count];
  NSMutableArray* result;
  if ([other isKindOfClass:[NSArray class]]) {
    len += [other count];
    result = [NSMutableArray arrayWithCapacity: len];    
    [result addObjectsFromArray:self];
    [result addObjectsFromArray:other];
  } else {
    len++;
    result = [NSMutableArray arrayWithCapacity: len];    
    [result addObjectsFromArray:self];
    [result addObject:other];    
  }
  return result;
}

- (NSMutableArray*) minus:(id) other 
{
  int len = [self count];
  NSMutableArray* result = [NSMutableArray arrayWithCapacity: len];    
  [result addObjectsFromArray:self];
  if ([other isKindOfClass:[NSArray class]]) {
    [result removeObjectsInArray:other]; 
  } else {
    [result removeObject:other]; 
  }
  return result;
}

- (NSMutableArray*) multiply:(id) num
{
  NSMutableArray* result;
  if ([num isKindOfClass:[NSNumber class]]) {
    int n = [num intValue]; 
    if (n == 0) { 
      return [NSMutableArray arrayWithCapacity:16];
    } else if (n > 1) {
      int len = [self count];
      result = [NSMutableArray arrayWithCapacity: len * n];     
      while (n-- > 0) {
	[result addObjectsFromArray:self];
      }
      return result;
    }
  }
  return self; 
}

-(NSMutableArray *) reverse
{
  NSMutableArray *reversed;
  int len = [self count];
 
  // Auto released string
  reversed = [NSMutableArray arrayWithCapacity:len];     
 
  // Probably woefully inefficient...
  while (len > 0)
    [reversed addObject: [self objectAtIndex:--len]];   
 
  return reversed;
}


 
@end
