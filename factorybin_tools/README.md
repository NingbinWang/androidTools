#intrduce
this tool is used to create factory burn-in bin file. if you execute factorybin.sh, it will create bin and md5 file in out folder.

#how to use this tool
this tool need some file

##step 1: put partition.xml
please put your partition.xml in partition folder

##step 2: put source
if you have ZIP file,you can unzip this file and then rename this folder to source.
such as:
   
    WW-Z01FD-1.0.0.43-EVB-userdebug-20160316.zip->unzip->folder:WW-Z01FD-1.0.0.43-EVB-userdebug-20160316
    rename folder:WW-Z01FD-1.0.0.43-EVB-userdebug-20160316->folder:source

##step 3:checkout partition.xml
plese checkout filename of partition.xml whether exist in source

##step 4:execute factorybin.sh
please execute factorybin.sh
such as:
     ./factorybin.sh para1 para2
  para1:
       the name of factory burn-in bin file, if you not input anything,it will use name singleimage
  para2:
       the bin size:
           "8G" :15269888
           "16G":30785536
       if you not input anything,it will use 8G.
#other tools
if you want to dd 4G userdata

python dd.py --if=/dev/zero --obs=512 --count=7765983 --skip=0 --of=userdata.img

#how to verify factory burn-in bin file right
you can let your phone in fastboot mode and then execute command

    fastboot flash factory_img xxxxxxx.bin

xxxxxxx.bin is factory burn-in bin file which you created

#auther
alex wang 

#date
20160317
