$(info >>>>>>>>>>================================build MediaCenter's all projects================================<<<<<<<<<<)
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

#include $(call all-makefiles-under,$(LOCAL_PATH))

mk1:= $(LOCAL_PATH)/BasicUtils/Common.mk
mk2:= $(LOCAL_PATH)/CustomizedJar/Common.mk
mk3:= $(LOCAL_PATH)/DLNAService/Common.mk
mk4:= $(LOCAL_PATH)/LocalScanService/Common.mk
mk5:= $(LOCAL_PATH)/MediaCenterApp/Common.mk
mk6:= $(LOCAL_PATH)/MediaCenterJar/Common.mk
mk7:= $(LOCAL_PATH)/ViewUtils/Common.mk
mk8:= $(LOCAL_PATH)/hisijar/hibdinfo/Common.mk
mk9:= $(LOCAL_PATH)/hisijar/himediaplayer/Common.mk
#include $(mk8) $(mk9)
include $(mk1) $(mk2) $(mk3) $(mk4)  $(mk5) $(mk6) $(mk7) $(mk8) $(mk9)
#include $(mk1) $(mk2) $(mk4) $(mk5) $(mk6) $(mk7)
