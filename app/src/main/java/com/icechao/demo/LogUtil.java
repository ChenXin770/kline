package com.icechao.demo;

import android.util.Log;

import com.orhanobut.logger.Logger;


/*************************************************************************
 * Description   :
 *
 * @PackageName  : com.icechao.klinelib.utils
 * @FileName     : LogUtil.java
 * @Author       : chao
 * @Date         : 2019/1/10
 * @Email        : icechliu@gmail.com
 * @version      : V1
 *************************************************************************/
public class LogUtil {
    private final static String TAG = "CHAO=>";

    public static void e(Object o) {

        Logger.e(TAG, String.valueOf(o));

    }

}
