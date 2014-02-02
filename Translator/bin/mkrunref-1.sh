#for file in test/groovy/trans/*.groovy

dir=test/groovy/trans
files="Closure01 Closure02 Numbers01 Properties01 Returns01 Returns02 Strings01 Dependencies01 Dependencies02 Dependencies03 Methods01 Methods02 Methods03 Methods04 Methods05 Constructor01 Types Types01 Types02 Types03 Types04 Types05 Static01 Static02 Constants01 NonBinary DynamicReturns Polymorphism01 Polymorphism02 Operators01 Operators02 Operators03"
output=runtest-output.txt
outref=output-ref.txt

cd $dir
echo "%%%%%%%%%%%%% Run $dir `date`" > $output

for i in $files
do
    echo "%%%%%%%%%%%%% Run $dir/Test$i"
    echo "%%%%%%%%%%%%% Run $dir/Test$i" >> $output
    groovy Test$i.groovy >> $output 2>&1

    cd ref/Test$i-java
    echo "%%%%%%%%%%%%% Run $dir/ref/Test$i-java"
    echo "%%%%%%%%%%%%% Run $dir/ref/Test$i-java" >> ../../$output 
    #java Test$i >> ../../$output 2>&1 
    if ant run; then
	echo "============ Run(complete) $dir/ref/ Test$i java: Success"
    else
	echo "============ Run(complete) $dir/ref/ Test$i java: Fail"
    fi
    ant run | grep "\[java\]" >> ../../$output 2>&1 
    ant run 2>&1 | grep "\[java\]" > $outref 
    cd ../..

    cd ref/Test$i-objc
    echo "%%%%%%%%%%%%% Run $dir/ref/Test$i-objc"
    echo "%%%%%%%%%%%%% Run $dir/ref/Test$i-objc" >> ../../$output 
    if ./Test$i; then
	echo "============ Run(complete) $dir/ref/ Test$i objc: Success"
    else
	echo "============ Run(complete) $dir/ref/ Test$i objc: Fail"
    fi 
    ./Test$i >> ../../$output 2>&1
    ./Test$i 2>&1 > $outref
    cd ../..

#    cd ref/Test$i-groovy
#    echo "%%%%%%%%%%%%% Run $dir/ref/Test$i-groovy"
#    echo "%%%%%%%%%%%%% Run $dir/ref/Test$i-groovy" >> ../../$output 
#    groovy Test$i.groovy >> ../../$output 2>&1 
#    cd ../..

    echo "" >> $output
done
