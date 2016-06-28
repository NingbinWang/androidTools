# Copyright 2006 The Android Open Source Project

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := debug eng

LOCAL_SRC_FILES:= serial_client.c

LOCAL_SHARED_LIBRARIES := libutils libcutils

# LOCAL_CFLAGS := -D_GNU_SOURCE 
LOCAL_MODULE:= serial_client

include $(BUILD_EXECUTABLE)

