package com.shang.todolist.ui.widget;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BasePopupView;
import com.lxj.xpopup.core.BottomPopupView;
import com.lxj.xpopup.enums.PopupPosition;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.shang.todolist.App;
import com.shang.todolist.R;
import com.shang.todolist.UiUtils;
import com.shang.todolist.db.CalendarDB;
import com.shang.todolist.db.DbThreadPool;
import com.shang.todolist.db.TodoBean;
import com.shang.todolist.db.TodoListContract;
import com.shang.todolist.event.DeleteTodoEvent;
import com.shang.todolist.event.EditTodoEvent;
import com.shang.todolist.event.InsertTodoEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;
import java.util.Date;

/**
 * Description: 添加待办事项的底部弹窗
 */
public class AddTodoPopup extends BottomPopupView implements View.OnClickListener {
    public EditText et_comment, et_desc;
    private ImageView btn_mark;
    private TextView tv_time, tv_remind;
    private ImageView btn_add, btn_delete, iv_del_time, iv_del_remind;
    private LinearLayout ll_add;
    private RelativeLayout rl_remind;
    private RelativeLayout rl_time;
    private int markFlag = 3;//默认 3  普通任务 灰色
    private int sort = 0;
    private int pos = -1;
    private long manifest = 0;
    private long alarmTime;//闹钟
    private TodoBean mBean;
    private BasePopupView popMark;

    public AddTodoPopup(@NonNull Context context, int sort, long manifest) {
        super(context);
        this.sort = sort;
        this.manifest = manifest;
    }

    public AddTodoPopup(@NonNull Context context, TodoBean bean, int pos) {
        super(context);
        this.mBean = bean;
        this.pos = pos;
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
        //TODO ⏰闹钟提醒UI布局
        ll_add = findViewById(R.id.ll_add);
        et_comment = findViewById(R.id.et_comment);
        et_desc = findViewById(R.id.et_desc);
        btn_mark = findViewById(R.id.btn_mark);
        rl_remind = findViewById(R.id.rl_remind);
        rl_time = findViewById(R.id.rl_time);
        btn_add = findViewById(R.id.btn_add);
        btn_delete = findViewById(R.id.btn_delete);
        ll_add.setOnClickListener(this);
        btn_mark.setOnClickListener(this);
        btn_add.setOnClickListener(this);
        btn_delete.setOnClickListener(this);
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
        if (mBean != null) {
            btn_delete.setVisibility(VISIBLE);
            et_comment.setText(mBean.title);
            et_comment.setSelection(mBean.title.length());
            et_desc.setText(mBean.description);
            btn_mark.setImageResource(switchMark(mBean.mark));
            //TODO 渲染⏰闹钟提醒
        }
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
                                        btn_mark.setImageResource(switchMark(position));
                                    }

                                });
                popMark.show();
                break;
//            case R.id.btn_alarm:
//                alarmTime = new Date().getTime();
//                iv_alarm.setImageResource(R.drawable.ic_alarm_flag);
//                iv_alarm.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.colorPrimary)));
//                break;
            case R.id.btn_add:
                if (mBean == null) {
                    //TODO 添加⏰闹钟提醒
                    mBean = new TodoBean(Calendar.getInstance().getTimeInMillis(),
                            sort, et_comment.getText().toString(), et_desc.getText().toString(), 0, false, markFlag, manifest);
                    insertValue(mBean);
                } else {
                    mBean.title = et_comment.getText().toString();
                    mBean.description = et_desc.getText().toString();
                    mBean.mark = markFlag;
                    //TODO 保存⏰闹钟提醒
                    updateValue(mBean);
                }
                dismiss();
                break;
            case R.id.btn_delete:
                deleteValue(pos, mBean.id);
                dismiss();
                break;
            default:
                break;
        }
    }

    private int switchMark(int position) {
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
        return markId;
    }

    private void insertValue(final TodoBean addBean) {
        //TODO 新增⏰闹钟表数据
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
                CalendarDB.addCalendarEventRemind(App.get(), addBean.title, addBean.description, 0, 0, 0, new CalendarDB.onCalendarRemindListener() {
                    @Override
                    public void onFailed(Status error_code) {
                        Toast.makeText(App.get(), "❌咦卧槽！闹钟创建失败了！", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSuccess() {
                        Toast.makeText(App.get(), "✔️闹钟创建成功！", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };
        DbThreadPool.getThreadPool().exeute(updateTask);
        EventBus.getDefault().post(new InsertTodoEvent(addBean));
    }

    private void deleteValue(int pos, final long deleteId) {
        //TODO 删除⏰闹钟表数据
        Runnable deleteTask = new Runnable() {
            @Override
            public void run() {
                App.get().getContentResolver().delete(TodoListContract.TodoListColumns.CONTENT_URI_DELETE, TodoListContract.TodoListColumns._ID + "=?", new String[]{String.valueOf(deleteId)});
            }
        };
        DbThreadPool.getThreadPool().exeute(deleteTask);
        EventBus.getDefault().post(new DeleteTodoEvent(pos));

    }

    private void updateValue(final TodoBean updateBean) {
        //TODO 保存⏰闹钟提醒字段
        Runnable updateTask = new Runnable() {
            @Override
            public void run() {
                ContentValues contentValues = new ContentValues();
                contentValues.put(TodoListContract.TodoListColumns.TITLE, updateBean.title);
                contentValues.put(TodoListContract.TodoListColumns.DESCRIPTION, updateBean.description);
                contentValues.put(TodoListContract.TodoListColumns.ALARM, updateBean.alarm);
                contentValues.put(TodoListContract.TodoListColumns.MARK, updateBean.mark);
                App.get().getContentResolver().update(TodoListContract.TodoListColumns.CONTENT_URI_UPDATE, contentValues, TodoListContract.TodoListColumns._ID + "=?", new String[]{String.valueOf(updateBean.id)});
            }
        };
        DbThreadPool.getThreadPool().exeute(updateTask);
        EventBus.getDefault().post(new EditTodoEvent(updateBean));
    }
}