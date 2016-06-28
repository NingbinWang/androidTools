LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

#we must select the correct wireless.xx.h and cp to wireless.h
WE_VERSION:= $(shell sed -ne "/WE_VERSION/{s:\([^0-9]*\)::;p;q;}" $(LOCAL_PATH)/iwlib.h )
WEXT_HEADER:= wireless.$(WE_VERSION).h
$(shell cp $(LOCAL_PATH)/$(WEXT_HEADER) $(LOCAL_PATH)/wireless.h)

################## build iwlib ###################
include $(CLEAR_VARS)
LOCAL_SRC_FILES := iwlib.c
LOCAL_CFLAGS += -Wstrict-prototypes -Wmissing-prototypes -Wshadow -Wpointer-arith -Wcast-qual -Winline -MMD -fPIC
LOCAL_MODULE:= libiw
LOCAL_MODULE_TAGS := eng debug
include $(BUILD_STATIC_LIBRARY)

################## build iwlist ###################
include $(CLEAR_VARS)
LOCAL_SRC_FILES := iwlist.c iwlist.h
LOCAL_CFLAGS += -Wstrict-prototypes -Wmissing-prototypes -Wshadow -Wpointer-arith -Wcast-qual -Winline -MMD -fPIC
LOCAL_MODULE:= iwlist
LOCAL_MODULE_TAGS := eng debug
LOCAL_STATIC_LIBRARIES := libiw
LOCAL_MODULE_PATH := $(TARGET_OUT_OPTIONAL_EXECUTABLES) # install to system/xbin
include $(BUILD_EXECUTABLE) 

################## build iwpriv ###################
include $(CLEAR_VARS)
LOCAL_SRC_FILES := iwpriv.c iwpriv.h
LOCAL_CFLAGS += -Wstrict-prototypes -Wmissing-prototypes -Wshadow -Wpointer-arith -Wcast-qual -Winline -MMD -fPIC
LOCAL_MODULE:= iwpriv
LOCAL_MODULE_TAGS := eng debug
LOCAL_STATIC_LIBRARIES := libiw
LOCAL_MODULE_PATH := $(TARGET_OUT_OPTIONAL_EXECUTABLES) # install to system/xbin
include $(BUILD_EXECUTABLE) 

################## build iwspy ###################
include $(CLEAR_VARS)
LOCAL_SRC_FILES := iwspy.c iwspy.h
LOCAL_CFLAGS += -Wstrict-prototypes -Wmissing-prototypes -Wshadow -Wpointer-arith -Wcast-qual -Winline -MMD -fPIC
LOCAL_MODULE:= iwspy
LOCAL_MODULE_TAGS := eng debug
LOCAL_STATIC_LIBRARIES := libiw
LOCAL_MODULE_PATH := $(TARGET_OUT_OPTIONAL_EXECUTABLES) # install to system/xbin
include $(BUILD_EXECUTABLE) 

################## build iwgetid ###################
include $(CLEAR_VARS)
LOCAL_SRC_FILES := iwgetid.c iwgetid.h
LOCAL_CFLAGS += -Wstrict-prototypes -Wmissing-prototypes -Wshadow -Wpointer-arith -Wcast-qual -Winline -MMD -fPIC
LOCAL_MODULE:= iwgetid
LOCAL_MODULE_TAGS := eng debug
LOCAL_STATIC_LIBRARIES := libiw
LOCAL_MODULE_PATH := $(TARGET_OUT_OPTIONAL_EXECUTABLES) # install to system/xbin
include $(BUILD_EXECUTABLE) 

################## build iwevent ###################
include $(CLEAR_VARS)
LOCAL_SRC_FILES := iwevent.c iwevent.h
LOCAL_CFLAGS += -Wstrict-prototypes -Wmissing-prototypes -Wshadow -Wpointer-arith -Wcast-qual -Winline -MMD -fPIC
LOCAL_MODULE:= iwevent
LOCAL_MODULE_TAGS := eng debug
LOCAL_STATIC_LIBRARIES := libiw
LOCAL_MODULE_PATH := $(TARGET_OUT_OPTIONAL_EXECUTABLES) # install to system/xbin
include $(BUILD_EXECUTABLE) 

################## build ifrename ###################
include $(CLEAR_VARS)
LOCAL_SRC_FILES := ifrename.c ifrename.h
LOCAL_CFLAGS += -Wstrict-prototypes -Wmissing-prototypes -Wshadow -Wpointer-arith -Wcast-qual -Winline -MMD -fPIC
LOCAL_MODULE:= ifrename
LOCAL_MODULE_TAGS := eng debug
LOCAL_STATIC_LIBRARIES := libiw
LOCAL_MODULE_PATH := $(TARGET_OUT_OPTIONAL_EXECUTABLES) # install to system/xbin
include $(BUILD_EXECUTABLE) 

################## build macaddr ###################
include $(CLEAR_VARS)
LOCAL_SRC_FILES := macaddr.c macaddr.h
LOCAL_CFLAGS += -Wstrict-prototypes -Wmissing-prototypes -Wshadow -Wpointer-arith -Wcast-qual -Winline -MMD -fPIC
LOCAL_MODULE:= macaddr
LOCAL_MODULE_TAGS := eng debug
LOCAL_STATIC_LIBRARIES := libiw
LOCAL_MODULE_PATH := $(TARGET_OUT_OPTIONAL_EXECUTABLES) # install to system/xbin
include $(BUILD_EXECUTABLE) 
