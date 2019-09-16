package com.shang.todolist.ui;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.listener.OnItemDragListener;
import com.chad.library.adapter.base.listener.OnItemSwipeListener;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.XPopupCallback;
import com.shang.todolist.App;
import com.shang.todolist.R;
import com.shang.todolist.UiUtils;
import com.shang.todolist.event.ClearAllEvent;
import com.shang.todolist.event.DeleteTodoEvent;
import com.shang.todolist.event.InsertTodoEvent;
import com.shang.todolist.ui.adapter.CustomAnimation;
import com.shang.todolist.ui.adapter.ItemDragAndSwipeCallback;
import com.shang.todolist.ui.adapter.SpaceItemDecoration;
import com.shang.todolist.ui.adapter.TodoListAdapter;
import com.shang.todolist.db.DbThreadPool;
import com.shang.todolist.db.TodoBean;
import com.shang.todolist.db.TodoListContract;
import com.shang.todolist.event.EditTodoEvent;
import com.shang.todolist.ui.widget.AddTodoPopup;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class TodoFragment extends BaseFragment {
    public static final String KEY_MANIFEST_ID = "MANIFEST_ID";
    public static final int WHAT_QUERY = 1;
    public static final int WHAT_QUERY_BYID = 2;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView mRecyclerView;
    private LinearLayout emptyView;
    private LinearLayout emptyToday;
    private TodoListAdapter mAdapter;
    private ItemTouchHelper mItemTouchHelper;
    private ItemDragAndSwipeCallback mItemDragAndSwipeCallback;
    private int mEditPos = -1;//被点击编辑的Item位置
    private long manifestId;//外面传递过来的清单ID，做查询条件
    private List<TodoBean> mTodoList = new ArrayList<>();
    private ContentObserver mObserver;
    private int fromPos;//记录排序from 和 to 的位置
    private long deleteId;//记录准备删除的ID主键
    private Runnable queryTask;

    /**
     * 构造方法
     *
     * @param mId 清单ID（待办存在于一个清单当中）
     * @return
     */
    public static TodoFragment newInstance(long mId) {
        Bundle args = new Bundle();
        args.putLong(KEY_MANIFEST_ID, mId);
        TodoFragment fragment = new TodoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (mContext == null) {
                return false;
            }
            int what = msg.what;
            switch (what) {
                case WHAT_QUERY:
                    //接收查询数据库返回的结果，更新UI
                    swipeRefresh.setRefreshing(false);
                    mAdapter.setNewData(mTodoList);
                    if (manifestId == 0) {
                        if (mTodoList.size() == 0) {
                            emptyToday.setVisibility(View.VISIBLE);
                        } else {
                            emptyToday.setVisibility(View.GONE);
                        }
                    } else {
                        if (mTodoList.size() == 0) {
                            emptyView.setVisibility(View.VISIBLE);
                        } else {
                            emptyView.setVisibility(View.GONE);
                        }
                    }
                    break;
                case WHAT_QUERY_BYID:
                    if (mEditPos == -1) {
                        break;
                    }
                    TodoBean bean = (TodoBean) msg.obj;
                    if (bean != null) {
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
        emptyView = find(R.id.empty_view);
        emptyToday = find(R.id.empty_today);
        swipeRefresh = find(R.id.swipe_refresh);
        //初始化下拉刷新布局
        swipeRefresh.setColorSchemeColors(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                queryValue();
            }
        });
        mRecyclerView = find(R.id.rv_todo_list);

        //初始化RecyclerView
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        //如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new SpaceItemDecoration(UiUtils.dip2px(5), 0, UiUtils.dip2px(5), 0, UiUtils.dip2px(5)));
    }

    @Override
    protected void initData() {
        mAdapter = new TodoListAdapter(mTodoList);
        View footer = new View(mContext);
        ViewGroup.LayoutParams llp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, UiUtils.dip2px(100));
        footer.setLayoutParams(llp);
        mAdapter.setFooterView(footer);
        mAdapter.bindToRecyclerView(mRecyclerView);
        mAdapter.openLoadAnimation(new CustomAnimation());
        //设置监听
        initListener();
        Bundle bundle = getArguments();
        if (bundle != null) {
            manifestId = bundle.getLong(KEY_MANIFEST_ID);
        }
        //查询数据库
        queryValue();
    }

    private void initListener() {
        //排序的监听
        OnItemDragListener listener = new OnItemDragListener() {
            @Override
            public void onItemDragStart(RecyclerView.ViewHolder viewHolder, int pos) {
                swipeRefresh.setEnabled(false);
                BaseViewHolder holder = ((BaseViewHolder) viewHolder);
                holder.setGone(R.id.iv_sort, true);
                holder.setGone(R.id.cb_status, false);
                fromPos = pos;
            }

            @Override
            public void onItemDragMoving(RecyclerView.ViewHolder source, int from, RecyclerView.ViewHolder target, int to) {
            }

            @Override
            public void onItemDragEnd(RecyclerView.ViewHolder viewHolder, int pos) {
                swipeRefresh.setEnabled(true);
                BaseViewHolder holder = ((BaseViewHolder) viewHolder);
                holder.setGone(R.id.iv_sort, false);
                holder.setGone(R.id.cb_status, true);
                if (fromPos < pos) {
                    updateSort(fromPos, pos);
                } else if (pos < fromPos) {
                    updateSort(pos, fromPos);
                }
            }
        };
        //侧滑删除的监听
        OnItemSwipeListener onItemSwipeListener = new OnItemSwipeListener() {
            @Override
            public void onItemSwipeStart(RecyclerView.ViewHolder viewHolder, int pos) {
                swipeRefresh.setEnabled(false);
                BaseViewHolder holder = ((BaseViewHolder) viewHolder);
                holder.setGone(R.id.iv_delete, true);
                holder.setGone(R.id.cb_status, false);
                deleteId = mTodoList.get(pos).id;
            }

            @Override
            public void clearView(RecyclerView.ViewHolder viewHolder, int pos) {
                swipeRefresh.setEnabled(true);
                BaseViewHolder holder = ((BaseViewHolder) viewHolder);
                holder.setGone(R.id.iv_delete, false);
                holder.setGone(R.id.cb_status, true);
                deleteId = 0;
            }

            @Override
            public void onItemSwiped(RecyclerView.ViewHolder viewHolder, int pos) {
                swipeRefresh.setEnabled(true);
                deleteValue(deleteId);
                if (pos < mTodoList.size()) {
                    updateSort(pos, mTodoList.size() - 1);
                }
                if (mTodoList.size() == 0) {
                    queryValue();
                }
            }

            @Override
            public void onItemSwipeMoving(Canvas canvas, RecyclerView.ViewHolder viewHolder, float dX, float dY, boolean isCurrentlyActive) {
//                canvas.drawColor(ContextCompat.getColor(mContext, R.color.colorAccent));
//                canvas.drawText("Just some text", 0, 40, paint);
            }
        };
        mItemDragAndSwipeCallback = new ItemDragAndSwipeCallback(mAdapter);
        mItemTouchHelper = new ItemTouchHelper(mItemDragAndSwipeCallback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);
        mItemDragAndSwipeCallback.setDragMoveFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN);
        mItemDragAndSwipeCallback.setSwipeMoveFlags(ItemTouchHelper.START);
        mAdapter.enableSwipeItem();
        mAdapter.enableDragItem(mItemTouchHelper);
        mAdapter.setOnItemSwipeListener(onItemSwipeListener);
        mAdapter.setOnItemDragListener(listener);
        mAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, final int position) {
                mEditPos = position;
                switch (view.getId()) {
                    case R.id.card_view:
                        AddTodoPopup pop = new AddTodoPopup(mContext, mTodoList.get(mEditPos), mEditPos);
                        new XPopup.Builder(mContext)
                                .autoOpenSoftInput(true)
                                .setPopupCallback(new XPopupCallback() {
                                    @Override
                                    public void onCreated() {

                                    }

                                    @Override
                                    public void onShow() {

                                    }

                                    @Override
                                    public void onDismiss() {
                                    }

                                    @Override
                                    public boolean onBackPressed() {
                                        return false;
                                    }
                                })
                                .asCustom(pop).show();
                        break;
                    case R.id.cb_status:
                        //修改状态
                        updateStatus(mTodoList.get(position));
                        mAdapter.notifyItemChanged(position);
                        break;
                    default:
                }
            }
        });
    }

    /**
     * 查询数据库，更新列表
     */
    private void queryValue() {
        if (queryTask == null) {
            queryTask = new Runnable() {
                @Override
                public void run() {
                    Cursor cursor = App.get().getContentResolver().query(TodoListContract.TodoListColumns.CONTENT_URI, null, TodoListContract.TodoListColumns.MANIFEST + "=?", new String[]{String.valueOf(manifestId)}, TodoListContract.TodoListColumns.SORT_ID);
                    mTodoList.clear();
                    while (cursor.moveToNext()) {
                        TodoBean bean = new TodoBean();
                        bean.id = cursor.getLong(cursor.getColumnIndex(TodoListContract.TodoListColumns._ID));
                        bean.sortId = cursor.getInt(cursor.getColumnIndex(TodoListContract.TodoListColumns.SORT_ID));
                        bean.title = cursor.getString(cursor.getColumnIndex(TodoListContract.TodoListColumns.TITLE));
                        bean.description = cursor.getString(cursor.getColumnIndex(TodoListContract.TodoListColumns.DESCRIPTION));
                        bean.alarm = cursor.getLong(cursor.getColumnIndex(TodoListContract.TodoListColumns.ALARM));
                        bean.remind = cursor.getInt(cursor.getColumnIndex(TodoListContract.TodoListColumns.REMIND));
                        bean.status = cursor.getInt(cursor.getColumnIndex(TodoListContract.TodoListColumns.STATUS)) == 1;
                        bean.mark = cursor.getInt(cursor.getColumnIndex(TodoListContract.TodoListColumns.MARK));
                        mTodoList.add(bean);
                    }
                    mHandler.sendEmptyMessage(WHAT_QUERY);
                }
            };
        }
        DbThreadPool.getThreadPool().exeute(queryTask);
    }

    /**
     * 更改排序
     * 从变更的开始到结束中间全部的数据都要改，很费时间，但没别的好办法
     * 只能让数据库和前台表的下标保持一致了
     */
    private void updateSort(final int from, final int to) {
        Runnable updateTask = new Runnable() {
            @Override
            public void run() {
                if (mTodoList == null || mTodoList.size() == 0 || mTodoList.size() - 1 < to)
                    return;
                ContentResolver resolver = App.get().getContentResolver();
                ContentValues contentValues = new ContentValues();
                String where = TodoListContract.TodoListColumns._ID + "=?";
                for (int i = from; i <= to; i++) {
                    contentValues.clear();
                    TodoBean bean = mTodoList.get(i);
                    bean.sortId = i;
                    contentValues.put(TodoListContract.TodoListColumns.SORT_ID, bean.sortId);
                    resolver.update(TodoListContract.TodoListColumns.CONTENT_URI_UPDATE, contentValues, where, new String[]{bean.id + ""});
                }
            }
        };
        DbThreadPool.getThreadPool().exeute(updateTask);
    }

    /**
     * 标记状态
     */
    private void updateStatus(final TodoBean updateBean) {
        updateBean.status = !updateBean.status;
        Runnable updateTask = new Runnable() {
            @Override
            public void run() {
                if (mTodoList == null || mTodoList.size() == 0)
                    return;
                ContentResolver resolver = App.get().getContentResolver();
                ContentValues contentValues = new ContentValues();
                String where = TodoListContract.TodoListColumns._ID + "=?";
                contentValues.put(TodoListContract.TodoListColumns.STATUS, updateBean.status ? 1 : 0);
                resolver.update(TodoListContract.TodoListColumns.CONTENT_URI_UPDATE, contentValues, where, new String[]{updateBean.id + ""});
            }
        };
        DbThreadPool.getThreadPool().exeute(updateTask);
    }

    /**
     * 根据ID查询
     *
     * @param searchId
     */
    private void queryById(final long searchId) {
        Runnable queryTask = new Runnable() {
            @Override
            public void run() {
                Cursor cursor = App.get().getContentResolver().query(TodoListContract.TodoListColumns.CONTENT_URI, null, TodoListContract.TodoListColumns._ID + "=?", new String[]{String.valueOf(searchId)}, null);
                TodoBean bean = null;
                while (cursor.moveToNext()) {
                    bean = new TodoBean();
                    bean.id = cursor.getLong(cursor.getColumnIndex(TodoListContract.TodoListColumns._ID));
                    bean.sortId = cursor.getInt(cursor.getColumnIndex(TodoListContract.TodoListColumns.SORT_ID));
                    bean.title = cursor.getString(cursor.getColumnIndex(TodoListContract.TodoListColumns.TITLE));
                    bean.description = cursor.getString(cursor.getColumnIndex(TodoListContract.TodoListColumns.DESCRIPTION));
                    bean.alarm = cursor.getLong(cursor.getColumnIndex(TodoListContract.TodoListColumns.ALARM));
                    bean.remind = cursor.getInt(cursor.getColumnIndex(TodoListContract.TodoListColumns.REMIND));
                    bean.status = cursor.getInt(cursor.getColumnIndex(TodoListContract.TodoListColumns.STATUS)) == 1;
                    bean.mark = cursor.getInt(cursor.getColumnIndex(TodoListContract.TodoListColumns.MARK));
                }
                Message msg = Message.obtain();
                msg.what = WHAT_QUERY_BYID;
                msg.obj = bean;
                mHandler.sendMessage(msg);
            }
        };
        DbThreadPool.getThreadPool().exeute(queryTask);
    }

    /**
     * 单条删除
     */
    private void deleteValue(final long id) {
        Runnable deleteTask = new Runnable() {
            @Override
            public void run() {
                App.get().getContentResolver().delete(TodoListContract.TodoListColumns.CONTENT_URI_DELETE, TodoListContract.TodoListColumns._ID + "=?", new String[]{String.valueOf(id)});
            }
        };
        DbThreadPool.getThreadPool().exeute(deleteTask);
    }

    /**
     * 新增Todo的事件，直接更新列表
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(InsertTodoEvent event) {
        if (event != null) {
            if (mTodoList.size() == 0) {
                queryValue();
            } else {
                mAdapter.addData(event.bean);
            }
        }
    }

    /**
     * 更新Todo的事件，直接填充列表
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EditTodoEvent event) {
        if (event != null) {
            if (mEditPos != -1) {
                mAdapter.setData(mEditPos, event.bean);
            }
        }
    }

    /**
     * 接收删除Todo的事件，先删，再改变排序
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(DeleteTodoEvent event) {
        if (event != null) {
            mAdapter.remove(event.pos);
            if (event.pos < mTodoList.size()) {
                updateSort(event.pos, mTodoList.size() - 1);
            }
            if (mTodoList.size() == 0) {
                queryValue();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ClearAllEvent event) {
        if (event != null) {
            queryValue();
            mEditPos = -1;
        }
    }

    @Override
    public void onDestroyView() {
        if (queryTask != null) {
            DbThreadPool.getThreadPool().cancel(queryTask);
        }
        super.onDestroyView();
    }
}
