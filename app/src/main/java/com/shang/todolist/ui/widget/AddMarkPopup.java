package com.shang.todolist.ui.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

import com.lxj.xpopup.core.HorizontalAttachPopupView;
import com.shang.todolist.R;

/**
 * Description:
 * <p>
 * //                new XPopup.Builder(getContext())
 * ////                        .offsetX(-10) //往左偏移10
 * ////                        .offsetY(10)  //往下偏移10
 * //                        .popupPosition(PopupPosition.Right) //手动指定位置，有可能被遮盖
 * //                        .hasShadowBg(false) // 去掉半透明背景
 * //                        .atView(v)
 * //                        .asCustom(new AddMarkPopup(getContext()))
 * //                        .show();
 * Create by lxj, at 2019/3/13
 */
public class AddMarkPopup extends HorizontalAttachPopupView implements View.OnClickListener {
    public AddMarkPopup(@NonNull Context context) {
        super(context);
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.add_mark_popup;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        findViewById(R.id.urgent_1).setOnClickListener(this);
        findViewById(R.id.urgent_2).setOnClickListener(this);
        findViewById(R.id.urgent_3).setOnClickListener(this);
        findViewById(R.id.urgent).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.urgent_1:
                break;
            case R.id.urgent_2:
                break;
            case R.id.urgent_3:
                break;
            case R.id.urgent:
                break;
            default:
                break;
        }
        dismiss();
    }
}
