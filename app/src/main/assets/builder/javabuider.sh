clear
echo Java NIDE

PROJECT_PATH=""
PROJECT_NAME=""
MAIN_CLASS=""
PATH_MAIN_CLASS=""

cd ~
cd ${PROJECT_PATH}

#Clean up
rm -rf build/*
rm -rf bin/*

#cd to src/java/main dir
cd src/main/java

#Now compile - note the use of a seperate lib (in non-dex format!)
echo Compile the java code
javac -verbose -cp -d ../../../build/ ${PATH_MAIN_CLASS}.java

#Now into build dir
cd ../../../build/

#Now convert to dex format (need no-strict)
echo Now convert to dex format
dx --dex --verbose --output=../bin/${PROJECT_NAME}.jar ${PATH_MAIN_CLASS}.class

#Back out
cd ..
java -jar ./bin/${PROJECT_NAME}.jar ${MAIN_CLASS}

///////////
javac -verbose -cp ../libs/demolib.jar -d ../build/ main/java/${PATH_MAIN_CLASS}.java
