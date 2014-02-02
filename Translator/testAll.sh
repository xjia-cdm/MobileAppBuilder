#for file in test/groovy/trans/*.groovy
ant translator

dir=test/groovy/trans
cont=false
success="\nSuccessful Tests:"
fail="\nFailed Tests:"

for i in Closure01 Numbers01 Properties01 Returns01 Returns02 Strings01 Dependencies01 Dependencies02 Dependencies03 Methods Types Types01 Types02 Types03 Types04 Types05 NonBinary DynamicReturns
do	
	rm -f "${dir}/temp/"*
	perl bin/gt -output=temp/ -header=no $dir/Test$i.groovy
	if [ -f "${dir}/temp/Test${i}.java" ]
	then
		success="${success}\nTest${i}"
	else
		fail="${fail}\nTest${i}"
	fi
	
done
rm -f "${dir}/temp/"*
rmdir "${dir}/temp/"

#special case for test 1 - 5
rm -f "${dir}/simple/temp/pkg1/pkg2/"*
perl bin/gt -output=temp/ -header=no $dir/simple/Test1.groovy
if [ -f "${dir}/simple/temp/pkg1/pkg2/Test1.java" ]
then
	success="${success}\nsimple/Test1"
	rm -f "${dir}/simple/temp/pkg1/pkg2/"*
	rmdir "${dir}/simple/temp/pkg1/pkg2"
	rmdir "${dir}/simple/temp/pkg1"
else
	fail="${fail}\nsimple/Test1"
fi

rm -f "${dir}/simple/temp/xj/pkg1/"*
perl bin/gt -output=temp/ -header=no $dir/simple/Test2.groovy
if [ -f "${dir}/simple/temp/xj/pkg1/A.java" ]
then
	success="${success}\nsimple/Test2"
	rm -f "${dir}/simple/temp/xj/pkg1/"*
	rmdir "${dir}/simple/temp/xj/pkg1"
	rmdir "${dir}/simple/temp/xj"
else
	fail="${fail}\nsimple/Test2"
fi

rm -f "${dir}/simple/temp/xj/pkg2/"*
perl bin/gt -output=temp/ -header=no $dir/simple/Test3.groovy
if [ -f "${dir}/simple/temp/xj/pkg2/A.java" ]
then
	success="${success}\nsimple/Test3"
	rm -f "${dir}/simple/temp/xj/pkg2/"*
	rmdir "${dir}/simple/temp/xj/pkg2"
	rmdir "${dir}/simple/temp/xj"
else
	fail="${fail}\nsimple/Test3"
fi

rm -f "${dir}/simple/temp/pkg1/pkg2/"*
perl bin/gt -output=temp/ -header=no $dir/simple/Test4.groovy
if [ -f "${dir}/simple/temp/pkg1/pkg2/Test4.java" ]
then
	success="${success}\nsimple/Test4"
	rm -f "${dir}/simple/temp/pkg1/pkg2/"*
	rmdir "${dir}/simple/temp/pkg1/pkg2"
	rmdir "${dir}/simple/temp/pkg1"
else
	fail="${fail}\nsimple/Test4"
fi

rm -f "${dir}/simple/temp/"*
perl bin/gt -output=temp/ -header=no $dir/simple/Test5.groovy
if [ -f "${dir}/simple/temp/B.java" ]
then
	success="${success}\nsimple/Test5"
	rm -f "${dir}/simple/temp/"*
else
	fail="${fail}\nsimple/Test5"
fi

for i in 6 100 140
do
	rm -f "${dir}/simple/temp/"*
	perl bin/gt -output=temp/ -header=no $dir/simple/Test$i.groovy
	
	if [ -f "${dir}/simple/temp/Test${i}.java" ]
	then
		success="${success}\nsimple/Test${i}"
	else
		fail="${fail}\nsimple/Test${i}"
	fi
done
rm -f "${dir}/simple/temp/"*
rmdir "${dir}/simple/temp/"

echo -e $success
echo -e $fail
