$(info >>>>>>>>>>==========================================build BasicUtils==========================================<<<<<<<<<<)
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
LOCAL_MODULE := BasicUtils
LOCAL_CERTIFICATE := platform
ALL_DEFAULT_INSTALLED_MODULES += $(LOCAL_PACKAGE_NAME)

LOCAL_SRC_FILES := $(call all-subdir-java-files)
LOCAL_STATIC_JAVA_LIBRARIES := libhttp
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := libhttp:libs/org.apache.http.legacy.jar
include $(BUILD_STATIC_JAVA_LIBRARY)
