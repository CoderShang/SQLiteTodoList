package com.shang.todolist.ui.widget;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;

import java.lang.reflect.Field;

/**
 * 键盘的监听
 * Created by shangzhongjia
 */

public class SoftKeyBoardListener {
    private View rootView;//activity的根视图
    boolean isVisiableForLast = false;
    private OnSoftKeyBoardChangeListener onSoftKeyBoardChangeListener;
    public ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = null;
    private int displayHight;

    public SoftKeyBoardListener(final Activity activity) {
        //获取activity的根视图
        rootView = activity.getWindow().getDecorView();
        onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect rect = new Rect();
                rootView.getWindowVisibleDisplayFrame(rect);
                //计算出可见屏幕的高度
                if (displayHight == rect.bottom - rect.top)
                    return;
                displayHight = rect.bottom - rect.top;
                //获得屏幕整体的高度
                int hight = rootView.getHeight();
                boolean visible = (double) displayHight / hight < 0.8;
                int statusBarHeight = 0;
                try {
                    Class<?> c = Class.forName("com.android.internal.R$dimen");
                    Object obj = c.newInstance();
                    Field field = c.getField("status_bar_height");
                    int x = Integer.parseInt(field.get(obj).toString());
                    statusBarHeight = activity.getResources().getDimensionPixelSize(x);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (visible && visible != isVisiableForLast) {
                    //获得键盘高度
                    keyboardHeight = hight - displayHight - statusBarHeight;
                    if (onSoftKeyBoardChangeListener != null) {
                        onSoftKeyBoardChangeListener.keyBoardShow(keyboardHeight);
                    }
                } else {
                    if (onSoftKeyBoardChangeListener != null) {
                        onSoftKeyBoardChangeListener.keyBoardHide(0);
                    }
                }
                isVisiableForLast = visible;
            }
        };
        //监听视图树中全局布局发生改变或者视图树中的某个视图的可视状态发生改变
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
    }

    private int keyboardHeight;

    /**
     * 删除键盘监听
     *
     * @param activity
     */
    public void removeListener(Activity activity) {
        if (activity != null && onGlobalLayoutListener != null) {
            activity.getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
        }
    }

    public void setListener(OnSoftKeyBoardChangeListener onSoftKeyBoardChangeListener) {
        this.onSoftKeyBoardChangeListener = onSoftKeyBoardChangeListener;
    }


    public interface OnSoftKeyBoardChangeListener {
        void keyBoardShow(int height);

        void keyBoardHide(int height);
    }
}
