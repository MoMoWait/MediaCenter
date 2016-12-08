$(info >>>>>>>>>>=================================build prebuild mediacenterapp's libs===================================<<<<<<<<<<)
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := universal-image-loader-1.8.4.jar libxutil:xutils.jar libandroidutil:androidutils.jar libsupportv4:supportv4.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += cdi-api.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += http-2.2.1.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += httpcore-4.2.3.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += javax.annotation_1.0.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += javax.inject.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += javax.servlet-3.0.0.v201103241009.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += jetty-client-8.1.9.v20130131.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += jetty-continuation-8.1.9.v20130131.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += jetty-http-8.1.9.v20130131.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += jetty-io-8.1.9.v20130131.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += jetty-security-8.1.9.v20130131.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += jetty-server-8.1.9.v20130131.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += jetty-servlet-8.1.9.v20130131.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += jetty-util-8.1.9.v20130131.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += seamless-http-1.0-alpha2.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += seamless-util-1.0-alpha2.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += seamless-xml-1.0-alpha2.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += slf4j-api-1.6.1.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += slf4j-jdk14-1.6.1.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += httpclient-4.2.2.jar
include $(BUILD_MULTI_PREBUILT)