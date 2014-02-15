#files="TipCalculator TipCalculator3"
files="TipCalculator2"

mkdir gen/Platform.iOS-Ref
mkdir gen/Platform.Android-Ref
for f in $files
do
    #mkdir gen/Platform.iOS-Ref/$f
    #mkdir gen/Platform.Android-Ref/$f

    devbin/appbuilder -nodate test/Tutorials/$f.madl test/org-ios-format1.properties
    cp -R gen/Platform.iOS/$f gen/Platform.iOS-Ref/

    devbin/appbuilder -nodate test/Tutorials/$f.madl test/org-android-format1.properties
    cp -R gen/Platform.Android/$f gen/Platform.Android-Ref/
done
