package com.shang.todolist.ui.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.lxj.xpopup.core.BottomPopupView;
import com.shang.todolist.R;
import com.shang.todolist.db.TodoBean;

/**
 * 添加待办的弹窗
 */
public class AddTodoBottomPopup extends BottomPopupView implements View.OnClickListener {
    private EditText et_comment;
    private ImageView btn_mark;
    private ImageView btn_alarm;
    private ImageView btn_add;
    private boolean markFlag;
    private TodoBean mBean;
    private OnAddListener listener;

    public interface OnAddListener {
        void onCreated(TodoBean bean);
    }

    public AddTodoBottomPopup(@NonNull Context context) {
        super(context);
    }

    public void setListener(OnAddListener listener) {
        this.listener = listener;
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.add_bottom_popup;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        et_comment = findViewById(R.id.et_comment);
        btn_mark = findViewById(R.id.btn_mark);
        btn_alarm = findViewById(R.id.btn_alarm);
        btn_add = findViewById(R.id.btn_add);
        btn_mark.setOnClickListener(this);
        btn_alarm.setOnClickListener(this);
        btn_add.setOnClickListener(this);
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

    @Override
    protected void onShow() {
        super.onShow();
    }

    @Override
    protected void onDismiss() {
        super.onDismiss();
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
                add();
                break;
            default:
                break;
        }
    }

    private void add() {
        dismiss();
        //                mBean=new TodoBean();
        if (listener != null) {
            listener.onCreated(mBean);
        }
    }
}
