#!/usr/bin/env bash
# PROJECT_PATH=""
# PROJECT_NAME=""
# MAIN_CLASS=""
# PATH_MAIN_CLASS=""
# ROOT_PACKAGE=""
ERROR_FILE="../../../build/error.txt"

clear
echo Java NIDE

cd ~
cd ${PROJECT_PATH}

#Clean up
rm -rf build/*
rm -rf bin/*

#cd to src dir
cd src/main/java

#Now compile - note the use of a separate lib (in non-dex format!), output error in stderr
echo Compile the java code
#javac -verbose -d ../../../build/ ${PATH_MAIN_CLASS}.java 2> ../../../build/error.txt
javac -d ../../../build/ ${PATH_MAIN_CLASS}.java 2> ${ERROR_FILE}

error_msg=$(< ${ERROR_FILE})

if [ -n "${error_msg}" ]; then

    #clear screen
    clear
    echo -e "\033[31mCompiled with error\e[0m"

    # print error msg
    cat ${ERROR_FILE}

else

    #Now into build dir
    cd ../../../build/

    #Now convert to dex format (need no-strict)
    echo Now convert to dex format
    dx --dex --verbose --no-strict --output=../bin/${PROJECT_NAME}.dex.jar ${ROOT_PACKAGE}

    #clear screen
    clear

    #Back out
    cd ..

    #exec jar file
    java -jar ./bin/${PROJECT_NAME}.dex.jar ${MAIN_CLASS}

fi