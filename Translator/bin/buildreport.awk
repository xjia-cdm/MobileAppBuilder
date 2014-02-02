#!/usr/bin/awk -f
BEGIN { 
    print "\t\t\tJava\tObjC\tGroovy\tRaw" 
    c = 0
}
{
    if (c == 0) {
	test = $4
	java = $6
    } else if (c == 1) {
	objc = $6
    } else if (c == 2) {
	groovy = $6 
    } else if (c == 3) {
	raw = $6
	printf("%-20s\t%s\t%s\t%s\t%s\n", test, java, objc, groovy, raw) 
    }
    c = ++c % 4
}
END   { 

}