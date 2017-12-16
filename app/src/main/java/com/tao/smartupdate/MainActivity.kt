package com.tao.smartupdate

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.toast
import android.os.Environment
import android.view.View.*
import com.tao.smartupdatelibrary.SmartUpdate
import org.jetbrains.anko.doAsync
import java.io.File

/**
 * Created by tao on 2017/12/13.
 */

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        isProgressBarShow(false)

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

    }

    private fun customToast(message: String) {
        runOnUiThread {
            toast(message)
        }
    }

    private fun isProgressBarShow(isShow: Boolean) {
        if (isShow) {
            runOnUiThread {
                progressBar.visibility = VISIBLE
            }

        } else {
            runOnUiThread {
                progressBar.visibility = INVISIBLE
            }
        }
    }
}
