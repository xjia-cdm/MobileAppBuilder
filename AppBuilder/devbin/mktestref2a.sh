files="Widgets"

mkdir gen/Platform.iOS-Ref
mkdir gen/Platform.Android-Ref
for f in $files
do
    #mkdir gen/Platform.iOS-Ref/$f
    #mkdir gen/Platform.Android-Ref/$f

    #devbin/appbuilder -nodate test/Tutorials/$f.madl test/org-ios.conf
    #cp -R gen/Platform.iOS/$f gen/Platform.iOS-Ref/

    devbin/appbuilder -nodate -d DesignOpt test/Tutorials/$f.madl test/org-android-radiogroup.conf
    cp -R gen/Platform.Android/DesignOpt/$f gen/Platform.Android-DesignOpt-Ref/
done
