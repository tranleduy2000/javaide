clear
echo Java NIDE

#cd into the home directory - so you can run the script from vim or anywhere else
cd ~

PROJECT_PATH=""
PROJECT_NAME=""
MAIN_CLASS=""
PATH_MAIN_CLASS=""

cd ${PROJECT_PATH}

#Clean up
rm -rf build/*
rm -rf bin/*

#cd to src/java/main dir
cd src/main/java

#Now compile - note the use of a seperate lib (in non-dex format!)
echo Compile the java code
javac -verbose -d ../../../build/ ${PATH_MAIN_CLASS}

#Now into build dir
cd ../../../build/

#Now convert to dex format (need no-strict)
echo Now convert to dex format
dx --dex --verbose --no-strict --output=../bin/${PROJECT_NAME}.dex.jar ${PATH_MAIN_CLASS}.class

#Back out
cd ..

java -jar ./bin/${PROJECT_NAME}.dex.jar ${MAIN_CLASS}

