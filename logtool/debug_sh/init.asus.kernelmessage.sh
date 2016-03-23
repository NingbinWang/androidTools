#!/system/bin/sh
kernelmessage_flag=`getprop persist.asus.kernelmessage`
is_oemuartlog_exist=`cat /proc/cmdline | grep UARTLOG`
if test "$is_oemuartlog_exist"; then
	echo 7 > /proc/sys/kernel/printk
else
	echo "$kernelmessage_flag" > /proc/sys/kernel/printk
fi
