package com.shang.todolist.ui;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.shang.todolist.App;
import com.shang.todolist.R;
import com.shang.todolist.UiUtils;
import com.shang.todolist.db.DbThreadPool;
import com.shang.todolist.db.TodoListBean;
import com.shang.todolist.db.TodoListContract;
import com.shang.todolist.event.DeleteTodoEvent;
import com.shang.todolist.event.EditTodoEvent;
import com.shang.todolist.ui.widget.TitleBar;

import org.greenrobot.eventbus.EventBus;

import java.util.Date;

/**
 * 新增 & 编辑 待办事项的页面
 */
public class EditActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String KEY_ID = "ID";
    public static final String KEY_POS = "POS";
    public static final String KEY_SORT_ID = "SORT_ID";
    private TitleBar title_bar;
    private EditText et_title, et_desc;
    private TextView tv_alarm;
    private CheckBox cb_mark;
    private RelativeLayout top_layout;
    private LinearLayout root_layout;
    private int searchId = -1; //主键ID，0说明是新添加，有值则编辑
    private int pos;
    private int sortId;
    private TodoListBean mTodoBean;
    private Runnable queryTask;
    private InputMethodManager manager;

    public static void startActivity(Context context, int searchId, int pos) {
        Intent intent = new Intent(context, EditActivity.class);
        intent.putExtra(KEY_ID, searchId);
        intent.putExtra(KEY_POS, pos);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, int maxId) {
        Intent intent = new Intent(context, EditActivity.class);
        intent.putExtra(KEY_SORT_ID, maxId);
        context.startActivity(intent);
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (EditActivity.this == null) {
                return false;
            }
            int what = msg.what;
            switch (what) {
                case 1:
                    //接收查询数据库返回的结果，更新UI
                    updateUI();
                    break;
                default:
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        Intent intent = getIntent();
        if (intent != null) {
            searchId = intent.getIntExtra(KEY_ID, -1);
            sortId = intent.getIntExtra(KEY_SORT_ID, 0);
            pos = intent.getIntExtra(KEY_POS, 0);
        }
        title_bar = findViewById(R.id.title_bar);
        top_layout = findViewById(R.id.top_layout);
        root_layout = findViewById(R.id.root_layout);
        et_title = findViewById(R.id.et_title);
        et_desc = findViewById(R.id.et_desc);
        tv_alarm = findViewById(R.id.tv_alarm);
        cb_mark = findViewById(R.id.cb_mark);
        title_bar.setImgLeft(R.drawable.ic_arrow_back);
        title_bar.setImgRight(R.drawable.ic_done);
        title_bar.setListener(new TitleBar.TitleBarListener() {
            @Override
            public void barLeft() {
                finish();
            }

            @Override
            public void barSecond() {
                deleteValue(pos, searchId);
            }

            @Override
            public void barRight() {
                submit();
            }
        });
        if (searchId != -1) {
            title_bar.setLeftTitle("编辑");
            title_bar.setImgSecond(R.drawable.ic_delete_white);
        } else {
            title_bar.setLeftTitle("新增一个待办");
            title_bar.setImgSecond(0);
        }
        if (searchId != -1) {
            queryValue(searchId);
        }
        manager = ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE));
    }

    /**
     * 更新UI
     */
    private void updateUI() {
        et_title.setText(mTodoBean.title);
        et_desc.setText(mTodoBean.description);
        if (mTodoBean.alarm != 0) {
            tv_alarm.setText(UiUtils.getDateStr(mTodoBean.alarm));
            tv_alarm.setCompoundDrawablesWithIntrinsicBounds(App.get().getResources().getDrawable(R.drawable.ic_alarm), null, null, null);
        }
        if (mTodoBean.mark != 0) {
            cb_mark.setChecked(true);
            cb_mark.setText("已标记为重要");
        }
    }

    /**
     * 获取值新增到数据库中
     */
    private void submit() {
        String title = et_title.getText().toString();
        if (TextUtils.isEmpty(title)) {
            Snackbar.make(root_layout, "标题不可为空！", Snackbar.LENGTH_LONG).show();
            return;
        }
        String desc = et_desc.getText().toString();
        long alarm = 0;
        int isMark = cb_mark.isChecked() ? 1 : 0;
        //调用数据库 执行 新增 或 修改 操作
        if (searchId == -1) {
            insertValue(new TodoListBean(searchId, sortId, title, desc, alarm, new Date().getTime(), false, isMark));
        } else {
            mTodoBean.title = title;
            mTodoBean.description = desc;
            mTodoBean.alarm = alarm;
            mTodoBean.mark = isMark;
            updateValue(mTodoBean);
        }
        finish();
    }

    private void insertValue(final TodoListBean addBean) {
        Runnable updateTask = new Runnable() {
            @Override
            public void run() {
                ContentValues contentValues = new ContentValues();
                contentValues.put(TodoListContract.TodoListColumns.SORT_ID, addBean.sortId);
                contentValues.put(TodoListContract.TodoListColumns.TITLE, addBean.title);
                contentValues.put(TodoListContract.TodoListColumns.DESCRIPTION, addBean.description);
                contentValues.put(TodoListContract.TodoListColumns.ALARM, addBean.alarm);
                contentValues.put(TodoListContract.TodoListColumns.CREATE_TIME, addBean.createTime);
                contentValues.put(TodoListContract.TodoListColumns.STATUS, addBean.status ? 1 : 0);
                contentValues.put(TodoListContract.TodoListColumns.MARK, addBean.mark);
                getContentResolver().insert(TodoListContract.TodoListColumns.CONTENT_URI, contentValues);
            }
        };
        DbThreadPool.getThreadPool().exeute(updateTask);
    }

    private void updateValue(final TodoListBean updateBean) {
        Runnable updateTask = new Runnable() {
            @Override
            public void run() {
                ContentValues contentValues = new ContentValues();
                contentValues.put(TodoListContract.TodoListColumns.TITLE, updateBean.title);
                contentValues.put(TodoListContract.TodoListColumns.DESCRIPTION, updateBean.description);
                contentValues.put(TodoListContract.TodoListColumns.ALARM, updateBean.alarm);
                contentValues.put(TodoListContract.TodoListColumns.CREATE_TIME, updateBean.createTime);
                contentValues.put(TodoListContract.TodoListColumns.MARK, updateBean.mark);
                getContentResolver().update(TodoListContract.TodoListColumns.CONTENT_URI, contentValues, TodoListContract.TodoListColumns._ID + "=?", new String[]{updateBean.id + ""});
            }
        };
        DbThreadPool.getThreadPool().exeute(updateTask);
        EventBus.getDefault().post(new EditTodoEvent(updateBean.id));
    }

    private void deleteValue(int pos, final int deleteId) {
        Runnable deleteTask = new Runnable() {
            @Override
            public void run() {
                getContentResolver().delete(TodoListContract.TodoListColumns.CONTENT_URI, TodoListContract.TodoListColumns._ID + "=?", new String[]{deleteId + ""});
            }
        };
        DbThreadPool.getThreadPool().exeute(deleteTask);
        EventBus.getDefault().post(new DeleteTodoEvent(pos));
        finish();
    }

    private void queryValue(final int searchId) {
        queryTask = new Runnable() {
            @Override
            public void run() {
                Cursor cursor = getContentResolver().query(TodoListContract.TodoListColumns.CONTENT_URI, null, TodoListContract.TodoListColumns._ID + "=?", new String[]{searchId + ""}, null);
                mTodoBean = new TodoListBean();
                while (cursor.moveToNext()) {
                    mTodoBean.id = cursor.getInt(cursor.getColumnIndex(TodoListContract.TodoListColumns._ID));
                    mTodoBean.sortId = cursor.getInt(cursor.getColumnIndex(TodoListContract.TodoListColumns.SORT_ID));
                    mTodoBean.title = cursor.getString(cursor.getColumnIndex(TodoListContract.TodoListColumns.TITLE));
                    mTodoBean.description = cursor.getString(cursor.getColumnIndex(TodoListContract.TodoListColumns.DESCRIPTION));
                    mTodoBean.alarm = cursor.getLong(cursor.getColumnIndex(TodoListContract.TodoListColumns.ALARM));
                    mTodoBean.createTime = cursor.getLong(cursor.getColumnIndex(TodoListContract.TodoListColumns.CREATE_TIME));
                    mTodoBean.status = cursor.getInt(cursor.getColumnIndex(TodoListContract.TodoListColumns.STATUS)) == 1;
                    mTodoBean.mark = cursor.getInt(cursor.getColumnIndex(TodoListContract.TodoListColumns.MARK));
                }
                mHandler.sendEmptyMessage(1);
            }
        };
        DbThreadPool.getThreadPool().exeute(queryTask);
    }

    @Override
    protected void onDestroy() {
        if (queryTask != null) {
            DbThreadPool.getThreadPool().cancel(queryTask);
        }
        if (manager != null)
            manager.hideSoftInputFromWindow(et_title.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        manager = null;
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.bar_second:
//
//                break;
            default:
                break;
        }
    }
}
