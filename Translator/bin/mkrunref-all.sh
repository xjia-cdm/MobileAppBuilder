bin/mkrunref-1.sh > runref-1.log
bin/mkrunref-2.sh > runref-2.log

echo 
echo 'Run test results (completion) [Summary]'
cat runref-1.log | grep '==='
cat runref-2.log | grep '==='

cat runref-1.log | grep '===' > runref-summary.log
cat runref-2.log | grep '===' >> runref-summary.log

bin/runreport.awk runref-summary.log



