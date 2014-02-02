#!/usr/bin/awk -f
BEGIN { 
    print "\t\t\tJava\tObjC" 
    c = 0
}
{
    if (c == 0) {
	test = $4
	java = $6
    } else if (c == 1) {
	objc = $6
	printf("%-20s\t%s\t%s\n", test, java, objc) 
    }
    c = ++c % 2
}
END   { 

}