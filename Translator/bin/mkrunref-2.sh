#for file in test/groovy/trans/simple/*.groovy

dir=test/groovy/trans/simple
output=runtest-output.txt
outref=output-ref.txt

cd $dir
echo "%%%%%%%%%%%%% Run $dir `date`" > $output

for i in {1..15} 100 140
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
