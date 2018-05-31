adb uninstall com.duy.compiler.javanide
adb install -r app-release.apk
adb shell am start -n "com.duy.compiler.javanide/com.duy.ide.activities.SplashScreenActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER
exit