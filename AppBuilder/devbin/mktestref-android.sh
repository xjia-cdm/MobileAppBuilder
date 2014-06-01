#files="android11a"
#files="android05a android05b android05c android06a"
#files="android05b android05c"
files="android02b"

mkdir gen/Platform.Android-Ref
for f in $files
do
    devbin/appbuilder -nodate test/$f.madl test/org-android.conf
    cp -R gen/Platform.Android/$f gen/Platform.Android-Ref/
done

