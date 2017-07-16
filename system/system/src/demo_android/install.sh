#Install the apk

#cd into the home dir - this way it works when run from inside vim or any other folder
cd ~/system/src/demo_android/

#Remove the Old
if [ -f /sdcard/demo_android_signed.apk ];then
	rm /sdcard/demo_android_signed.apk
fi

#Only works if APK is on the sdcard
cp ./dist/demo_android_signed.apk /sdcard/

#Now try and view it..
am start -a android.intent.action.VIEW -t application/vnd.android.package-archive -d file:///sdcard/demo_android_signed.apk
