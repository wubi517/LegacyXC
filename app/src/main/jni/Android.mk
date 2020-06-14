LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := notifications
LOCAL_SRC_FILES := notifications.c

include $(BUILD_SHARED_LIBRARY)