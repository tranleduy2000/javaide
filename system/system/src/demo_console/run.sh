#Run script for the demo console app

#cd into the home directory - so you can run the script from vim or anywhere else
cd ~/system/src/demo_console/

#Notice that the DEX version of the .jar is used with java
java -jar ./dist/demo_console.dex.jar:./libs/demolib.dex.jar org.demo.start

