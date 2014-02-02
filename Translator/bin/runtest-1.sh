#for file in test/groovy/trans/*.groovy

dir=test/groovy/trans
files="Closure01 Closure02 Numbers01 Properties01 Returns01 Returns02 Strings01 Dependencies01 Dependencies02 Dependencies03 Methods01 Methods02 Methods03 Methods04 Methods05 Constructor01 Types Types01 Types02 Types03 Types04 Types05 Static01 Static02 Constants01 NonBinary DynamicReturns Polymorphism01 Polymorphism02 Operators01 Operators02 Operators03"
output=output.txt
outref=output-ref.txt

cd $dir

for i in $files
do
    cd ref/Test$i-java
    echo "%%%%%%%%%%%%% Run $dir/ref/Test$i-java"
    #java Test$i >> ../../$output 2>&1 
    ant run 2>&1 | grep "\[java\]" > $output 
    if diff $output $outref; then
	echo "============ Run(result-diff) $dir/ref/ Test$i java: Success"
    else
	echo "============ Run(result-diff) $dir/ref/ Test$i java: Fail"
    fi
    cd ../..

    cd ref/Test$i-objc
    echo "%%%%%%%%%%%%% Run $dir/ref/Test$i-objc"
    ./Test$i 2>&1 > $output
    if diff $output $outref; then
	echo "============ Run(result-diff) $dir/ref/ Test$i objc: Success"
    else
	echo "============ Run(result-diff) $dir/ref/ Test$i objc: Fail"
    fi 
    cd ../..

#    cd ref/Test$i-groovy
#    echo "%%%%%%%%%%%%% Run $dir/ref/Test$i-groovy"
#    echo "%%%%%%%%%%%%% Run $dir/ref/Test$i-groovy" >> ../../$output 
#    groovy Test$i.groovy >> ../../$output 2>&1 
#    cd ../..

done
