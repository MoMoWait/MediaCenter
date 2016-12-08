$(info >>>>>>>>>>==========================================build MediaCenterApp==========================================<<<<<<<<<<)
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
LOCAL_PACKAGE_NAME := MediaCenter
LOCAL_CERTIFICATE := platform
ALL_DEFAULT_INSTALLED_MODULES += $(LOCAL_PACKAGE_NAME)

LOCAL_SRC_FILES := $(call all-subdir-java-files)
ViewUtils_res_dir := ../ViewUtils/res
res_dir := res $(ViewUtils_res_dir)
LOCAL_RESOURCE_DIR := $(addprefix $(LOCAL_PATH)/, $(res_dir))
LOCAL_AAPT_FLAGS := --auto-add-overlay
LOCAL_AAPT_FLAGS += --extra-packages com.rockchips.mediacenter.viewutils

LOCAL_STATIC_JAVA_LIBRARIES := BasicUtils MediaCenterJar ViewUtils AirsharingJAR
LOCAL_STATIC_JAVA_LIBRARIES += universal-image-loader-1.8.4
#LOCAL_STATIC_JAVA_LIBRARIES += CustomizedJar
LOCAL_STATIC_JAVA_LIBRARIES += HiBDInfo
LOCAL_STATIC_JAVA_LIBRARIES += HiMediaPlayer
LOCAL_STATIC_JAVA_LIBRARIES += libxutil
LOCAL_STATIC_JAVA_LIBRARIES += libandroidutil
LOCAL_STATIC_JAVA_LIBRARIES += libsupportv4
LOCAL_STATIC_JAVA_LIBRARIES += cdi-api
LOCAL_STATIC_JAVA_LIBRARIES += http-2.2.1
LOCAL_STATIC_JAVA_LIBRARIES += httpcore-4.2.3
LOCAL_STATIC_JAVA_LIBRARIES += javax.annotation_1.0
LOCAL_STATIC_JAVA_LIBRARIES += javax.inject
LOCAL_STATIC_JAVA_LIBRARIES += javax.servlet-3.0.0.v201103241009
LOCAL_STATIC_JAVA_LIBRARIES += jetty-client-8.1.9.v20130131
LOCAL_STATIC_JAVA_LIBRARIES += jetty-continuation-8.1.9.v20130131
LOCAL_STATIC_JAVA_LIBRARIES += jetty-http-8.1.9.v20130131
LOCAL_STATIC_JAVA_LIBRARIES += jetty-io-8.1.9.v20130131
LOCAL_STATIC_JAVA_LIBRARIES += jetty-security-8.1.9.v20130131
LOCAL_STATIC_JAVA_LIBRARIES += jetty-server-8.1.9.v20130131
LOCAL_STATIC_JAVA_LIBRARIES += jetty-servlet-8.1.9.v20130131
LOCAL_STATIC_JAVA_LIBRARIES += jetty-util-8.1.9.v20130131
LOCAL_STATIC_JAVA_LIBRARIES += seamless-http-1.0-alpha2
LOCAL_STATIC_JAVA_LIBRARIES += seamless-util-1.0-alpha2
LOCAL_STATIC_JAVA_LIBRARIES += seamless-xml-1.0-alpha2
LOCAL_STATIC_JAVA_LIBRARIES += slf4j-api-1.6.1
LOCAL_STATIC_JAVA_LIBRARIES += slf4j-jdk14-1.6.1
LOCAL_STATIC_JAVA_LIBRARIES += httpclient-4.2.2
LOCAL_PROGUARD_ENABLED := disabled

include $(BUILD_PACKAGE)

include $(LOCAL_PATH)/libs/Common.mk
