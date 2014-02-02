#for file in test/groovy/trans/*.groovy
ant translator

dir=test/groovy/trans
cont=false
success="\nSuccessful Tests:"
fail="\nFailed Tests:"

for i in Closure01 Numbers01 Properties01 Returns01 Returns02 Strings01 Dependencies01 Dependencies02 Dependencies03 Methods Types Types01 Types02 Types03 Types04 Types05 NonBinary DynamicReturns
do	
	rm -f "${dir}/temp/"*
	perl bin/gt -output=temp/ -target=objc -header=no $dir/Test$i.groovy
	if [ -f "${dir}/temp/Test${i}.h" -o  -f "${dir}/temp/Test${i}.m" ]
	then
		success="${success}\nTest${i}"
	else
		fail="${fail}\nTest${i}"
	fi
	
done
rm -f "${dir}/temp/"*
rmdir "${dir}/temp/"


for i in {1..4}
do
	rm -f "${dir}/simple/out/"*
	perl bin/gt -output=temp/ -target=objc -header=no $dir/simple/Test${i}.groovy
	if [ -f "${dir}/simple/temp/out/Test${i}.h" -o -f "${dir}/simple/temp/out/Test${i}.m" ]
	then
		success="${success}\nsimple/Test${i}"
		rm -f "${dir}/simple/temp/out/"*
		rmdir "${dir}/simple/temp/out"
	else
		fail="${fail}\nsimple/Test${i}"
	fi
done

rm -f "${dir}/simple/temp/"*
perl bin/gt -output=temp/ -target=objc -header=no $dir/simple/Test5.groovy
if [ -f "${dir}/simple/temp/B.h" -o -f "${dir}/simple/temp/B.m" ]
then
	success="${success}\nsimple/Test5"
	rm -f "${dir}/simple/temp/"*
else
	fail="${fail}\nsimple/Test5"
fi

for i in 6 100 140
do
	rm -f "${dir}/simple/temp/"*
	perl bin/gt -output=temp/ -target=objc -header=no $dir/simple/Test$i.groovy
	
	if [ -f "${dir}/simple/temp/Test${i}.h" -o -f "${dir}/simple/temp/Test${i}.m" ]
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
