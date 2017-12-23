# Kotlin-SmartUpdate

Kotlin实现Android的增量更新

## 说明 

本项目包含两个部分：**Android增量更新项目**和**jni的C项目**，对于不想再次进行jni开发的朋友，可以直接下载本demo，将包含的smartupdatelibrary作为lib库引用即可

## 开发步骤：jni、Android部分（若不编写jni部分，可直接跳到8）

### 一. jni部分

#### 1. 环境准备

SDK中安装有NDK，并安装了CMake、LLDB工具

#### 2. 新建Android Library

——>SmartUpdateLibrary

#### 3. 通过CMakelists.txt配置NDK

在SmartUpdateLibrary根目录下新建CMakelists.txt，此文件功能强大，直接替换了以往jni的旧编译方式NDK，内容如下：

	# For more information about using CMake with Android Studio, read the
	# documentation: https://d.android.com/studio/projects/add-native-code.html
	
	# Sets the minimum version of CMake required to build the native library.
	
	cmake_minimum_required(VERSION 3.4.1)
	
	#设置生成的so动态库最后输出的路径
	set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI})
	
	# Creates and names a library, sets it as either STATIC
	# or SHARED, and provides the relative paths to its source code.
	# You can define multiple libraries, and CMake builds them for you.
	# Gradle automatically packages shared libraries with your APK.
	
	add_library( # Sets the name of the library.
	             patch_lib
	
	             # Sets the library as a shared library.
	             SHARED
	
	             # Provides a relative path to your source file(s).
	             src/main/jni/SmartUpdate.c )
	
	# include_directories(src/main/jni/include)
	
	# Searches for a specified prebuilt library and stores the path as a
	# variable. Because CMake includes system libraries in the search path by
	# default, you only need to specify the name of the public NDK library
	# you want to add. CMake verifies that the library exists before
	# completing its build.
	
	find_library( # Sets the name of the path variable.
	              log-lib
	
	              # Specifies the name of the NDK library that
	              # you want CMake to locate.
	              log )
	
	# Specifies libraries CMake should link to your target library. You
	# can link multiple libraries, such as libraries you define in this
	# build script, prebuilt third-party libraries, or system libraries.
	
	target_link_libraries( # Specifies the target library.
	                       patch_lib
	
	                       # Links the target library to the log library
	                       # included in the NDK.
	                       ${log-lib} )

#### 4. java与c的交互

在java目录下新建SmartUpdate.java，里面进行so文件的加载，以及通过底层方法进行增量更新，关键代码如下：

	/**
     * 加载so文件
     */
    static {
        System.loadLibrary("patch_lib");
    }

    /**
     * 本地方法实现增量更新
     *
     * @param oldPath   旧版本APK路径
     * @param newPath   新版本APK路径
     * @param patchPath 差分包patch路径
     * @return 0，表示更新成功
     */
    public static native int update(String oldPath, String newPath, String patchPath);

#### 5. c方法的编写

在smartupdatelibrary\src\main路径下，新建jni目录，并新建SmartUpdate.c，参考SmartUpdate.java的update方法，进行c方法的编写（具体内容请参见代码，编写时请注意包名是否正确）：

	JNIEXPORT jint JNICALL
	Java_com_tao_smartupdatelibrary_SmartUpdate_update(JNIEnv *env, jclass type, jstring oldPath_, jstring newPath_, jstring 	patchPath_) {}
	
#### 6. 配置library的gradle

在smartupdatelibrary的build.gradle中进行配置，以达到CMakelists.txt的编译作用：

	apply plugin: 'com.android.library'

	android {
	    compileSdkVersion 26
	
	
	
	    defaultConfig {
	        minSdkVersion 15
	        targetSdkVersion 26
	        versionCode 1
	        versionName "1.0"
	
	        testInstrumentationRunner "android.support.test.runner.AndroidJUnitRunner"
	
	        externalNativeBuild {
	            cmake {
	                cppFlags ''
	
	                //下面可有可无，根据具体要适配的平台进行增删
	                abiFilters 'armeabi', 'armeabi-v7a', 'arm64-v8a', 'x86', 'x86_64', 'mips', 'mips64'
	            }
	        }
	
	    }
	
	    buildTypes {
	        release {
	            minifyEnabled false
	            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
	        }
	    }
	
	    externalNativeBuild {
	        cmake {
	            path 'CMakeLists.txt'
	        }
	    }
	
	    sourceSets {
	        main {
	            jniLibs.srcDirs = ['src/main/jniLibs']
	            //添加下面选项，是为了解决AS报“more than ··so··”错误
	            jniLibs.srcDirs = []
	        }
	    }
	}

	dependencies {
	    implementation fileTree(include: ['*.jar'], dir: 'libs')
	    implementation 'com.android.support:appcompat-v7:26.1.0'
	    testImplementation 'junit:junit:4.12'
	    androidTestImplementation 'com.android.support.test:runner:1.0.1'
	    androidTestImplementation 'com.android.support.test.espresso:espresso-core:3.0.1'
	}

#### 7. 同步jni所有配置

将demo中tools里面的bzip2包复制到jni目录下，再进行底层方法的完善，最终代码请参考SmartUpdate.c，最后在smartupdatelibrary项目中右击，选择——“Link C++ Project with Gradle”，再同步一下gradle，即可完成jni部分的代码编写

### 二. Android部分

#### 8. 新建Android-Kotlin增量更新项目

在此gradle中添加smartupdatelibrary作为lib库依赖，为方便开发，也添加anko库：

	implementation "org.jetbrains.anko:anko:$anko_version"
    implementation project(':smartupdatelibrary')

#### 9. patch增量包的制作以及下载

正常的增量更新过程中，需要去服务器下载patch增量包到本地，与本地已经安装的旧版本apk进行合并，成为新版本apk，再进行安装；此demo中，重点是增量更新，所以省去了去服务器下载patch增量包的过程，请在tools目录下直接把new_patch.patch导入到手机中

* **附：patch增量包的制作（同时也是服务器端制作patch增量包的过程）**
	*  1.打开本demo中tools目录，发现有new6.8.8.apk、old6.8.0.apk
	*  2.在此目录下执行cmd命令行，以生成patch增量包：./bsdiff.exe ./old6.8.0.apk ./new6.8.8.apk new_patch.patch

#### 10. 增量更新

关键代码如下：

	patch.onClick {

		val pm = packageManager

        //此处以“VIVA畅读”app为例子，获取当前应用的包名：adb shell dumpsys window w |findstr \/ |findstr name=
        val appInfo = pm.getApplicationInfo("viva.reader", 0)

        //旧版本路径
        val oldPath = appInfo.sourceDir

        //新版本保存路径
        val newApkFile = File(Environment.getExternalStorageDirectory(), "new6.8.8.apk")

        //patch更新包保存路径
        val patchFile = File(Environment.getExternalStorageDirectory(), "new_patch.patch")

        //更新包是否已经下载判断
        if (!patchFile.exists()) {
            customToast("请将差分包new_patch.patch保存到sdcard")
            return@onClick
        }

        //合并更新包是个耗时操作，故放在子线程中执行
        doAsync {
            isProgressBarShow(true)
            val result = SmartUpdate.update(oldPath, newApkFile.absolutePath, patchFile.absolutePath)
            if (result == 0) {
                isProgressBarShow(false)
                customToast("合并成功")
            } else {
                isProgressBarShow(false)
                customToast("合并失败")
            }
        }

	}

#### 11. 验证

* 安装tools目录下的old6.8.0.apk
* 运行安装本demo的app，点击"旧版本与patch更新包合并成为新版本"，在手机中会生成new6.8.8.apk，安装即可