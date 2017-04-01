$(info >>>>>>>>>>================================build MediaCenter's all projects================================<<<<<<<<<<)
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
mk1:= $(LOCAL_PATH)/MediaCenterApp/Android.mk
mk2:= $(LOCAL_PATH)/Cling/Android.mk
include $(mk1) $(mk2)
