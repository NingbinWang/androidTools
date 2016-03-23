LOCAL_PATH:= $(call my-dir)


include $(CLEAR_VARS)
LOCAL_SHARED_LIBRARIES := liblog libcutils 
LOCAL_SRC_FILES:= logcommand.cpp
LOCAL_MODULE:= logcommand
LOCAL_MODULE_TAGS:= optional

include $(BUILD_EXECUTABLE)

include $(CLEAR_VARS)
LOCAL_SHARED_LIBRARIES := liblog libcutils 
LOCAL_SRC_FILES:= dumpsys.c
LOCAL_MODULE:= libdumpsys_logtool
LOCAL_MODULE_TAGS:= optional

include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_SHARED_LIBRARIES := liblog libcutils 
ifeq "18" "$(PLATFORM_SDK_VERSION)"
LOCAL_SRC_FILES:= asuslogcat.cpp  logtool.cpp event4_3.logtags
else
LOCAL_SRC_FILES:= asuslogcat.cpp  logtool.cpp event4_4.logtags
endif
LOCAL_MODULE:= asuslogcat
LOCAL_MODULE_TAGS:= optional
ifeq "18" "$(PLATFORM_SDK_VERSION)"
LOCAL_CFLAGS  += -DBUILD_LOGCAT_4_3
endif
include $(BUILD_EXECUTABLE)

include $(CLEAR_VARS)
LOCAL_SHARED_LIBRARIES := liblog libcutils
LOCAL_SRC_FILES:= dumpkmsg.cpp logtool.cpp
LOCAL_MODULE:= logkmsg
LOCAL_MODULE_TAGS:= optional

include $(BUILD_EXECUTABLE)


include $(CLEAR_VARS)
LOCAL_SHARED_LIBRARIES := liblog libcutils 
LOCAL_SRC_FILES:= logdumps.cpp logtool.cpp
LOCAL_MODULE:= dumps
LOCAL_MODULE_TAGS:= optional

include $(BUILD_EXECUTABLE)
