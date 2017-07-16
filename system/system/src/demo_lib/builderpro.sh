#Build script for this java project that uses proguard

#cd into the main folder
cd ~/system/src/demo_lib/

#The main class
mainclass=org/library/libfunc.java

#Clean up
echo Clean Up
rm -rf ./build/*
rm -rf ./dist/*

#First cd into the source directory and build the main class
cd src

#Now compile
echo Compile the Library first
javac -verbose -d ../build/ $mainclass

#Back out
cd ..

#Convert to Normal and DEX format..
cd build

#First create a normal jar file
echo Create normal jar - for use with javac
jar -v ../dist/demolib.jar org

# Now use proguard
cd ..
proguard @proguard.cfg

#Start from the root folder
echo Convert to DEX format - for use with java
dx --dex --verbose --no-strict --output=./dist/demolib.dex.jar ./dist/demolib.pro.jar

#Back out
cd ..
