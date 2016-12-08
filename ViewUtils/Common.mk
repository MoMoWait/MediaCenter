$(info >>>>>>>>>>==========================================build ViewUtils==========================================<<<<<<<<<<)
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
LOCAL_MODULE := ViewUtils
LOCAL_CERTIFICATE := platform
ALL_DEFAULT_INSTALLED_MODULES += $(LOCAL_PACKAGE_NAME)

LOCAL_SRC_FILES := $(call all-subdir-java-files)
LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res

LOCAL_STATIC_JAVA_LIBRARIES := BasicUtils libandroidutil

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := libandroidutil:libs/androidutils.jar

include $(BUILD_STATIC_JAVA_LIBRARY)
