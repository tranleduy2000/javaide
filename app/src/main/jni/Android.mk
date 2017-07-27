#
# Copyright (C) 2008 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# This makefile supplies the rules for building a library of JNI code for
# use by our example of how to bundle a shared library with an APK.

LOCAL_PATH:= $(call my-dir)

#include $(CLEAR_VARS)
#LOCAL_MODULE := android_aapt
#LOCAL_SRC_FILES := $(LOCAL_PATH)/tools/aapt/AaptAssets.cpp \
#                   $(LOCAL_PATH)/tools/aapt/AaptConfig.cpp \
#                   $(LOCAL_PATH)/tools/aapt/AaptUtil.cpp \
#                   $(LOCAL_PATH)/tools/aapt/AaptXml.cpp \
#                   $(LOCAL_PATH)/tools/aapt/ApkBuilder.cpp \
#                   $(LOCAL_PATH)/tools/aapt/Command.cpp \
#                   $(LOCAL_PATH)/tools/aapt/CrunchCache.cpp \
#                   $(LOCAL_PATH)/tools/aapt/FileFinder.cpp \
#                   $(LOCAL_PATH)/tools/aapt/Images.cpp \
#                   $(LOCAL_PATH)/tools/aapt/Package.cpp \
#                   $(LOCAL_PATH)/tools/aapt/pseudolocalize.cpp \
#                   $(LOCAL_PATH)/tools/aapt/Resource.cpp \
#                   $(LOCAL_PATH)/tools/aapt/ResourceFilter.cpp \
#                   $(LOCAL_PATH)/tools/aapt/ResourceIdCache.cpp \
#                   $(LOCAL_PATH)/tools/aapt/ResourceTable.cpp \
#                   $(LOCAL_PATH)/tools/aapt/SourcePos.cpp \
#                   $(LOCAL_PATH)/tools/aapt/StringPool.cpp \
#                   $(LOCAL_PATH)/tools/aapt/WorkQueue.cpp \
#                   $(LOCAL_PATH)/tools/aapt/XMLNode.cpp \
#                   $(LOCAL_PATH)/tools/aapt/ZipEntry.cpp \
#                   $(LOCAL_PATH)/tools/aapt/ZipFile.cpp
#LOCAL_LDLIBS := -ldl -llog
#include $(BUILD_SHARED_LIBRARY)


include $(CLEAR_VARS)
LOCAL_MODULE:= libjackpal-androidterm2
LOCAL_SRC_FILES:=  termExec.cpp
LOCAL_LDLIBS := -ldl -llog
include $(BUILD_SHARED_LIBRARY)

