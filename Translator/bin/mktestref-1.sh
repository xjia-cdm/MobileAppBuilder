#for file in test/groovy/trans/*.groovy

dir=test/groovy/trans
for i in Closure01 Closure02 Numbers01 Properties01 Returns01 Returns02 Strings01 Dependencies01 Dependencies02 Dependencies03 Methods01 Methods02 Methods03 Methods04 Methods05 Constructor01 Types Types01 Types02 Types03 Types04 Types05 Static01 Static02 Constants01 NonBinary DynamicReturns Polymorphism01 Polymorphism02 Operators01 Operators02 Operators03
do
    echo "Generate reference for $dir/Test$i.groovy"
    echo "bin/gt -output=ref/Test$i-java -header=no $dir/Test$i.groovy"
    bin/gt -output=ref/Test$i-java -header=no $dir/Test$i.groovy

    echo "bin/gt -output=ref/Test$i-objc -target=objc -header=no $dir/Test$i.groovy"
    bin/gt -output=ref/Test$i-objc -target=objc -header=no $dir/Test$i.groovy

    echo "bin/gt -output=ref/Test$i-groovy -target=groovy -header=no $dir/Test$i.groovy"
    bin/gt -output=ref/Test$i-groovy -target=groovy -header=no $dir/Test$i.groovy

    echo "bin/gt -output=ref/Test$i-raw -target=raw -header=no $dir/Test$i.groovy"
    bin/gt -output=ref/Test$i-raw -target=raw -header=no $dir/Test$i.groovy
done


