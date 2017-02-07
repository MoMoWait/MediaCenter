$(info >>>>>>>>>>================================build MediaCenter's all projects================================<<<<<<<<<<)
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

#include $(call all-makefiles-under,$(LOCAL_PATH))

mk1:= $(LOCAL_PATH)/BasicUtils/Common.mk
mk5:= $(LOCAL_PATH)/MediaCenterApp/Android.mk
mk6:= $(LOCAL_PATH)/MediaCenterJar/Common.mk
mk7:= $(LOCAL_PATH)/ViewUtils/Common.mk
mk8:= $(LOCAL_PATH)/hisijar/hibdinfo/Common.mk
mk9:= $(LOCAL_PATH)/hisijar/himediaplayer/Common.mk
include $(mk1) $(mk5) $(mk6) $(mk7) $(mk8) $(mk9)
