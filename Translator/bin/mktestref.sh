dir=test/groovy/trans

for i in Types
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
