package com.shang.todolist.ui.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;

public class AppBar extends Toolbar {
    public AppBar(Context context) {
        super(context);
    }

    public AppBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AppBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
