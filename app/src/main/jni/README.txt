To build complete aapt binaries:
1. Adjust ndk-build-aaptcomplete.sh to call the ndk-build script of the Android NDK
2. Look near the bottom of /jni/Android.mk. Uncomment "include $(BUILD_EXECUTABLE)" and comment out "include $(BUILD_SHARED_LIBRARY)".
3.  Determine whether you need the PIE or non-PIE binaries.  If you need PIE, there are two lines to uncomment at the bottom of jni/Android.mk, and one in jni/Application.mk.
4. Run ndk-build-aaptcomplete.sh.
5. Binaries can be found in /libs/armeabi/ and /libs/x86