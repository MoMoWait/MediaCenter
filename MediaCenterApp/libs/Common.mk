$(info >>>>>>>>>>=================================build prebuild mediacenterapp's libs===================================<<<<<<<<<<)
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := universal-image-loader-1.8.4.jar libxutil:xutils.jar libandroidutil:androidutils.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += mozilla_chardet.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += jcifs-1.3.18.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += org.apache.http.legacy.jar
include $(BUILD_MULTI_PREBUILT)
