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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.shang.todolist.R;
import com.shang.todolist.UiUtils;
import com.shang.todolist.db.DbThreadPool;
import com.shang.todolist.db.ManifestBean;
import com.shang.todolist.db.TodoBean;
import com.shang.todolist.db.TodoListContract;
import com.shang.todolist.ui.adapter.MainAdapter;
import com.shang.todolist.ui.widget.AddManifestView;
import com.shang.todolist.ui.widget.AddTodoView;
import com.shang.todolist.ui.widget.SoftKeyBoardListener;
import com.shang.todolist.ui.widget.TitleBar;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final int FIRST = 0;
    public static final int SECOND = 1;
    public static final int THIRD = 2;
    private RecyclerView mRecyclerView;
    private MainAdapter mAdapter;
    private TitleBar title_bar;
    private FloatingActionButton fab;
    private TextView btn_github;
    private ImageView btn_settings;
    private DrawerLayout drawer_layout;
    private FrameLayout root_layout;
    private AddTodoView add_todo_view;
    private AddManifestView add_manifest_view;
    private TextView btn_manifest_add;
    private TextView btn_today;
    private TextView tv_today_num;
    private View above_view;
    private int lastPos = -1;
    private List<ManifestBean> mManifestList = new ArrayList<>();
    private SoftKeyBoardListener mSoftKeyBoardListener;
    private int clickId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = findViewById(R.id.rv_manifest);
        title_bar = findViewById(R.id.title_bar);
        above_view = findViewById(R.id.above_view);
        add_todo_view = findViewById(R.id.add_todo_view);
        drawer_layout = findViewById(R.id.drawer_layout);
        root_layout = findViewById(R.id.root_layout);
        btn_github = findViewById(R.id.btn_github);
        btn_settings = findViewById(R.id.btn_settings);
        fab = findViewById(R.id.fab_add);
        title_bar.setTitle(UiUtils.getTodayAndWeekStr());
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
        add_manifest_view.setListener(new AddManifestView.OnAddListener() {
            @Override
            public void onCreated(ManifestBean bean) {
                UiUtils.hideSoftKeyboard(MainActivity.this, add_manifest_view.et_comment);
//                insertValue(bean);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        add_manifest_view.clearUI();
                    }
                }, 500);
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab.hide();
                clickId = view.getId();
                add_todo_view.et_comment.requestFocus();
                UiUtils.showSoftKeyboard(MainActivity.this, add_todo_view.et_comment);
            }
        });
        above_view.setOnClickListener(this);
        btn_github.setOnClickListener(this);
        //初始化默认的Fragment
        switchFragment(0);
        mSoftKeyBoardListener = new SoftKeyBoardListener(this);
        mSoftKeyBoardListener.setListener(new SoftKeyBoardListener.OnSoftKeyBoardChangeListener() {

            @Override
            public void keyBoardShow(int height) {
                above_view.setVisibility(View.VISIBLE);
                if (clickId == R.id.btn_manifest_add) {
                    add_manifest_view.setVisibility(View.VISIBLE);
                    add_manifest_view.et_comment.setSelection(add_manifest_view.et_comment.getText().length());
                } else {
                    add_todo_view.setVisibility(View.VISIBLE);
                    add_todo_view.et_comment.setSelection(add_todo_view.et_comment.getText().length());
                }
            }

            @Override
            public void keyBoardHide(int height) {
                above_view.setVisibility(View.GONE);
                if (clickId == R.id.btn_manifest_add) {
                    add_manifest_view.setVisibility(View.GONE);
                } else {
                    add_todo_view.closePop();
                    add_todo_view.setVisibility(View.GONE);
                }
                fab.show();
            }
        });
        //初始化RecyclerView
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        //如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new MainAdapter(mManifestList);
        mAdapter.setHeaderView(getHeaderView());
        mAdapter.setFooterView(getFooterView());
        mAdapter.bindToRecyclerView(mRecyclerView);
        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                title_bar.setTitle(mManifestList.get(position).name);
                switchFragment(1);
            }
        });
    }

    private View getHeaderView() {
        View item_left_header = LayoutInflater.from(this).inflate(R.layout.item_left_header, null, false);
        btn_today = item_left_header.findViewById(R.id.btn_today);
        tv_today_num = item_left_header.findViewById(R.id.tv_today_num);
        btn_today.setText(UiUtils.getTodayAndWeekStr());
        btn_today.setOnClickListener(this);
        return item_left_header;
    }

    private View getFooterView() {
        View item_left_footer = LayoutInflater.from(this).inflate(R.layout.item_left_footer, null, false);
        btn_manifest_add = item_left_footer.findViewById(R.id.btn_manifest_add);
        btn_manifest_add.setOnClickListener(this);
        return item_left_footer;
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
                contentValues.put(TodoListContract.TodoListColumns.MANIFEST, addBean.manifest);
                getContentResolver().insert(TodoListContract.TodoListColumns.CONTENT_URI_ADD, contentValues);
            }
        };
        DbThreadPool.getThreadPool().exeute(updateTask);
    }

    @Override
    public void onClick(View v) {
        clickId = v.getId();
        switch (v.getId()) {
            case R.id.above_view:
                UiUtils.hideSoftKeyboard(this, add_todo_view.et_comment);
                break;
            case R.id.btn_today:
                title_bar.setTitle(btn_today.getText().toString());
                switchFragment(0);
                break;
            case R.id.btn_manifest_add:
                fab.hide();
                add_manifest_view.et_comment.requestFocus();
                UiUtils.showSoftKeyboard(MainActivity.this, add_manifest_view.et_comment);
                break;
            case R.id.btn_settings:

                break;
            case R.id.btn_github:
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse("https://github.com/CoderShang/SQLiteTodoList");
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
