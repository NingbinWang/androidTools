LOCAL_PATH =device/asus/common/logtool
  
#Pay attention!!!! 
#logtool.makefile should be call inherit-product in lunch project makefile like
#$(call inherit-product, $(LOCAL_PATH)/logtool.mk) in device/qcom/msm8952_64/msm8952_64.mk


#PRODUCT_COPY_FILES += \
 #   device/qcom/common/asdf/asdf.img:asdf.img \
  #  device/qcom/common/splash/splash.bin:splash.bin

#logtool default property value

ADDITIONAL_DEFAULT_PROPERTIES += persist.asuslog.main.enable=1 \
         persist.asuslog.set.date=1 \
         persist.asuslog.kernel.enable=1 \
         persist.asuslog.radio.enable =1 \
         persist.asuslog.combine.config=31 \
         persist.asuslog.combine.enable=1 \
         persist.asuslog.events.enable=1 \
         persist.asuslog.events.enable=1 \
         persist.asuslog.fdccrash.enable=1\
         persist.asuslog.fac.init=1\
         persist.asus.logtool.pug=0\
	 persist.asuslog.logcat.enable=1\
	 persist.asuslog.logcatr.enable=1\
	 persist.asuslog.logcate.enable=1\
         persist.asus.kernelmessage=0 \
         persist.asus.savelogs=0 \
         persist.asus.startlog=0
#asus_bsp default disable modem crash cause ap reboot
ADDITIONAL_DEFAULT_PROPERTIES += persist.sys.ssr.modem=1





PRODUCT_PACKAGES += \
    serial_client \
    modem_country

#logtool copy files 
PRODUCT_COPY_FILES += \
    $(LOCAL_PATH)/fdc-logtool/assets/init_factorylogtool.sh:/system/etc/init_factorylogtool.sh \
    $(LOCAL_PATH)/fdc-logtool/assets/FT_SD_DIAG_ALL_WITH_AUDIO_NEW.CFG:/system/etc/Diag.cfg \
    $(LOCAL_PATH)/fdc-logtool/assets/modemlog.sh:/system/etc/modemlog.sh \
    $(LOCAL_PATH)/fdc-logtool/assets/gps_qxdm_log.cfg:/system/etc/gps.cfg \
    $(LOCAL_PATH)/fdc-logtool/assets/audio.cfg:/system/etc/audio.cfg \
    $(LOCAL_PATH)/fdc-logtool/assets/A60K_DIAG_V16.cfg:/system/etc/modem_and_audio.cfg \
    $(LOCAL_PATH)/fdc-logtool/assets/logtool_clear.sh:/system/etc/logtool_clear.sh \
    $(LOCAL_PATH)/fdc-logtool/assets/A60K_DIAG_V16_NRT.cfg:/system/etc/Compact_mode.cfg \
    $(LOCAL_PATH)/fdc-logtool/assets/raw_sender:/system/bin/raw_sender \
    $(LOCAL_PATH)/fdc-logtool/assets/init.asus.logtool.rc:root/init.asus.logtool.rc \
    $(LOCAL_PATH)/debug_sh/init.asus.check_last.sh:system/etc/init.asus.check_last.sh \
    $(LOCAL_PATH)/debug_sh/init.asus.check_asdf.sh:system/etc/init.asus.check_asdf.sh \
    $(LOCAL_PATH)/debug_sh/init.asus.debugtool.rc:root/init.asus.debugtool.rc \
    $(LOCAL_PATH)/debug_sh/init.asus.ramdump.sh:system/etc/init.asus.ramdump.sh \
    $(LOCAL_PATH)/debug_sh/savelogmtp.sh:system/etc/savelogmtp.sh \
    $(LOCAL_PATH)/debug_sh/savelogs.sh:system/etc/savelogs.sh \
    $(LOCAL_PATH)/debug_sh/saveramdump.sh:system/etc/saveramdump.sh \
    $(LOCAL_PATH)/debug_sh/init.asus.checkdatalog.sh:system/etc/init.asus.checkdatalog.sh \
    $(LOCAL_PATH)/debug_sh/init.asus.kernelmessage.sh:system/etc/init.asus.kernelmessage.sh \
    $(LOCAL_PATH)/debug_sh/prepare_asusdebug.sh:system/etc/prepare_asusdebug.sh

ifneq (,$(filter userdebug eng, $(TARGET_BUILD_VARIANT)))

PRODUCT_PACKAGES += \
                  logtool
endif

PRODUCT_PACKAGES += \
                  LogUploader \
                  packlogs.sh \
		  texfat.ko \
		  tcpdump \
                  logcommand \
                  dumps \
                  libdumpsys_logtool\
		  DetectLogApp
