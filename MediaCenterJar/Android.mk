$(info >>>>>>>>>>==========================================build MediaCenterJar==========================================<<<<<<<<<<)
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
LOCAL_MODULE := MediaCenterJar
LOCAL_CERTIFICATE := platform
ALL_DEFAULT_INSTALLED_MODULES += $(LOCAL_PACKAGE_NAME)

LOCAL_SRC_FILES := $(call all-subdir-java-files)
#LOCAL_SRC_FILES += src/com/rockchips/mediacenter/aidl/IDlnaFileShareService.aidl

LOCAL_STATIC_JAVA_LIBRARIES := BasicUtils HiMediaPlayer
#LOCAL_JAVA_LIBRARIES := HiMediaPlayer

include $(BUILD_STATIC_JAVA_LIBRARY)
