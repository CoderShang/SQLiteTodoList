package com.shang.todolist;

import android.content.ContentValues;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.callback.ItemDragAndSwipeCallback;
import com.chad.library.adapter.base.listener.OnItemDragListener;
import com.chad.library.adapter.base.listener.OnItemSwipeListener;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.shang.todolist.db.DbThreadPool;
import com.shang.todolist.db.TodoListBean;
import com.shang.todolist.db.TodoListContract;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class TodoFragment extends BaseFragment implements View.OnClickListener {
    private FrameLayout title_bar;
    private ImageView btn_open, btn_settings;
    private FloatingActionButton fab;
    private TextView btn_github;
    private DrawerLayout drawer_layout;
    private FrameLayout root_layout;
    private MaterialSpinner spinner;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView mRecyclerView;
    private TodoListAdapter mAdapter;
    private ItemTouchHelper mItemTouchHelper;
    private ItemDragAndSwipeCallback mItemDragAndSwipeCallback;
    private int mStatus = -1;//查询条件，按照状态查找
    private int mEditPos = -1;//被点击编辑的Item位置
    // 当前数据库最大的序号，应该SELECT MAX(column_name) FROM table_name ，
    // 但为了省事，直接取集合长度了...，传给新增页面，直接+1 使用
    private int mListSize;
    private List<TodoListBean> mTodoList = new ArrayList<>();
    private ContentObserver mObserver;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (getActivity() == null) {
                return false;
            }
            int what = msg.what;
            switch (what) {
                case 1:
                    //接收查询数据库返回的结果，更新UI
                    swipeRefresh.setRefreshing(false);
                    mAdapter.setNewData(mTodoList);
                    break;
                case 2:
                    if (mTodoList.size() != 0) {
                        mRecyclerView.smoothScrollToPosition(mTodoList.size() - 1);
                    }
                    queryValue();
                    break;
                case 3:
                    if (mEditPos == -1) {
                        break;
                    }
                    TodoListBean bean = (TodoListBean) msg.obj;
                    if (bean == null) {
                        mAdapter.remove(mEditPos);
                    } else {
                        mAdapter.setData(mEditPos, bean);
                    }
                    break;
                default:
            }
            return false;
        }
    });

    @Override
    protected int setLayout() {
        return R.layout.fragment_todo;
    }

    @Override
    protected void initView() {
        title_bar = find(R.id.title_bar);
        btn_open = find(R.id.btn_open);
        drawer_layout = find(R.id.drawer_layout);
        btn_settings = find(R.id.btn_settings);
        btn_github = find(R.id.btn_github);
        swipeRefresh = find(R.id.swipe_refresh);
        root_layout = find(R.id.root_layout);
        mRecyclerView = find(R.id.rv_todo_list);
        spinner = find(R.id.spinner);
        fab = find(R.id.fab_add);
        title_bar.setOnClickListener(this);
        btn_github.setOnClickListener(this);
        btn_settings.setOnClickListener(this);
        btn_open.setOnClickListener(this);
        //初始化下拉刷新布局
        initSuperSwipe();
    }

    @Override
    protected void initData() {
        //初始化RecyclerView
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new TodoListAdapter(mTodoList);
        View emptyView = new View(getActivity());
        ViewGroup.LayoutParams llp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, UiUtils.dip2px(100));
        emptyView.setLayoutParams(llp);
        mAdapter.setFooterView(emptyView);
        mAdapter.bindToRecyclerView(mRecyclerView);
        mAdapter.openLoadAnimation(new CustomAnimation());
        mAdapter.isFirstOnly(false);
        //设置监听
        initListener();
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

    private int fromPos, toPos;//记录排序from 和 to 的id

    private void initListener() {
        // 注册数据改变的监听器
        mObserver = new ContentObserver(mHandler) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                mHandler.sendEmptyMessage(2);
            }
        };
        App.get().getApplicationContext().getContentResolver().registerContentObserver(TodoListContract.TodoListColumns.CONTENT_URI, true, mObserver);
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
                if (mTodoList != null) {
                    mListSize = mTodoList.size();
                } else {
                    mListSize = 0;
                }
                EditActivity.startActivity(getActivity(), mListSize);
            }
        });
        OnItemDragListener listener = new OnItemDragListener() {
            @Override
            public void onItemDragStart(RecyclerView.ViewHolder viewHolder, int pos) {
                swipeRefresh.setEnabled(false);
                fromPos = pos;
            }

            @Override
            public void onItemDragMoving(RecyclerView.ViewHolder source, int from, RecyclerView.ViewHolder target, int to) {
            }

            @Override
            public void onItemDragEnd(RecyclerView.ViewHolder viewHolder, int pos) {
                swipeRefresh.setEnabled(true);
                toPos = pos;
                if (toPos == fromPos)
                    return;
                //TODO 排序ID对调
                TodoListBean fromBean = mTodoList.get(toPos);
                TodoListBean toBean = mTodoList.get(fromPos);
                int tempId = fromBean.frontId;
                fromBean.frontId = toBean.frontId;
                toBean.frontId = tempId;
                updateSort(fromBean, toBean);
            }
        };
        OnItemSwipeListener onItemSwipeListener = new OnItemSwipeListener() {
            @Override
            public void onItemSwipeStart(RecyclerView.ViewHolder viewHolder, int pos) {
                Log.d(TAG, "view swiped start: " + pos);
                BaseViewHolder holder = ((BaseViewHolder) viewHolder);
//                holder.setTextColor(R.id.tv, Color.WHITE);
                swipeRefresh.setEnabled(false);
            }

            @Override
            public void clearView(RecyclerView.ViewHolder viewHolder, int pos) {
                Log.d(TAG, "View reset: " + pos);
                BaseViewHolder holder = ((BaseViewHolder) viewHolder);
//                holder.setTextColor(R.id.tv, Color.BLACK);
                swipeRefresh.setEnabled(true);
            }

            @Override
            public void onItemSwiped(RecyclerView.ViewHolder viewHolder, int pos) {
                Log.d(TAG, "View Swiped: " + pos);
                swipeRefresh.setEnabled(true);
            }

            @Override
            public void onItemSwipeMoving(Canvas canvas, RecyclerView.ViewHolder viewHolder, float dX, float dY, boolean isCurrentlyActive) {
//                canvas.drawColor(ContextCompat.getColor(ItemDragAndSwipeUseActivity.this, R.color.color_light_blue));
//                canvas.drawText("Just some text", 0, 40, paint);
            }
        };
        mItemDragAndSwipeCallback = new ItemDragAndSwipeCallback(mAdapter);
        mItemTouchHelper = new ItemTouchHelper(mItemDragAndSwipeCallback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);
        //mItemDragAndSwipeCallback.setDragMoveFlags(ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.UP | ItemTouchHelper.DOWN);
        mItemDragAndSwipeCallback.setSwipeMoveFlags(ItemTouchHelper.START | ItemTouchHelper.END);
        mAdapter.enableSwipeItem();
        mAdapter.enableDragItem(mItemTouchHelper);
        mAdapter.setOnItemSwipeListener(onItemSwipeListener);
        mAdapter.setOnItemDragListener(listener);
        mAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                mEditPos = position;
                switch (view.getId()) {
                    case R.id.card_view:
                        EditActivity.startActivity(getActivity(), mTodoList.get(position).id, mListSize);
                        break;
//                    case R.id.btn_delete:
//                        deleteValue(mTodoList.get(position).id);
//                        break;
                    case R.id.cb_status:
                        TodoListBean bean = mTodoList.get(position);
                        if (bean != null) {
                            bean.status = !bean.status;
                            updateStatus(bean);
                        }
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
            case R.id.btn_open:
                drawer_layout.openDrawer(Gravity.LEFT);
                break;
            case R.id.btn_settings:
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

    /**
     * 查询数据库，更新列表
     */
    private void queryValue() {
        swipeRefresh.setRefreshing(true);
        Runnable queryTask = new Runnable() {
            @Override
            public void run() {
                Cursor cursor;
                if (mStatus == -1) {
                    cursor = App.get().getContentResolver().query(TodoListContract.TodoListColumns.CONTENT_URI, null, null, null, TodoListContract.TodoListColumns.FRONT_ID);
                } else {
                    cursor = App.get().getContentResolver().query(TodoListContract.TodoListColumns.CONTENT_URI, null, TodoListContract.TodoListColumns.STATUS + "=?", new String[]{mStatus + ""}, TodoListContract.TodoListColumns.FRONT_ID);
                }
                mTodoList.clear();
                while (cursor.moveToNext()) {
                    TodoListBean bean = new TodoListBean();
                    bean.id = cursor.getInt(cursor.getColumnIndex(TodoListContract.TodoListColumns._ID));
                    bean.frontId = cursor.getInt(cursor.getColumnIndex(TodoListContract.TodoListColumns.FRONT_ID));
                    bean.behindId = cursor.getInt(cursor.getColumnIndex(TodoListContract.TodoListColumns.BEHIND_ID));
                    bean.title = cursor.getString(cursor.getColumnIndex(TodoListContract.TodoListColumns.TITLE));
                    bean.description = cursor.getString(cursor.getColumnIndex(TodoListContract.TodoListColumns.DESCRIPTION));
                    bean.alarm = cursor.getLong(cursor.getColumnIndex(TodoListContract.TodoListColumns.ALARM));
                    bean.createTime = cursor.getLong(cursor.getColumnIndex(TodoListContract.TodoListColumns.CREATE_TIME));
                    bean.status = cursor.getInt(cursor.getColumnIndex(TodoListContract.TodoListColumns.STATUS)) == 1;
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
     */
    private void updateSort(final TodoListBean fromBean, final TodoListBean toBean) {
        Runnable updateTask = new Runnable() {
            @Override
            public void run() {
//                ContentValues contentValues1 = new ContentValues();
//                contentValues1.put(TodoListContract.TodoListColumns.SORT_ID, fromBean.sortId);
//                ContentValues contentValues2 = new ContentValues();
//                contentValues2.put(TodoListContract.TodoListColumns.SORT_ID, toBean.sortId);
//                App.get().getContentResolver().update(TodoListContract.TodoListColumns.CONTENT_URI, contentValues1, TodoListContract.TodoListColumns._ID + "=?", new String[]{fromBean.id + ""});
//                App.get().getContentResolver().update(TodoListContract.TodoListColumns.CONTENT_URI, contentValues2, TodoListContract.TodoListColumns._ID + "=?", new String[]{toBean.id + ""});
            }
        };
        DbThreadPool.getThreadPool().exeute(updateTask);
    }

    /**
     * 标记状态
     *
     * @param updateBean
     */
    private void updateStatus(final TodoListBean updateBean) {
        Runnable updateTask = new Runnable() {
            @Override
            public void run() {
                ContentValues contentValues = new ContentValues();
                contentValues.put(TodoListContract.TodoListColumns.STATUS, updateBean.status ? 1 : 0);
                App.get().getContentResolver().update(TodoListContract.TodoListColumns.CONTENT_URI, contentValues, TodoListContract.TodoListColumns._ID + "=?", new String[]{updateBean.id + ""});
            }
        };
        DbThreadPool.getThreadPool().exeute(updateTask);
    }

    /**
     * 根据ID查询
     *
     * @param searchId
     */
    private void queryById(final int searchId) {
        Runnable queryTask = new Runnable() {
            @Override
            public void run() {
                Cursor cursor = App.get().getContentResolver().query(TodoListContract.TodoListColumns.CONTENT_URI, null, TodoListContract.TodoListColumns._ID + "=?", new String[]{searchId + ""}, null);
                TodoListBean bean = null;
                while (cursor.moveToNext()) {
                    bean = new TodoListBean();
                    bean.id = cursor.getInt(cursor.getColumnIndex(TodoListContract.TodoListColumns._ID));
                    bean.frontId = cursor.getInt(cursor.getColumnIndex(TodoListContract.TodoListColumns.FRONT_ID));
                    bean.behindId = cursor.getInt(cursor.getColumnIndex(TodoListContract.TodoListColumns.BEHIND_ID));
                    bean.title = cursor.getString(cursor.getColumnIndex(TodoListContract.TodoListColumns.TITLE));
                    bean.description = cursor.getString(cursor.getColumnIndex(TodoListContract.TodoListColumns.DESCRIPTION));
                    bean.alarm = cursor.getLong(cursor.getColumnIndex(TodoListContract.TodoListColumns.ALARM));
                    bean.createTime = cursor.getLong(cursor.getColumnIndex(TodoListContract.TodoListColumns.CREATE_TIME));
                    bean.status = cursor.getInt(cursor.getColumnIndex(TodoListContract.TodoListColumns.STATUS)) == 1;
                    bean.mark = cursor.getInt(cursor.getColumnIndex(TodoListContract.TodoListColumns.MARK));
                }
                Message msg = Message.obtain();
                msg.what = 3;
                msg.obj = bean;
                mHandler.sendMessage(msg);
            }
        };
        DbThreadPool.getThreadPool().exeute(queryTask);
    }

    /**
     * 单条删除
     */
    private void deleteValue(final int id) {
        Runnable deleteTask = new Runnable() {
            @Override
            public void run() {
                App.get().getContentResolver().delete(TodoListContract.TodoListColumns.CONTENT_URI, TodoListContract.TodoListColumns._ID + "=?", new String[]{id + ""});
            }
        };
        DbThreadPool.getThreadPool().exeute(deleteTask);
        //它删它的，我更新我的UI，互不影响
        mAdapter.remove(mEditPos);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RefreshEvent event) {
        if (event != null) {
            queryById(event.id);
        }
    }

    @Override
    public void onDestroyView() {
        App.get().getApplicationContext().getContentResolver().unregisterContentObserver(mObserver);
        super.onDestroyView();
    }
}
