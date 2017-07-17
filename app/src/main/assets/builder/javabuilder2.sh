#!/usr/bin/env bash
# PROJECT_PATH=""
# PROJECT_NAME=""
# MAIN_CLASS=""
# PATH_MAIN_CLASS=""
# ROOT_PACKAGE=""

clear
echo Java NIDE

cd ~
cd ${PROJECT_PATH}

#Clean up
rm -rf build/*
rm -rf bin/*

#cd to src dir
cd src/main/java

#Now compile - note the use of a seperate lib (in non-dex format!)
echo Compile the java code
javac -verbose -d ../../../build/ ${PATH_MAIN_CLASS}.java

#Now into build dir
cd ../../../build/

#Now convert to dex format (need no-strict)
echo Now convert to dex format
#dx --dex --verbose --no-strict --output=../bin/${PROJECT_NAME}.jar
dx --dex --verbose --no-strict --output=../bin/${PROJECT_NAME}.jar ${ROOT_PACKAGE}

#Back out
cd ..
java -jar ./bin/${PROJECT_NAME}.jar ${MAIN_CLASS}

