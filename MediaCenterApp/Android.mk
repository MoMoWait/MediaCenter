$(info >>>>>>>>>>==========================================build MediaCenterApp==========================================<<<<<<<<<<)
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
LIBS_PATH := $(LOCAL_PATH)
LOCAL_MODULE_TAGS := optional
LOCAL_PACKAGE_NAME := MediaCenter
LOCAL_CERTIFICATE := platform
#LOCAL_DEX_PREOPT := false
ALL_DEFAULT_INSTALLED_MODULES += $(LOCAL_PACKAGE_NAME)

LOCAL_SRC_FILES := $(call all-subdir-java-files)
#ViewUtils_res_dir := ../ViewUtils/res
#res_dir := res $(ViewUtils_res_dir)
#LOCAL_RESOURCE_DIR := $(addprefix $(LOCAL_PATH)/, $(res_dir))
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/res
LOCAL_AAPT_FLAGS := --auto-add-overlay
LOCAL_STATIC_JAVA_LIBRARIES := Cling
LOCAL_STATIC_JAVA_LIBRARIES += universal-image-loader-1.8.4
LOCAL_STATIC_JAVA_LIBRARIES += android-support-v4
LOCAL_STATIC_JAVA_LIBRARIES += libxutil
LOCAL_STATIC_JAVA_LIBRARIES += libandroidutil
LOCAL_STATIC_JAVA_LIBRARIES += mozilla_chardet
LOCAL_STATIC_JAVA_LIBRARIES += jcifs-1.3.18
LOCAL_STATIC_JAVA_LIBRARIES += org.apache.http.legacy
LOCAL_PROGUARD_ENABLED := disabled
#LOCAL_JNI_SHARED_LIBRARIES := libmediacenter-jni
include $(BUILD_PACKAGE)
#include $(LOCAL_PATH)/jni/Android.mk
include $(LIBS_PATH)/libs/Common.mk
#include $(call all-makefiles-under,$(LOCAL_PATH))


