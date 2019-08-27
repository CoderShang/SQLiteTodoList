package com.shang.todolist.ui.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.shang.todolist.R;
import com.shang.todolist.db.TodoBean;

import java.util.Calendar;

/**
 * 添加待办的底部View
 */
public class AddTodoView extends LinearLayout implements View.OnClickListener {
    public EditText et_comment;
    private ImageView btn_mark;
    private ImageView btn_alarm;
    private ImageView btn_add;
    private LinearLayout ll_add;
    private boolean markFlag;
    private TodoBean mBean;
    private OnAddListener listener;

    public AddTodoView(Context context) {
        super(context);
        init(context);
    }

    public AddTodoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AddTodoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void clearUI() {
        et_comment.setText("");
        markFlag = false;
        btn_mark.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.disable)));

    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.add_bottom_popup, this, true);
        ll_add = findViewById(R.id.ll_add);
        et_comment = findViewById(R.id.et_comment);
        btn_mark = findViewById(R.id.btn_mark);
        btn_alarm = findViewById(R.id.btn_alarm);
        btn_add = findViewById(R.id.btn_add);
        ll_add.setOnClickListener(this);
        btn_mark.setOnClickListener(this);
        btn_alarm.setOnClickListener(this);
        btn_add.setOnClickListener(this);
        btn_add.setEnabled(false);
        et_comment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    btn_add.setEnabled(false);
                    btn_add.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.disable)));
                } else {
                    btn_add.setEnabled(true);
                    btn_add.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.colorPrimary)));
                }
            }
        });
    }

    public interface OnAddListener {
        void onCreated(TodoBean bean);
    }

    public void setListener(OnAddListener listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_mark:
                markFlag = !markFlag;
                if (markFlag) {
                    btn_mark.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.urgent_red)));
                } else {
                    btn_mark.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.disable)));
                }
                break;
            case R.id.btn_alarm:
                Toast.makeText(getContext(), "暂未开发...", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_add:
                if (listener != null) {
                    mBean = new TodoBean(Calendar.getInstance().getTimeInMillis(),
                            0, et_comment.getText().toString(), "", 0, false, markFlag ? 1 : 0);
                    listener.onCreated(mBean);
                }
                break;
            default:
                break;
        }
    }
}
