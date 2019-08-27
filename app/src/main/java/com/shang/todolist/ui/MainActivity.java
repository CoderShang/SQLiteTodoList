package com.shang.todolist.ui;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.shang.todolist.R;
import com.shang.todolist.UiUtils;
import com.shang.todolist.db.DbThreadPool;
import com.shang.todolist.db.TodoBean;
import com.shang.todolist.db.TodoListContract;
import com.shang.todolist.ui.widget.AddTodoView;
import com.shang.todolist.ui.widget.SoftKeyBoardListener;
import com.shang.todolist.ui.widget.TitleBar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final int FIRST = 0;
    public static final int SECOND = 1;
    public static final int THIRD = 2;
    private TitleBar title_bar;
    private FloatingActionButton fab;
    private TextView btn_github, btn_today, btn_plan;
    private DrawerLayout drawer_layout;
    private FrameLayout root_layout;
    private AddTodoView add_todo_view;
    private View above_view;
    private int lastPos = -1;
    private SoftKeyBoardListener mSoftKeyBoardListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        title_bar = findViewById(R.id.title_bar);
        above_view = findViewById(R.id.above_view);
        add_todo_view = findViewById(R.id.add_todo_view);
        drawer_layout = findViewById(R.id.drawer_layout);
        root_layout = findViewById(R.id.root_layout);
        btn_github = findViewById(R.id.btn_github);
        btn_today = findViewById(R.id.btn_today);
        btn_plan = findViewById(R.id.btn_plan);
        btn_today.setText("今天  |  " + UiUtils.getTodayAndWeekStr());
        fab = findViewById(R.id.fab_add);
        title_bar.setTitle("今天  |  " + UiUtils.getTodayAndWeekStr());
        title_bar.setImgLeft(R.drawable.ic_todo);
        title_bar.setImgRight(R.drawable.ic_alarm);
        title_bar.setListener(new TitleBar.TitleBarListener() {
            @Override
            public void barLeft() {
                drawer_layout.openDrawer(Gravity.LEFT);
            }

            @Override
            public void barSecond() {

            }

            @Override
            public void barRight() {
            }
        });
        add_todo_view.setListener(new AddTodoView.OnAddListener() {
            @Override
            public void onCreated(TodoBean bean) {
                UiUtils.hideSoftKeyboard(MainActivity.this, add_todo_view.et_comment);
                insertValue(bean);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        add_todo_view.clearUI();
                    }
                }, 500);
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab.hide();
                add_todo_view.et_comment.requestFocus();
                UiUtils.showSoftKeyboard(MainActivity.this, add_todo_view.et_comment);
//                new XPopup.Builder(MainActivity.this)
//                        .moveUpToKeyboard(false) //如果不加这个，评论弹窗会移动到软键盘上面
//                        .asCustom(new ZhihuCommentPopup(MainActivity.this)/*.enableDrag(false)*/)
//                        .show();
            }
        });
        above_view.setOnClickListener(this);
        btn_github.setOnClickListener(this);
        btn_today.setOnClickListener(this);
        btn_plan.setOnClickListener(this);
        //初始化默认的Fragment
        switchFragment(0);
        mSoftKeyBoardListener = new SoftKeyBoardListener(this);
        mSoftKeyBoardListener.setListener(new SoftKeyBoardListener.OnSoftKeyBoardChangeListener() {

            @Override
            public void keyBoardShow(int height) {
//                add_todo_view.et_comment.setSelection(add_todo_view.et_comment.getText().length());
                above_view.setVisibility(View.VISIBLE);
                add_todo_view.setVisibility(View.VISIBLE);
            }

            @Override
            public void keyBoardHide(int height) {
                above_view.setVisibility(View.GONE);
                add_todo_view.setVisibility(View.GONE);
                fab.show();
            }
        });
    }


    private void switchFragment(int position) {
        drawer_layout.closeDrawer(Gravity.LEFT);
        if (position == lastPos) {
            return;
        }
        lastPos = position;
        Fragment fragment = null;
        switch (position) {
            case FIRST:
                fragment = TodoFragment.newInstance(0);
                break;
            case SECOND:
                fragment = TodoFragment.newInstance(0);
                break;
            case THIRD:
                fragment = TodoFragment.newInstance(0);
                break;
            default:
                break;
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.main_container, fragment).commitAllowingStateLoss();
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
                getContentResolver().insert(TodoListContract.TodoListColumns.CONTENT_URI, contentValues);
            }
        };
        DbThreadPool.getThreadPool().exeute(updateTask);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.above_view:
                UiUtils.hideSoftKeyboard(this, add_todo_view.et_comment);
                break;
            case R.id.btn_today:
                title_bar.setTitle(btn_today.getText().toString());
                switchFragment(0);
                break;
            case R.id.btn_plan:
                title_bar.setTitle(btn_plan.getText().toString());
                switchFragment(1);
                break;
            case R.id.btn_github:
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse("https://github.com/CoderShang");
                intent.setData(content_url);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        if (mSoftKeyBoardListener != null) {
            mSoftKeyBoardListener.removeListener(this);
        }
        super.onDestroy();
    }
}
