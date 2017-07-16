#Build script for Android with proguard

#cd into the home dir
export PROJECTHOME=~/system/src/demo_android
cd $PROJECTHOME

#Clean up
rm -rf build
rm -rf dist

#create the needed directories
mkdir -m 770 -p dist
mkdir -m 770 -p build/classes

#Rmove the R.java file as will be created by aapt
rm src/org/me/androiddemo/R.java

#Now use aapt
echo Create the R.java file
aapt p -f -v -M AndroidManifest.xml -F ./build/resources.res -I ~/system/classes/android.jar -S res/ -J src/org/me/androiddemo

#cd into the src dir
cd src

#Now compile - note the use of a seperate lib (in non-dex format!)
echo Compile the java code
javac -verbose -cp ../libs/demolib.jar -d ../build/classes org/me/androiddemo/MainActivity.java

#Back out
cd $PROJECTHOME

#Now into build dir
cd build/classes/

# Create a normal jar
jar -v ../demo_android.jar org

# And now use proguard
cd $PROJECTHOME
proguard @proguard.cfg

#Convert to dex format (--no-strict not required) (Notice demolib.jar at the end - non-dex format)
echo Now convert to dex format
dx --dex --verbose --output=./build/demo_android.dex ./build/demo_android.pro.jar ./libs/demolib.jar

#Back out
cd $PROJECTHOME

#And finally - create the .apk
apkbuilder ./dist/demo_android.apk -v -u -z ./build/resources.res -f ./build/demo_android.dex

#And now sign it
cd dist
signer demo_android.apk demo_android_signed.apk

cd $PROJECTHOME
