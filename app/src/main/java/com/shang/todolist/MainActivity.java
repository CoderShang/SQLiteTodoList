package com.shang.todolist;

import android.content.ContentValues;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Canvas;
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
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.callback.ItemDragAndSwipeCallback;
import com.chad.library.adapter.base.listener.OnItemDragListener;
import com.chad.library.adapter.base.listener.OnItemSwipeListener;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.shang.todolist.db.DbThreadPool;
import com.shang.todolist.db.TodoListBean;
import com.shang.todolist.db.TodoListContract;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private FloatingActionButton fab;
    private FrameLayout root_layout;
    private MaterialSpinner spinner;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView mRecyclerView;
    private TodoListAdapter mAdapter;
    private ItemTouchHelper mItemTouchHelper;
    private ItemDragAndSwipeCallback mItemDragAndSwipeCallback;
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
        FrameLayout title_bar = findViewById(R.id.title_bar);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        root_layout = findViewById(R.id.root_layout);
        mRecyclerView = findViewById(R.id.rv_todo_list);
        spinner = findViewById(R.id.spinner);
        fab = findViewById(R.id.fab_add);
        ImageView img_github = findViewById(R.id.img_github);
        img_github.setOnClickListener(this);
        title_bar.setOnClickListener(this);
        //初始化下拉刷新布局
        initSuperSwipe();
        //初始化RecyclerView
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new TodoListAdapter(mTodoList);
        View view = new View(this);
        ViewGroup.LayoutParams llp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UiUtils.dip2px(100));
        view.setLayoutParams(llp);
        mAdapter.setFooterView(view);
        mAdapter.bindToRecyclerView(mRecyclerView);
        mAdapter.openLoadAnimation(new CustomAnimation());
        mAdapter.isFirstOnly(false);
        //设置监听
        initListener();
        // 注册数据改变的监听器
        mObserver = new ContentObserver(mHandler) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                super.onChange(selfChange, uri);
                mHandler.sendEmptyMessage(2);
            }
        };
        App.get().getApplicationContext().getContentResolver().registerContentObserver(TodoListContract.TodoListColumns.CONTENT_URI, true, mObserver);
        //查询数据库
        queryValue();
    }

    /**
     * 初始化刷新加载布局
     */
    private void initSuperSwipe() {
        swipeRefresh.setColorSchemeColors(Color.parseColor("#292836"));
        swipeRefresh.setRefreshing(true);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                queryValue();
            }
        });
    }

    private void initListener() {
        spinner.setItems("全部事项", "待办事项", "已办事项");
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                switch (position) {
                    case 0:
                        mStatus = -1;
                        break;
                    case 1:
                        mStatus = 0;
                        break;
                    case 2:
                        mStatus = 1;
                        break;
                    default:
                        break;
                }
                queryValue();
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditActivity.startActivity(MainActivity.this);
            }
        });
        OnItemDragListener listener = new OnItemDragListener() {
            @Override
            public void onItemDragStart(RecyclerView.ViewHolder viewHolder, int pos) {
                BaseViewHolder holder = ((BaseViewHolder) viewHolder);
//                holder.setTextColor(R.id.tv, Color.WHITE);
                swipeRefresh.setEnabled(false);
            }

            @Override
            public void onItemDragMoving(RecyclerView.ViewHolder source, int from, RecyclerView.ViewHolder target, int to) {

            }

            @Override
            public void onItemDragEnd(RecyclerView.ViewHolder viewHolder, int pos) {
                BaseViewHolder holder = ((BaseViewHolder) viewHolder);
//                holder.setTextColor(R.id.tv, Color.BLACK);
                swipeRefresh.setEnabled(true);
            }
        };
        OnItemSwipeListener onItemSwipeListener = new OnItemSwipeListener() {
            @Override
            public void onItemSwipeStart(RecyclerView.ViewHolder viewHolder, int pos) {
                BaseViewHolder holder = ((BaseViewHolder) viewHolder);
//                holder.setTextColor(R.id.tv, Color.WHITE);
                swipeRefresh.setEnabled(false);
            }

            @Override
            public void clearView(RecyclerView.ViewHolder viewHolder, int pos) {
                BaseViewHolder holder = ((BaseViewHolder) viewHolder);
//                holder.setTextColor(R.id.tv, Color.BLACK);
                swipeRefresh.setEnabled(true);
            }

            @Override
            public void onItemSwiped(RecyclerView.ViewHolder viewHolder, int pos) {
                swipeRefresh.setEnabled(true);
            }

            @Override
            public void onItemSwipeMoving(Canvas canvas, RecyclerView.ViewHolder viewHolder, float dX, float dY, boolean isCurrentlyActive) {
//                canvas.drawText("Just some text", 0, 40, paint);
            }
        };
        mItemDragAndSwipeCallback = new ItemDragAndSwipeCallback(mAdapter);
        mItemTouchHelper = new ItemTouchHelper(mItemDragAndSwipeCallback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);

        //mItemDragAndSwipeCallback.setDragMoveFlags(ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.UP | ItemTouchHelper.DOWN);
        mItemDragAndSwipeCallback.setSwipeMoveFlags(ItemTouchHelper.START | ItemTouchHelper.END);
        mAdapter.enableSwipeItem();
        mAdapter.setOnItemSwipeListener(onItemSwipeListener);
        mAdapter.enableDragItem(mItemTouchHelper);
        mAdapter.setOnItemDragListener(listener);
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
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.title_bar:
                mRecyclerView.smoothScrollToPosition(0);
                break;
            case R.id.img_github:
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
