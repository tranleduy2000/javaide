#Build script that uses proguard

#cd into the home directory
cd ~/system/src/demo_console/

#Clean up
rm -rf build/*
rm -rf dist/*

#First cd into the src dir
cd src

#Now compile - note the use of a seperate lib (in non-dex format!)
echo Compile the java code
javac -verbose -cp ../libs/demolib.jar -d ../build/ org/demo/start.java

#Back out
cd ..

#Now into build dir
cd build

#First create a normal jar
echo Build a normal jar
jar -v ../dist/demo_console.jar org
cd ..

# Now create the proguarded version
# Checkout the proguard.cfg config settings
echo Now run proguard..
proguard @proguard.cfg

#Now convert the proguarded version to dex format (no-strict not required)
cd dist
echo Now convert to dex format
dx --dex --verbose --output=./demo_console.dex.jar ./demo_console.pro.jar

#Back out
cd ..
