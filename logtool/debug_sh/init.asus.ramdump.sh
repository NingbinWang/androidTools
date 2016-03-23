#!/system/bin/sh
action_log='/dev/console'
echo "[550kl] enter init.asus.ramdump.sh" > $action_log
ramdump_flag=`getprop persist.asus.ramdump`
echo "$ramdump_flag" > /sys/kernel/debug/Asus_ramdump/Asus_ramdump_flag

sleep 40
is_autosavelog_exist=`cat /proc/cmdline | grep autosavelog`
if test "$is_autosavelog_exist"; then
	autosavelogmtp_flag=`getprop persist.asus.autosavelogmtp`
	if test $autosavelogmtp_flag -eq 1 ; then
		start savelogmtp
		exit
	fi
fi	

