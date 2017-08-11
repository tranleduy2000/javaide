adb uninstall com.duy.compiler.javanide
adb install -r app-prod-release.apk
clear
echo success.
adb shell am start -n "com.duy.compiler.javanide/com.duy.ide.activities.ActivitySplashScreen" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER