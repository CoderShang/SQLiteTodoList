package com.shang.todolist.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.shang.todolist.R;

/**
 * 通用的标题
 */
public class TitleBar extends FrameLayout implements View.OnClickListener {

    public TitleBar(Context context) {
        super(context);
        init(context);
    }

    public TitleBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TitleBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private TextView bar_title_center, bar_title_left;
    private ImageView bar_left, bar_second, bar_right;
    private TitleBarListener mListener;

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.title_bar, this, true);
        bar_title_center = (TextView) findViewById(R.id.bar_title_center);
        bar_title_left = (TextView) findViewById(R.id.bar_title_left);
        bar_left = (ImageView) findViewById(R.id.bar_left);
        bar_second = (ImageView) findViewById(R.id.bar_second);
        bar_right = (ImageView) findViewById(R.id.bar_right);
        bar_left.setOnClickListener(this);
        bar_second.setOnClickListener(this);
        bar_right.setOnClickListener(this);
    }

    public void setImgLeft(int rid) {
        if (rid == 0) {
            bar_left.setVisibility(GONE);
        } else {
            bar_left.setVisibility(VISIBLE);
            bar_left.setImageResource(rid);
        }
    }

    public void setImgRight(int rid) {
        if (rid == 0) {
            bar_right.setVisibility(GONE);
        } else {
            bar_right.setVisibility(VISIBLE);
            bar_right.setImageResource(rid);
        }
    }

    public void setImgSecond(int rid) {
        if (rid == 0) {
            bar_second.setVisibility(GONE);
        } else {
            bar_second.setVisibility(VISIBLE);
            bar_second.setImageResource(rid);
        }
    }

    public void setTitle(String title) {
        bar_title_center.setVisibility(VISIBLE);
        bar_title_center.setText(title);
    }

    public void setTitle(int strId) {
        if (strId == 0) {
            bar_title_center.setVisibility(GONE);
        } else {
            bar_title_center.setVisibility(VISIBLE);
            bar_title_center.setText(strId);
        }
    }

    public void setLeftTitle(String title) {
        bar_title_left.setVisibility(VISIBLE);
        bar_title_left.setText(title);
    }

    public void setLeftTitle(int strId) {
        if (strId == 0) {
            bar_title_left.setVisibility(GONE);
        } else {
            bar_title_left.setVisibility(VISIBLE);
            bar_title_left.setText(strId);
        }
    }

    public void setListener(TitleBarListener listener) {
        mListener = listener;
    }

    public interface TitleBarListener {
        void barLeft();

        void barSecond();

        void barRight();
    }

    @Override
    public void onClick(View v) {
        if (mListener == null) {
            return;
        }
        switch (v.getId()) {
            case R.id.bar_left:
                mListener.barLeft();
                break;
            case R.id.bar_second:
                mListener.barSecond();
                break;
            case R.id.bar_right:
                mListener.barRight();
                break;
            default:
                break;
        }
    }
}
