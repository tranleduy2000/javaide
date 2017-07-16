#Simple Build Script

#Clean up
echo Clean up
if [ -f hello.class ]; then
	rm hello.class
fi

if [ -f hello.jar ];then
	rm hello.jar
fi

#First compile hello.java
echo First compile the java 
javac -verbose hello.java

#Now convert to dex file
echo Now convert to dex format
dx --dex --verbose --output=hello.jar ./hello.class


