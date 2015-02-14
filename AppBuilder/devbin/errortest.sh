#files="test01 test02 test03 test04 test05 test06 test07 test10 test11 test12 test13"
files="test01 test02 test03 test05 test10 test11 test12 test13 nofile"
#files="test03"
#files="test12"

for f in $files
do
    devbin/appbuilder -nodate test/$f.madl test/org-ios.properties
done