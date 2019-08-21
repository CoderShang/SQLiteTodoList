package com.shang.todolist;

import android.content.ContentValues;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.shang.todolist.db.DbThreadPool;
import com.shang.todolist.db.TodoListBean;
import com.shang.todolist.db.TodoListContract;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private CoordinatorLayout root_layout;
    private Toolbar mToolbar;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rv_todo_list;
    private TodoListAdapter mAdapter;
    private int mStatus;//查询条件，按照状态查找
    private List<TodoListBean> mTodoList = new ArrayList<>();
    private Runnable queryTask;
    private Runnable updateTask;
    private Runnable deleteTask;
    private ContentObserver mObserver;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (MainActivity.this == null) {
                return false;
            }
            int what = msg.what;
            switch (what) {
                case 1:
                    //接收查询数据库返回的结果，更新UI
                    swipeRefresh.setRefreshing(false);
                    mAdapter.notifyDataSetChanged();
                    break;
                case 2:
                    queryValue();
                default:
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        root_layout = findViewById(R.id.root_layout);
        mToolbar = findViewById(R.id.toolbar);
        //初始化ToolBar
        setSupportActionBar(mToolbar);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_sort:
                        mAdapter.setSort();
                        break;
                    case R.id.action_delete:
                        mAdapter.setDelete();
                        break;
                    case R.id.query_all:
                        mToolbar.setTitle("全部事项");
                        mStatus = -1;
                        queryValue();
                        break;
                    case R.id.query_unfinished:
                        mToolbar.setTitle("待办事项");
                        mStatus = 0;
                        queryValue();
                        break;
                    case R.id.query_finished:
                        mStatus = 1;
                        mToolbar.setTitle("已办事项");
                        queryValue();
                        break;
                    case R.id.action_help:
                        Intent intent = new Intent();
                        intent.setAction("android.intent.action.VIEW");
                        Uri content_url = Uri.parse("https://github.com/CoderShang");
                        intent.setData(content_url);
                        startActivity(intent);
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        FloatingActionButton fab = findViewById(R.id.fab_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditActivity.startActivity(MainActivity.this);
            }
        });
        //初始化下拉刷新布局
        initSuperSwipe();
        //初始化RecyclerView
        rv_todo_list = findViewById(R.id.rv_todo_list);
        rv_todo_list.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new TodoListAdapter(mTodoList);
        View view = new View(this);
        ViewGroup.LayoutParams llp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UiUtils.dip2px(100));
        view.setLayoutParams(llp);
        mAdapter.setFooterView(view);
        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                EditActivity.startActivity(MainActivity.this, mTodoList.get(position).id);
            }
        });
        mAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                switch (view.getId()) {
                    case R.id.iv_delete:
                        Snackbar.make(root_layout, "删除", Snackbar.LENGTH_LONG).show();
                        break;
                    case R.id.iv_drag:
                        Snackbar.make(root_layout, "排序", Snackbar.LENGTH_LONG).show();
                        break;
                    case R.id.cb_status:
                        Snackbar.make(root_layout, "checkBox", Snackbar.LENGTH_LONG).show();
                        break;
                    default:
                }
            }
        });
        mAdapter.bindToRecyclerView(rv_todo_list);
        mAdapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        mAdapter.isFirstOnly(false);
        // 注册数据改变的监听器
        mObserver = new ContentObserver(mHandler) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                super.onChange(selfChange, uri);
                mHandler.sendEmptyMessage(2);
            }
        };
        App.get().getApplicationContext().getContentResolver().registerContentObserver(TodoListContract.TodoListColumns.CONTENT_URI, true, mObserver);
        queryValue();
    }

    /**
     * 初始化刷新加载布局
     */
    private void initSuperSwipe() {
        swipeRefresh.setColorSchemeColors(Color.parseColor("#282624"));
        swipeRefresh.setRefreshing(true);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                queryValue();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * 查询数据库，更新列表
     */
    private void queryValue() {
        swipeRefresh.setRefreshing(true);
        queryTask = new Runnable() {
            @Override
            public void run() {
                Cursor cursor;
                if (mStatus == -1) {
                    cursor = getContentResolver().query(TodoListContract.TodoListColumns.CONTENT_URI, null, null, null, null);
                } else {
                    cursor = getContentResolver().query(TodoListContract.TodoListColumns.CONTENT_URI, null, TodoListContract.TodoListColumns.STATUS + "=?", new String[]{mStatus + ""}, null);
                }
                mTodoList.clear();
                while (cursor.moveToNext()) {
                    TodoListBean bean = new TodoListBean();
                    bean.id = cursor.getInt(cursor.getColumnIndex(TodoListContract.TodoListColumns._ID));
                    bean.orderId = cursor.getInt(cursor.getColumnIndex(TodoListContract.TodoListColumns.ORDER_ID));
                    bean.title = cursor.getString(cursor.getColumnIndex(TodoListContract.TodoListColumns.TITLE));
                    bean.description = cursor.getString(cursor.getColumnIndex(TodoListContract.TodoListColumns.DESCRIPTION));
                    bean.alarm = cursor.getLong(cursor.getColumnIndex(TodoListContract.TodoListColumns.ALARM));
                    bean.createTime = cursor.getLong(cursor.getColumnIndex(TodoListContract.TodoListColumns.CREATE_TIME));
                    bean.status = cursor.getInt(cursor.getColumnIndex(TodoListContract.TodoListColumns.STATUS));
                    bean.mark = cursor.getInt(cursor.getColumnIndex(TodoListContract.TodoListColumns.MARK));
                    mTodoList.add(bean);
                }
                mHandler.sendEmptyMessage(1);
            }
        };
        DbThreadPool.getThreadPool().exeute(queryTask);
    }

    /**
     * 更改排序
     *
     * @param updateBean
     */
    private void updateValue(final TodoListBean updateBean) {
        updateTask = new Runnable() {
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
    }

    /**
     * 批量删除
     */
    private void deleteValue(final int id) {
        deleteTask = new Runnable() {
            @Override
            public void run() {
                getContentResolver().delete(TodoListContract.TodoListColumns.CONTENT_URI, TodoListContract.TodoListColumns._ID + "=?", new String[]{id + ""});
            }
        };
        DbThreadPool.getThreadPool().exeute(deleteTask);
    }

    @Override
    protected void onDestroy() {
        if (queryTask != null) {
            DbThreadPool.getThreadPool().cancel(queryTask);
        }
        if (updateTask != null) {
            DbThreadPool.getThreadPool().cancel(updateTask);
        }
        if (deleteTask != null) {
            DbThreadPool.getThreadPool().cancel(deleteTask);
        }
        App.get().getApplicationContext().getContentResolver().unregisterContentObserver(mObserver);
        super.onDestroy();
    }
}
