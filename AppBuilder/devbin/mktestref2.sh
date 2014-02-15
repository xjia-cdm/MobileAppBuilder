#files="Hello Widgets Actions Actions2"
files="Table Tabs Navigation Navigation2"

mkdir gen/Platform.iOS-Ref
mkdir gen/Platform.Android-Ref
for f in $files
do
    #mkdir gen/Platform.iOS-Ref/$f
    #mkdir gen/Platform.Android-Ref/$f

    devbin/appbuilder -nodate test/Tutorials/$f.madl test/org-ios.properties
    cp -R gen/Platform.iOS/$f gen/Platform.iOS-Ref/

    devbin/appbuilder -nodate test/Tutorials/$f.madl test/org-android.properties
    cp -R gen/Platform.Android/$f gen/Platform.Android-Ref/
done
