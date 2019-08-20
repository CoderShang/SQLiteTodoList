package com.shang.todolist;

import android.content.res.Resources;

public class UiUtils {


    /**
     * dip转pix
     *
     * @param dpValue
     * @return
     */
    public static int dip2px(float dpValue) {
        final float scale = App.get().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 获得资源
     */
    public static Resources getResources() {
        return App.get().getResources();
    }

}
