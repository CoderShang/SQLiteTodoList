package com.shang.todolist.ui.widget;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BasePopupView;
import com.lxj.xpopup.core.BottomPopupView;
import com.lxj.xpopup.enums.PopupPosition;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.lxj.xpopup.interfaces.SimpleCallback;
import com.lxj.xpopup.interfaces.XPopupCallback;
import com.lxj.xpopup.util.XPopupUtils;
import com.lxj.xpopup.widget.VerticalRecyclerView;
import com.shang.todolist.App;
import com.shang.todolist.R;
import com.shang.todolist.UiUtils;
import com.shang.todolist.db.DbThreadPool;
import com.shang.todolist.db.TodoBean;
import com.shang.todolist.db.TodoListContract;
import com.shang.todolist.ui.MainActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Description: 添加待办事项的底部弹窗
 */
public class AddTodoPopup extends BottomPopupView implements View.OnClickListener {
    public EditText et_comment;
    private ImageView btn_mark;
    private ImageView btn_alarm;
    private ImageView btn_add;
    private LinearLayout ll_add;
    private int markFlag = 3;//默认 3  普通任务 灰色
    private int sort = 0;
    private long manifest = 0;
    private long alarmTime;//闹钟
    private TodoBean mBean;
    private BasePopupView popMark;

    public AddTodoPopup(@NonNull Context context, int sort, long manifest) {
        super(context);
        this.sort = sort;
        this.manifest = manifest;
    }

    public AddTodoPopup(@NonNull Context context, TodoBean bean) {
        super(context);
        this.mBean = bean;
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.add_bottom_popup;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        initView();
    }

    private void initView() {
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_mark:
                popMark = new XPopup.Builder(getContext())
                        .hasShadowBg(false)
                        .isRequestFocus(false)
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
                                    }

                                });
                popMark.show();
                break;
            case R.id.btn_alarm:
                alarmTime = new Date().getTime();
                btn_alarm.setImageResource(R.drawable.ic_alarm_flag);
                break;
            case R.id.btn_add:
                mBean = new TodoBean(Calendar.getInstance().getTimeInMillis(),
                        sort + 1, et_comment.getText().toString(), "", 0, false, markFlag, manifest);
                insertValue(mBean);
                dismiss();
                break;
            default:
                break;
        }
    }

    private void insertValue(final TodoBean addBean) {
        Runnable updateTask = new Runnable() {
            @Override
            public void run() {
                ContentValues contentValues = new ContentValues();
                contentValues.put(TodoListContract.TodoListColumns._ID, addBean.id);
                contentValues.put(TodoListContract.TodoListColumns.SORT_ID, addBean.sortId);
                contentValues.put(TodoListContract.TodoListColumns.TITLE, addBean.title);
                contentValues.put(TodoListContract.TodoListColumns.DESCRIPTION, addBean.description);
                contentValues.put(TodoListContract.TodoListColumns.ALARM, addBean.alarm);
                contentValues.put(TodoListContract.TodoListColumns.STATUS, addBean.status ? 1 : 0);
                contentValues.put(TodoListContract.TodoListColumns.MARK, addBean.mark);
                contentValues.put(TodoListContract.TodoListColumns.MANIFEST, addBean.manifest);
                App.get().getContentResolver().insert(TodoListContract.TodoListColumns.CONTENT_URI_ADD, contentValues);
            }
        };
        DbThreadPool.getThreadPool().exeute(updateTask);
    }
}