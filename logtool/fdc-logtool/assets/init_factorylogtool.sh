#!/system/bin/sh
basic_information_path=/data/other/other.txt
setprop persist.asuslog.savedir "/sdcard/Asuslog/"
setprop persist.asuslog.dump.date ""
TIME=`date +%Y_%m%d_%H%M%S`
setprop persist.asuslog.dump.date $TIME
echo 1 > /proc/asusevtlog-switch
mkdir -p /sdcard/Asuslog/
mkdir -p /data/other/

busyboxfile=/data/debug/busybox
rawsenderfile=/data/debug/raw_sender
QMESA_64=/data/debug/QMESA_64

if [ ! -e "$busyboxfile" ]; then
	cp "/data/data/com.asus.fdclogtool/files/busybox" "/data/debug/busybox"
	chmod 4777 "/data/debug/busybox"
fi

if [ ! -e "$rawsenderfile" ]; then
	cp "/data/data/com.asus.fdclogtool/files/raw_sender" "/data/debug/raw_sender"
	chmod 4777 "/data/debug/raw_sender"
fi

if [ ! -e "$QMESA_64" ]; then
	cp "/data/data/com.asus.fdclogtool/files/QMESA_64" "/data/debug/QMESA_64"
	chmod 4777 "/data/debug/QMESA_64"
fi

buildType=`getprop ro.build.type`
if [ "$buildType" != "user" ]; then
   logmain_kernel=`getprop persist.asuslog.logcat.enable`
   logevent=`getprop persist.asuslog.logcate.enable`
   logradio=`getprop persist.asuslog.logcatr.enable`
   modem_ram=`getprop persist.asuslog.modem.ramdumps`

   if [ ! -n "$logmain_kernel" ]; then
  	setprop persist.asuslog.logcat.enable 1
   fi

   if [ ! -n "$logevent" ]; then
  	setprop persist.asuslog.logcate.enable 1
   fi
	
   if [ ! -n "$logradio" ]; then
  	setprop persist.asuslog.logcatr.enable 1
   fi

   if [ ! -n "$modem_ram" ]; then
	setprop persist.asuslog.modem.ramdumps 1
   fi
fi

############################################################

echo "/proc/apid " > $basic_information_path	
cat /proc/apid >> $basic_information_path
echo "/proc/aplte " >> $basic_information_path
cat /proc/aplte >> $basic_information_path
echo "/proc/aprf " >> $basic_information_path
cat /proc/aprf >> $basic_information_path
echo "/proc/apsta " >> $basic_information_path
cat /proc/apsta >> $basic_information_path
echo "/proc/apmem " >> $basic_information_path
cat /proc/apmem >> $basic_information_path
echo "/proc/aphd " >> $basic_information_path
cat /proc/aphd >> $basic_information_path
echo "image version= " >> $basic_information_path
getprop ro.build.display.id >> $basic_information_path
echo "ro.product.model= " >> $basic_information_path
getprop ro.product.model >> $basic_information_path
echo "ro.product.name= " >> $basic_information_path
getprop ro.product.name >> $basic_information_path
echo "ro.product.device= " >> $basic_information_path
getprop ro.product.device >> $basic_information_path
echo "Serial Number= " >> $basic_information_path
getprop ro.serialno >> $basic_information_path

setprop persist.asuslog.fac.init ""

