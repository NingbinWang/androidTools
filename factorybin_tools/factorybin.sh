#!/bin/bash
#author alex wang
#date:20160317
echo "***********************ptool partition**********************"
python ./tools/ptool.py -x ./partition/partition.xml 
if [ $? -eq 0 ]; then
echo "ptool.py succse"
else
echo "ptool.py fail"
exit 0
fi
echo "***********************start mkdir obj *********************"
mkdir obj
echo "***********************now move some files******************"
# satrt mv bin
mv ./*.bin ./obj
if [ $? -eq 0 ]; then
echo "move *.bin succse"
else
echo "move error please reset"
rm ./*.bin 
rm ./*.xml
rm ./obj -rf
exit 0
fi
#start mv xml
mv ./*.xml ./obj
if [ $? -eq 0 ]; then
echo "move *.xml succse"
else
echo "move error please reset"
rm ./*.bin 
rm ./*.xml 
rm ./obj -rf
exit 0
fi
echo "**********************cp/mv source img and firmware***********"
#start cp image
cp ./source/*.img                     ./
cp ./source/emmc_appsboot.mbn         ./
cp ./source/sec.dat                   ./
mkdir temp
mv ./source/firmware/gpt*             ./temp
mv ./source/firmware/prog*            ./temp 
mv ./source/firmware/validate*        ./temp
cp ./source/firmware/*.mbn            ./
cp ./source/firmware/*.bin            ./
echo "**********************cp/mv succse***************************"
echo "**********************checksparse rawprogram0.xml************"
python ./tools/checksparse.py -i ./obj/rawprogram0.xml
if [ $? -eq 0 ]; then
echo "checksparse.py succse"
else
echo "checksparse.py fail"
rm ./*.bin
rm ./obj/ -rf
rm ./*.img
rm ./*.mbn
rm ./sec.dat
mv ./temp/*  ./source/firmware/
rm temp -rf
exit 0
fi
echo "***********************start use gpt table bin***************"
cp ./obj/gpt_main0.bin               ./
cp ./obj/gpt_backup0.bin             ./ 
cp ./obj/gpt_both0.bin               ./
echo "***********************start msp.py patch0.xml***************"
case $2 in
"8G")
echo "***********************bin size will be 15269888**************"
	binsize=15269888
;;
"16G")
echo "***********************bin size will be 30785536**************"
	binsize=30785536
;;
*)
echo "**********if you not input we will usb 8G*******************"
binsize=15269888
;;
esac
python ./tools/msp.py -p ./obj/patch0.xml -r ./obj/rawprogram0.xml -d ${binsize}
if [ $? -eq 0 ]; then
echo "msp.py succse"
else
echo "msp.py fail"
rm ./*.bin
rm ./obj/ -rf
rm ./*.img
rm ./*.mbn
rm ./*.bak
rm ./sec.dat
mv ./temp/*                          ./source/firmware/
rm temp -rf
exit 0
fi
echo "***********************start mkdir out and md5sum***************"
mkdir out
cp ./singleimage.bin ./out/singleimage.bin
rm ./singleimage.bin
md5sum ./out/singleimage.bin > ./out/singleimage.md5
if [ -z $1 ];then
echo "you not input name,please fix name in out folder"
else
mv ./out/singleimage.bin  ./out/$1.bin
mv ./out/singleimage.md5  ./out/$1.md5
fi
rm ./*.bin
rm ./obj/ -rf
rm ./*.img
rm ./*.mbn
rm ./*.bak
rm ./sec.dat
mv ./temp/*                        ./source/firmware/
rm temp -rf
