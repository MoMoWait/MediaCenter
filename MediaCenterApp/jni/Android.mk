LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
 
#LOCAL_MODULE_TAGS := samples
LOCAL_MODULE_TAGS := optional

# This is the target being built.
LOCAL_MODULE:= libmediacenter-jni
 
LOCAL_DEFAULT_CPP_EXTENSION := cpp
 
# All of the source files that we will compile.
LOCAL_SRC_FILES := native.cpp
 
#LOCAL_SHARED_LIBRARIES := \
    libutils liblog libmedia_jni libmedia libbinder libnativehelper
 
LOCAL_SHARED_LIBRARIES := \
    libutils liblog libmedia libbinder libnativehelper
LOCAL_C_INCLUDES += \
    $(JNI_H_INCLUDE)

LOCAL_PRELINK_MODULE := false 
 
include $(BUILD_SHARED_LIBRARY)
