bin/runtest-1.sh > runtest-1.log
bin/runtest-2.sh > runtest-2.log

echo 
echo 'Run test results (diff) [Summary]'
cat runtest-1.log | grep '==='
cat runtest-2.log | grep '==='

cat runtest-1.log | grep '===' > runtest-summary.log
cat runtest-2.log | grep '===' >> runtest-summary.log

bin/runreport.awk runtest-summary.log


