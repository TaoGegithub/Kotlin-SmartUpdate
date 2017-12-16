# 1、模仿服务器端进行的操作：进行差分包patch的分离
usage: bsdiff.exe oldfile newfile patchfile
example: bsdiff.exe old.apk new.apk patch.patch


# 2、获取当前应用的包名：
usage: adb shell dumpsys window w |findstr \/ |findstr name=