NDK_TOOLCHAIN_VERSION := 4.9
APP_OPTIM    := debug
APP_STL      := gnustl_static
APP_ABI      := armeabi-v7a armeabi x86 x86_64 arm64-v8a
APP_CPPFLAGS += -std=gnu++11
APP_PLATFORM := android-18