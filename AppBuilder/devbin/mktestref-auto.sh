files="app01 app01a app01f app01g"

mkdir gen/Platform.iOS-AutoLayout-Ref
for f in $files
do
    devbin/appbuilder -nodate -d AutoLayout test/$f.madl test/org-autolayout.properties
    cp -R gen/Platform.iOS/AutoLayout/$f gen/Platform.iOS-AutoLayout-Ref/
done