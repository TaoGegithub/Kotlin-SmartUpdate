package com.tao.smartupdatelibrary;

/**
 * Created by tao on 2017/12/13.
 */

public class SmartUpdate {

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

}
