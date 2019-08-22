package com.shang.todolist;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.shang.todolist.db.DbThreadPool;
import com.shang.todolist.db.TodoListBean;
import com.shang.todolist.db.TodoListContract;

import org.greenrobot.eventbus.EventBus;

import java.util.Date;

/**
 * 新增 & 编辑 待办事项的页面
 */
public class EditActivity extends AppCompatActivity {
    public static final String KEY_ID = "ID";
    public static final String KEY_MAX_ID = "MAX_ID";
    private Toolbar mToolbar;
    private EditText et_title, et_desc;
    private TextView tv_alarm;
    private CheckBox cb_mark;
    private RelativeLayout top_layout;
    private LinearLayout root_layout;
    private int searchId = -1; //主键ID，0说明是新添加，有值则编辑
    private int maxId = -1; //添加orderId时，直接+1
    private TodoListBean mTodoBean;
    private Runnable queryTask;

    public static void startActivity(Context context, int searchId, int maxId) {
        Intent intent = new Intent(context, EditActivity.class);
        intent.putExtra(KEY_ID, searchId);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, int maxId) {
        Intent intent = new Intent(context, EditActivity.class);
        intent.putExtra(KEY_MAX_ID, maxId);
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
        mToolbar = findViewById(R.id.toolbar);
        top_layout = findViewById(R.id.top_layout);
        root_layout = findViewById(R.id.root_layout);
        et_title = findViewById(R.id.et_title);
        et_desc = findViewById(R.id.et_desc);
        tv_alarm = findViewById(R.id.tv_alarm);
        cb_mark = findViewById(R.id.cb_mark);
        setSupportActionBar(mToolbar);
        Intent intent = getIntent();
        if (intent != null) {
            searchId = intent.getIntExtra(KEY_ID, -1);
            maxId = intent.getIntExtra(KEY_MAX_ID, 0);
        }
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //设置点击事件
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_delete:
                        deleteValue(searchId);
                        break;
                    case R.id.action_done:
                        submit();
                        break;
                    default:
                }
                return false;
            }
        });
        if (searchId != -1) {
            queryValue(searchId);
        }
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
            insertValue(new TodoListBean(searchId, 0, 0, title, desc, alarm, new Date().getTime(), false, isMark));
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
                contentValues.put(TodoListContract.TodoListColumns.FRONT_ID, addBean.frontId);
                contentValues.put(TodoListContract.TodoListColumns.BEHIND_ID, addBean.behindId);
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
        EventBus.getDefault().post(new RefreshEvent(updateBean.id));
    }

    private void deleteValue(final int deleteId) {
        Runnable deleteTask = new Runnable() {
            @Override
            public void run() {
                getContentResolver().delete(TodoListContract.TodoListColumns.CONTENT_URI, TodoListContract.TodoListColumns._ID + "=?", new String[]{deleteId + ""});
            }
        };
        DbThreadPool.getThreadPool().exeute(deleteTask);
        EventBus.getDefault().post(new RefreshEvent(deleteId));
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
                    mTodoBean.frontId = cursor.getInt(cursor.getColumnIndex(TodoListContract.TodoListColumns.FRONT_ID));
                    mTodoBean.behindId = cursor.getInt(cursor.getColumnIndex(TodoListContract.TodoListColumns.BEHIND_ID));
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
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);

        if (searchId != -1) {
            mToolbar.setTitle("编辑");
            menu.findItem(R.id.action_delete).setVisible(true);
        } else {
            mToolbar.setTitle("新增一个待办");
            menu.findItem(R.id.action_delete).setVisible(false);
        }
        return true;
    }

    @Override
    public void finish() {
        InputMethodManager manager = ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE));
        if (manager != null)
            manager.hideSoftInputFromWindow(et_title.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        super.finish();
    }
}
