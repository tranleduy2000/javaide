#cd into the home directory - so you can run the script from vim or anywhere else
cd ~/system/src/demo_console/

#Clean up
rm -rf build/*
rm -rf bin/*

#First cd into the src dir
cd src

#Now compile - note the use of a seperate lib (in non-dex format!)
echo Compile the java code
javac -verbose -d ../build/ org/demo/start.java

#Back out
cd ..

#Now into build dir
cd build

#Now convert to dex format (need no-strict)
echo Now convert to dex format
dx --dex --verbose --no-strict --output=../dist/demo_console.dex.jar org

#Back out
cd ..

javac -verbose -d ../../../build/ com/duy/Main.java
dx --dex --verbose --no-strict --output=../dist/demo_console.dex.jar org

cd ../../
cd build
