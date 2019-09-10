package com.shang.todolist.ui.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

import com.lxj.xpopup.core.CenterPopupView;
import com.shang.todolist.R;

public class CustomPopup extends CenterPopupView {
    public CustomPopup(@NonNull Context context) {
        super(context);
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.add_todo_popup;
    }
    @Override
    protected void onCreate() {
        super.onCreate();
//        findViewById(R.id.tv_close).setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dismiss();
//            }
//        });
    }
    protected void onShow() {
        super.onShow();
    }
}
