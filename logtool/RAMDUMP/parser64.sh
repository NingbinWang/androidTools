#!/bin/sh
if [ -x ./temp ]
then
	rm -r ./temp/*
fi

if [ -f ./parser64.sh ]
then
	mkdir temp
	mkdir ramdump-case
fi

clear

if [ -f ./ramdump-case/vmlinux ]
then
	python ramparse.py --force-hardware=8937 --64-bit -a ./ramdump-case -v ./ramdump-case/vmlinux -o ./temp/ --nm-path ./aarch64-linux-gnu-nm --gdb-path ./aarch64-linux-gnu-gdb --output-file linux-parser-output.txt --everything
else
	python ramparse.py --force-hardware=8937 --64-bit -a ./ramdump-case -v ${OUT}/obj/KERNEL_OBJ/vmlinux -o ./temp/ --nm-path ./aarch64-linux-gnu-nm --gdb-path ./aarch64-linux-gnu-gdb --output-file linux-parser-output.txt --everything
fi

modem_crash=`grep --color=auto 'modem crashed' ./temp/linux-parser-output.txt`
if [ -z "$modem_crash" ];  then
cat ./temp/linux-parser-output.txt | grep "KERNEL PANIC detected"
else
echo [!] MODEM CRASH detected
fi
echo ===========================================================================
echo Please reference ./temp/linux-parser-output.txt
#==========================================================

