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

import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BasePopupView;
import com.lxj.xpopup.enums.PopupAnimation;
import com.lxj.xpopup.enums.PopupPosition;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.lxj.xpopup.interfaces.SimpleCallback;
import com.shang.todolist.R;
import com.shang.todolist.UiUtils;
import com.shang.todolist.db.TodoBean;

import java.util.Calendar;
import java.util.Date;

/**
 * 添加待办的底部View
 */
public class AddTodoView extends LinearLayout implements View.OnClickListener {
    public EditText et_comment;
    private ImageView btn_mark;
    private ImageView btn_alarm;
    private ImageView btn_add;
    private LinearLayout ll_add;
    private int markFlag = 3;//默认 3  普通任务 灰色
    private long alarmTime;//闹钟
    private TodoBean mBean;
    private OnAddListener listener;
    private BasePopupView popMark;

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
        markFlag = 3;
        btn_mark.setImageResource(R.drawable.ic_urgent);
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
                    btn_add.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.gray_999)));
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

    public void closePop() {
        if (popMark != null) {
            popMark.dismiss();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_mark:
                popMark = new XPopup.Builder(getContext())
                        .hasShadowBg(false)
                        .offsetY(UiUtils.dip2px(-1))
                        .popupPosition(PopupPosition.Top) //手动指定弹窗的位置
                        .atView(v)  // 依附于所点击的View，内部会自动判断在上方或者下方显示
                        .asAttachList(new String[]{"紧急", "较急", "一般", "无"},
                                new int[]{R.drawable.ic_urgent_1, R.drawable.ic_urgent_2, R.drawable.ic_urgent_3, R.drawable.ic_urgent},
                                new OnSelectListener() {
                                    @Override
                                    public void onSelect(int position, String text) {
                                        markFlag = position;
                                        int markId;
                                        switch (position) {
                                            case 0:
                                                markId = R.drawable.ic_urgent_1;
                                                break;
                                            case 1:
                                                markId = R.drawable.ic_urgent_2;
                                                break;
                                            case 2:
                                                markId = R.drawable.ic_urgent_3;
                                                break;
                                            default:
                                                markId = R.drawable.ic_urgent;
                                                break;
                                        }
                                        btn_mark.setImageResource(markId);
                                        et_comment.requestFocus();
                                    }

                                });
                popMark.show();
                break;
            case R.id.btn_alarm:
                alarmTime = new Date().getTime();
                btn_alarm.setImageResource(R.drawable.ic_alarm_flag);
                break;
            case R.id.btn_add:
                if (listener != null) {
                    mBean = new TodoBean(Calendar.getInstance().getTimeInMillis(),
                            0, et_comment.getText().toString(), "", alarmTime, false, markFlag);
                    listener.onCreated(mBean);
                }
                break;
            default:
                break;
        }
    }
}
