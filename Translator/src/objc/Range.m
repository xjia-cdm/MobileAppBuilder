
#import "Range.h"

NSArray* rangeWithIntegers(int from, int to, BOOL inclusive)
{
  int n = to - from;
  if (n < 0) n = -n;
  if (inclusive) n++;
  NSNumber* ia[n];
  if (from <= to) {
    int k = 0;
    for (int i = from; i <= to; i++) {
      if (inclusive || i < to)
	ia[k++] = [NSNumber numberWithInt:i];
    }
  } else { 
    int k = 0;
    for (int i = from; i >= to; i--) {
      if (inclusive || i > to)
	ia[k++] = [NSNumber numberWithInt:i];
    }
  }
  return [NSArray arrayWithObjects:ia count:n];
}

