#!/system/bin/sh

modemlogsize=`getprop persist.asuslog.modem.size`
modemlogcount=`getprop persist.asuslog.modem.count`
modemlogpath=`getprop persist.asuslog.modem.path`
modemcfgpath=`getprop persist.asuslog.modem.diacfg`

#Add-Begin by terry_tao 2015/11/30
#fix not capture modem logs when reboot device due to /sdcard/ hasn't been created
sdcardpath="/sdcard/"
count=1000
while [ ! -d $sdcardpath ] && [ $count -gt 0 ]
do
  ((count--))
  usleep 200000
done
#Add-End by terry_tao 2015/11/30

if [ ! -n "$modemlogsize" ]; then
  modemlogsize='200'
fi

if [ ! -n "$modemlogcount" ]; then
  modemlogcount='3'
fi 

if [ ! -n "$modemlogpath" ]; then
  modemlogpath='/sdcard/Asuslog/Modem'
else
   if [ ! -d "$modemlogpath" ]; then
	 setprop persist.asuslog.modem.path /sdcard/Asuslog/Modem
 	 modemlogpath='/sdcard/Asuslog/Modem'
	 if [ ! -f "$modemlogpath" ]; then
		mkdir -p $modemlogpath
         else
		rm -r $modemlogpath
		mkdir -p $modemlogpath
	 fi
   fi
fi

if [ ! -n "$modemcfgpath" ]; then
  modemcfgpath='/system/etc/modem_and_audio.cfg'
  setprop persist.asuslog.modem.diacfg "/system/etc/modem_and_audio.cfg"
fi 

if [ "$modemcfgpath" = "/system/etc/Diag.cfg" ]; then
	/system/bin/raw_sender 0x4b 0x12 0x21 0x0d  0xc8 0x00 0x00 0x00  0x00 0x20 0x00 0x00
	/system/bin/diag_mdlog -f $modemcfgpath -o $modemlogpath/modem_audio_gps -s $modemlogsize -n $modemlogcount -c
elif [ "$modemcfgpath" = "/system/etc/modem_and_audio.cfg" ]; then
	/system/bin/raw_sender 0x4b 0x12 0x21 0x0d  0xc8 0x00 0x00 0x00  0x00 0x20 0x00 0x00
        /system/bin/diag_mdlog -f $modemcfgpath -o $modemlogpath/modem_audio -s $modemlogsize -n $modemlogcount -c
elif [ "$modemcfgpath" = "/system/etc/gps.cfg" ]; then
	/system/bin/raw_sender 0x4b 0x12 0x21 0x0d  0xc8 0x00 0x00 0x00  0x00 0x20 0x00 0x00
        /system/bin/diag_mdlog -f $modemcfgpath -o $modemlogpath/gps -s $modemlogsize -n $modemlogcount -c
elif [ "$modemcfgpath" = "/system/etc/audio.cfg" ]; then
	/system/bin/raw_sender 0x4b 0x12 0x21 0x0d  0xc8 0x00 0x00 0x00  0x00 0x20 0x00 0x00
        /system/bin/diag_mdlog -f $modemcfgpath -o $modemlogpath/audio -s $modemlogsize -n $modemlogcount -c
elif [ "$modemcfgpath" = "/system/etc/Compact_mode.cfg" ]; then
	/system/bin/raw_sender 0x4b 0x12 0x21 0x0d  0x20 0xbf 0x02 0x00  0x00 0x00 0x10 0x00
	/system/bin/diag_mdlog -f $modemcfgpath -o $modemlogpath/compact -s $modemlogsize -n $modemlogcount -c
fi

