#for file in test/groovy/trans/simple/*.groovy

dir=test/groovy/trans/simple
output=output.txt
outref=output-ref.txt

cd $dir

for i in {1..15} 100 140
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
