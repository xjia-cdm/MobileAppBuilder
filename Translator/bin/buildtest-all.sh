
bin/buildtest-1.sh > buildtest-1.log
bin/buildtest-2.sh > buildtest-2.log

echo 
echo 'Build test results [Summary]'
cat buildtest-1.log | grep '==='
cat buildtest-2.log | grep '==='

cat buildtest-1.log | grep '===' > buildtest-summary.log
cat buildtest-2.log | grep '===' >> buildtest-summary.log

bin/buildreport.awk buildtest-summary.log
