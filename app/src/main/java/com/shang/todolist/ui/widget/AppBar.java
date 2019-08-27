package com.shang.todolist.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;

import com.shang.todolist.R;

public class AppBar extends Toolbar {
    public AppBar(Context context) {
        this(context, null);
    }

    public AppBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AppBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.AppBar);
//            float radius = ta.getDimension(R.styleable.RoundBGRelativeLayout_radius, 0);
            ta.recycle();
        }
    }
}
