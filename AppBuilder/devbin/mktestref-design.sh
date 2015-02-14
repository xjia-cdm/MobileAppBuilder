files1="app02"
files2="app07"

for f in $files1
do
    devbin/appbuilder -d DesignOpt -nodate test/$f.madl test/org-android-radiogroup.conf
    cp -R gen/Platform.Android/DesignOpt/$f gen/Platform.Android-DesignOpt-Ref/
done

for f in $files2
do
    devbin/appbuilder -nodate -d DesignOpt test/$f.madl test/org-android-expandable.conf
    cp -R gen/Platform.Android/DesignOpt/$f gen/Platform.Android-DesignOpt-Ref/
done