#for file in test/groovy/trans/simple/*.groovy

dir=test/groovy/trans/simple
cd $dir/ref
for i in {1..15} 100 140
do
    echo "%%%%%%%%%%%%% Build $dir/ref/Test$i-java"
    cd Test$i-java
    if ant 2>&1; then
	echo "============ Build $dir/ref/ Test$i java: Success"
    else
	echo "============ Build $dir/ref/ Test$i java: Fail"
    fi
    cd ..

    echo "%%%%%%%%%%%%% Build $dir/ref/Test$i-objc"
    cd Test$i-objc
    if make 2>&1; then
	echo "============ Build $dir/ref/ Test$i objc: Success"
    else
	echo "============ Build $dir/ref/ Test$i objc: Fail"
    fi 
    cd ..

    echo "%%%%%%%%%%%%% Build $dir/ref/Test$i-groovy"
    cd Test$i-groovy
    if ant 2>&1; then
	echo "============ Build $dir/ref/ Test$i groovy: Success"
    else
	echo "============ Build $dir/ref/ Test$i groovy: Fail"
    fi
    cd ..

    echo "%%%%%%%%%%%%% Build $dir/ref/Test$i-raw"
    cd Test$i-raw
    if ant 2>&1; then
	echo "============ Build $dir/ref/ Test$i raw: Success"
    else
	echo "============ Build $dir/ref/ Test$i raw: Fail"
    fi 
    cd ..
done
