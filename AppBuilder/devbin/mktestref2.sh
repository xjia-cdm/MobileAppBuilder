#files="Hello Widgets Actions Actions2"
#files="Tabs Navigation Navigation2"

#files="ImplicitActions Form Widgets"
#files="FormAction"
#files="WorldCities EuropeanCountries EuropeanUnion"
#files="MultiViews MultiViews2 ListViews"

#files="EuropeanCountries"
#files="EuropeanUnion"

#files="Navigation Navigation2"

files="Actions EuropeanCountries EuropeanUnion Form FormAction Hello ImplicitActions Navigation Navigation2 Tabs Widgets WorldCities MultiViews MultiViews2 ListViews"

mkdir gen/Platform.iOS-Ref
mkdir gen/Platform.Android-Ref
for f in $files
do
    #mkdir gen/Platform.iOS-Ref/$f
    #mkdir gen/Platform.Android-Ref/$f

    devbin/appbuilder -nodate test/Tutorials/$f.madl test/org-ios.conf
    cp -R gen/Platform.iOS/$f gen/Platform.iOS-Ref/

    devbin/appbuilder -nodate test/Tutorials/$f.madl test/org-android.conf
    cp -R gen/Platform.Android/$f gen/Platform.Android-Ref/
done
