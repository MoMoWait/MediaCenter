
LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
#LOCAL_USE_AAPT2 := true
#LOCAL_JAR_EXCLUDE_FILES := none
LOCAL_MODULE_TAGS := optional
LOCAL_STATIC_JAVA_LIBRARIES := dlna
LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_SRC_FILES += \
    src/com/rockchip/mediacenter/core/config/mediaMime.xml \
    src/com/rockchip/mediacenter/ISystemDeviceService.aidl \
    src/com/rockchip/mediacenter/dlna/IMediaCenterService.aidl
LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res
LOCAL_PROGUARD_ENABLED := disabled
LOCAL_MODULE := HomeMedia
#LOCAL_PACKAGE_NAME := HomeMedia
LOCAL_CERTIFICATE := platform
include $(BUILD_STATIC_JAVA_LIBRARY) 
#include $(BUILD_JAVA_LIBRARY) 
#include $(BUILD_PACKAGE) 

##################################################
include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := dlna:libs/mediacenter.jar
include $(BUILD_MULTI_PREBUILT)

# Use the folloing include to make our test apk.
#include $(call all-makefiles-under,$(LOCAL_PATH))

