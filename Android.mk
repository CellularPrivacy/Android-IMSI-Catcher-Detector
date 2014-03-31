LOCAL_PATH:= $(call my-dir)

ifneq ($(TARGET_SIMULATOR),true)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_PACKAGE_NAME := Android-IMSI-Catcher-Detector
LOCAL_JAVA_LIBRARIES := telephony-common effects maps usb
LOCAL_STATIC_JAVA_LIBRARIES := android-support-v13 RootTools
LOCAL_PROGUARD_ENABLED := disabled
LOCAL_CERTIFICATE := platform

include $(BUILD_PACKAGE)

###########################################################
include $(CLEAR_VARS)

# XXX: manually create symlink to addon-google_apis-google-17
# ln -s ${ANDROID_HOME}/add-ons/addon-google_apis-google-17 addon-google_apis-google-17
GOOGLE_APIS_ADDON = addon-google_apis-google-17/libs

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := RootTools:libs/RootTools.jar
LOCAL_PREBUILT_JAVA_LIBRARIES := \
    effects:${GOOGLE_APIS_ADDON}/effects.jar \
    maps:${GOOGLE_APIS_ADDON}/maps.jar \
    usb:${GOOGLE_APIS_ADDON}/usb.jar

include $(BUILD_MULTI_PREBUILT)
############################################################

# Build the test package
include $(call all-makefiles-under,$(LOCAL_PATH))

endif # TARGET_SIMULATOR
