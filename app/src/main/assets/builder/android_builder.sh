#!/usr/bin/env bash
#Build script

#Gobal variable
project_path=""
project_name=""
main_class=""
main_class_path=""
root_package=""
package="com.duy.javanide"
package_path="com/duy/javanide"

cd ~
cd ${project_path}

#Clean up
rm -rf build
rm -rf bin

#create the needed directories
mkdir -m 770 -p bin
mkdir -m 770 -p build/classes

#Rmove the R.java file as will be created by aapt
rm src/main/java/${package_path}/R.java

#Now use aapt
echo Create the R.java file
aapt p -f -v -M AndroidManifest.xml -F ./build/resources.res -I ~/system/classes/android.jar -S res/ -J src/main/java/${package_path}

#cd into the src dir
cd src

#Now compile - note the use of a seperate lib (in non-dex format!)
echo Compile the java code
#javac -verbose -cp ../libs/demolib.jar -d ../build/classes org/me/androiddemo/MainActivity.java
javac -verbose -d ../build/classes ${main_class_path}.java

#Back out
cd ..

#Now into build dir
cd build/classes/

#Now convert to dex format (need --no-strict) (Notice demolib.jar at the end - non-dex format)
echo Now convert to dex format
#dx --dex --verbose --no-strict --output=../${project_name}.dex org ../../libs/demolib.jar
dx --dex --verbose --no-strict --output=../${project_name}.dex ${root_package}

#Back out
cd ../..

#And finally - create the .apk
apkbuilder ./bin/${project_name}.apk -v -u -z ./build/resources.res -f ./build/${project_name}.dex

#And now sign it
cd bin
signer ${project_name}.apk ${project_name}_signed.apk

cd ..

