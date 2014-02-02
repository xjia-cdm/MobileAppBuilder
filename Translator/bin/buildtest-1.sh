#for file in test/groovy/trans/*.groovy

dir=test/groovy/trans
cd $dir/ref
for i in Closure01 Closure02 Numbers01 Properties01 Returns01 Returns02 Strings01 Dependencies01 Dependencies02 Dependencies03 Methods01 Methods02 Methods03 Methods04 Methods05 Constructor01 Types Types01 Types02 Types03 Types04 Types05 Static01 Static02 Constants01 NonBinary DynamicReturns Polymorphism01 Polymorphism02 Operators01 Operators02 Operators03
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
