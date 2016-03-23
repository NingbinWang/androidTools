#!/system/bin/sh

is_datalog_exist=`ls /data | grep logcat_log`
startlog_flag=`getprop persist.asus.startlog`
version_type=`getprop ro.build.type`
check_factory_version=`getprop ro.asus.factory`
is_sb=`grep -c SB=Y /proc/cmdline`
echo 49152 > /proc/sys/vm/min_free_kbytes
action_log='/dev/console'

if test "$version_type" = "userdebug"; then
	if test "$check_factory_version" = "1"; then
#		start mpdecision
	echo "[550kl_debug]in factory version" > $action_log
	fi
echo "[550kl_debug]in userdebug" > $action_log
fi 

if test -e /data/everbootup; then
	echo "[550kl_debug]/data/everbootup exit" > $action_log
	echo 1 > /data/everbootup
	startlog_flag=`getprop persist.asus.startlog`

	if test "$is_datalog_exist"; then
		chown system.system /data/logcat_log
		chmod 0775 /data/logcat_log
		if test "$startlog_flag" -eq 1;then
		setprop persist.asus.asusklog 1
		setprop sys.config.klog 1
		start logcat
		start logcat-radio
		start logcat-events
		else
		setprop persist.asus.asusklog 0
		setprop sys.config.klog 0
		stop logcat
		stop logcat-radio
		stop logcat-events
		fi
	fi        	
else
	echo "[550kl_debug] /data/everbootup not exit" > $action_log
	setprop persist.asus.ramdump 1
	setprop persist.asus.autosavelogmtp 0
	if  test "$version_type" = "eng"; then
		setprop persist.asus.startlog 1
		setprop persist.asus.kernelmessage 7
	elif test "$version_type" = "userdebug"; then
			if test "$check_factory_version" = "1"; then
				if test "$is_sb" = "1"; then
					setprop persist.asus.kernelmessage 0
				else
					setprop persist.asus.kernelmessage 7
				fi
				setprop persist.asus.enable_navbar 1
			else
				setprop persist.asus.kernelmessage 7	
			fi
		setprop persist.asus.startlog 1
		setprop persist.sys.downloadmode.enable 1
		
	fi
	
	startlog_flag=`getprop persist.asus.startlog`
	recheck_datalog=`ls /data | grep logcat_log`

	if test "$recheck_datalog"; then
		chown system.system /data/logcat_log
		chmod 0775 /data/logcat_log
		if test "$startlog_flag" -eq 1;then
		setprop persist.asus.asusklog 1
		setprop sys.config.klog 1
		start logcat
		start logcat-radio
		start logcat-events
		else
		setprop persist.asus.asusklog 0
		setprop sys.config.klog 0
		stop logcat
		stop logcat-radio
		stop logcat-events
		fi		
	fi
	echo 0 > /data/everbootup
fi
