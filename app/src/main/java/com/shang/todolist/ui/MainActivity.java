package com.shang.todolist.ui;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.listener.OnItemDragListener;
import com.chad.library.adapter.base.listener.OnItemSwipeListener;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.XPopupCallback;
import com.shang.todolist.App;
import com.shang.todolist.R;
import com.shang.todolist.UiUtils;
import com.shang.todolist.db.DbOpenHelper;
import com.shang.todolist.db.DbThreadPool;
import com.shang.todolist.db.ManifestBean;
import com.shang.todolist.db.TodoListContract;
import com.shang.todolist.event.ClearAllEvent;
import com.shang.todolist.event.DeleteManifestEvent;
import com.shang.todolist.event.EditManifestEvent;
import com.shang.todolist.event.InsertManifestEvent;
import com.shang.todolist.ui.adapter.ItemDragAndSwipeCallback;
import com.shang.todolist.ui.adapter.MainAdapter;
import com.shang.todolist.ui.widget.AddManifestPopup;
import com.shang.todolist.ui.widget.TitleBar;
import com.shang.todolist.ui.widget.AddTodoPopup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final int FIRST = -1;
    public static final int WHAT_QUERY = 1;
    public static final int WHAT_QUERY_TODAY = 2;
    private RecyclerView mRecyclerView;
    private MainAdapter mAdapter;
    private TitleBar title_bar;
    private FloatingActionButton fab;
    private TextView btn_github;
    private ImageView btn_settings;
    private DrawerLayout drawer_layout;
    private FrameLayout root_layout, fl_header;
    private TextView btn_manifest_add;
    private Button btn_today_img;
    private TextView btn_today;
    private TextView tv_today_num;
    private List<ManifestBean> mManifestList = new ArrayList<>();
    private Runnable queryTask;
    private ItemTouchHelper mItemTouchHelper;
    private ItemDragAndSwipeCallback mItemDragAndSwipeCallback;
    private ContentObserver mObserver;
    private int mClickPos = -1;//被点击编辑的Item位置
    private int fromPos;//记录排序from 和 to 的位置
    private long deleteId;//记录准备删除的ID主键
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            int what = msg.what;
            switch (what) {
                case WHAT_QUERY:
                    //接收查询数据库返回的结果，更新UI
                    mAdapter.setNewData(mManifestList);
                    break;
                case WHAT_QUERY_TODAY:
                    int num = (int) msg.obj;
                    if (num == 0) {
                        tv_today_num.setText("");
                    } else {
                        tv_today_num.setText(String.valueOf(num));
                    }
                    break;
                default:
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);
        mRecyclerView = findViewById(R.id.rv_manifest);
        title_bar = findViewById(R.id.title_bar);
        drawer_layout = findViewById(R.id.drawer_layout);
        root_layout = findViewById(R.id.root_layout);
        btn_github = findViewById(R.id.btn_github);
        btn_settings = findViewById(R.id.btn_settings);
        fab = findViewById(R.id.fab_add);
        title_bar.setTitle(UiUtils.getTodayAndWeekStr());
        title_bar.setImgLeft(R.drawable.ic_todo);
        title_bar.setImgRight(R.drawable.ic_alarm);
        //初始化RecyclerView
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        //如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new MainAdapter(mManifestList);
        mAdapter.setHeaderView(getHeaderView());
        mAdapter.setFooterView(getFooterView());
        mAdapter.bindToRecyclerView(mRecyclerView);
        initListener();
        //初始化默认的Fragment
        switchFragment(FIRST);
        queryValue();
    }

    private void initListener() {
        btn_github.setOnClickListener(this);
        btn_settings.setOnClickListener(this);
        title_bar.setListener(new TitleBar.TitleBarListener() {
            @Override
            public void barLeft() {
                drawer_layout.openDrawer(Gravity.LEFT);
            }

            @Override
            public void barSecond() {
                new XPopup.Builder(MainActivity.this)
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
                                fab.show();
                            }

                            @Override
                            public boolean onBackPressed() {
                                return false;
                            }
                        })
                        .asCustom(new AddManifestPopup(MainActivity.this, mManifestList.get(mClickPos), mClickPos)).show();
            }

            @Override
            public void barRight() {
                Intent intent = new Intent(MainActivity.this, AlarmActivity.class);
                startActivity(intent);
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab.hide();
                int sort = Integer.valueOf(tv_today_num.getText().toString());
                long manifest = 0;
                if (mClickPos != -1) {
                    sort = mManifestList.get(mClickPos).num;
                    manifest = mManifestList.get(mClickPos).id;
                }
                AddTodoPopup pop = new AddTodoPopup(MainActivity.this, sort, manifest);
                new XPopup.Builder(MainActivity.this)
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
                                fab.show();
                            }

                            @Override
                            public boolean onBackPressed() {
                                return false;
                            }
                        })
                        .asCustom(pop).show();
            }
        });

        // 注册to-do数据改变的监听器
        mObserver = new ContentObserver(mHandler) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                if (uri == null) {
                    return;
                }
                if (uri.getPath().contains(TodoListContract.TodoListColumns.addPath)) {
                    queryValue();
                    Log.d("Insert:新增了一条数据！", uri.getPath());
                } else if (uri.getPath().contains(TodoListContract.TodoListColumns.deletePath)) {
                    queryValue();
                    Log.d("Delete:有数据被删除啦！", uri.getPath());
                }
            }
        };
        App.get().getApplicationContext().getContentResolver()
                .registerContentObserver(TodoListContract.TodoListColumns.CONTENT_URI,
                        true, mObserver);
        //排序的监听
        OnItemDragListener listener = new OnItemDragListener() {
            @Override
            public void onItemDragStart(RecyclerView.ViewHolder viewHolder, int pos) {
                BaseViewHolder holder = ((BaseViewHolder) viewHolder);
                holder.setGone(R.id.iv_sort, true);
                holder.setGone(R.id.tv_num, false);
                fromPos = pos;
            }

            @Override
            public void onItemDragMoving(RecyclerView.ViewHolder source, int from, RecyclerView.ViewHolder target, int to) {
            }

            @Override
            public void onItemDragEnd(RecyclerView.ViewHolder viewHolder, int pos) {
                BaseViewHolder holder = ((BaseViewHolder) viewHolder);
                holder.setGone(R.id.iv_sort, false);
                holder.setGone(R.id.tv_num, true);
                if (fromPos < pos) {
                    updateSort(fromPos, pos);
                } else if (pos < fromPos) {
                    updateSort(pos, fromPos);
                }
                mClickPos = pos;
            }
        };
        //侧滑删除的监听
        OnItemSwipeListener onItemSwipeListener = new OnItemSwipeListener() {
            @Override
            public void onItemSwipeStart(RecyclerView.ViewHolder viewHolder, int pos) {
                BaseViewHolder holder = ((BaseViewHolder) viewHolder);
                holder.setGone(R.id.iv_delete, true);
                holder.setGone(R.id.btn_manifest, false);
                deleteId = mManifestList.get(pos).id;
            }

            @Override
            public void clearView(RecyclerView.ViewHolder viewHolder, int pos) {
                BaseViewHolder holder = ((BaseViewHolder) viewHolder);
                holder.setGone(R.id.iv_delete, false);
                holder.setGone(R.id.btn_manifest, true);
                deleteId = 0;
            }

            @Override
            public void onItemSwiped(RecyclerView.ViewHolder viewHolder, int pos) {
                if (mClickPos == pos) {
                    mClickPos = -1;
                    title_bar.setImgSecond(0);
                    title_bar.setTitle(btn_today.getText().toString());
                    fl_header.setBackgroundResource(R.color.colorAccent);
                    switchFragment(FIRST);
                }
                deleteValue(deleteId);
                if (pos < mManifestList.size()) {
                    updateSort(pos, mManifestList.size() - 1);
                }
                if (mManifestList.size() == 0) {
                    queryValue();
                }
            }

            @Override
            public void onItemSwipeMoving(Canvas canvas, RecyclerView.ViewHolder viewHolder, float dX, float dY, boolean isCurrentlyActive) {

            }
        };
        mItemDragAndSwipeCallback = new ItemDragAndSwipeCallback(mAdapter);
        mItemTouchHelper = new ItemTouchHelper(mItemDragAndSwipeCallback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);
        mItemDragAndSwipeCallback.setDragMoveFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN);
        mItemDragAndSwipeCallback.setSwipeMoveFlags(ItemTouchHelper.END);
        mAdapter.enableSwipeItem();
        mAdapter.enableDragItem(mItemTouchHelper);
        mAdapter.setOnItemSwipeListener(onItemSwipeListener);
        mAdapter.setOnItemDragListener(listener);
        mAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                if (mClickPos == position) {
                    return;
                }
                title_bar.setImgSecond(R.drawable.ic_edit);
                title_bar.setTitle(mManifestList.get(position).name);
                mManifestList.get(position).selected = true;
                view.setBackgroundResource(R.color.colorAccent);
                if (mClickPos != -1) {
                    mManifestList.get(mClickPos).selected = false;
                    mAdapter.setData(mClickPos, mManifestList.get(mClickPos));
                } else {
                    fl_header.setBackgroundResource(android.R.color.transparent);
                }
                mClickPos = position;
                // 刷新Fragment
                switchFragment(mClickPos);
            }
        });
    }

    private View getHeaderView() {
        View item_left_header = LayoutInflater.from(this).inflate(R.layout.item_left_header, null, false);
        fl_header = item_left_header.findViewById(R.id.fl_header);
        btn_today_img = item_left_header.findViewById(R.id.btn_today_img);
        btn_today = item_left_header.findViewById(R.id.btn_today);
        tv_today_num = item_left_header.findViewById(R.id.tv_today_num);
        btn_today.setText(UiUtils.getTodayAndWeekStr());
        Calendar mCalendar = Calendar.getInstance();
        int mDay = mCalendar.get(Calendar.DAY_OF_MONTH);
        btn_today_img.setText(mDay + "");
        fl_header.setOnClickListener(this);
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
        Fragment fragment;
        if (position == FIRST) {
            fragment = TodoFragment.newInstance(0);
        } else {
            fragment = TodoFragment.newInstance(mManifestList.get(position).id);
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.main_container, fragment).commitAllowingStateLoss();
    }

    /**
     * 查询清单列表
     */
    private void queryValue() {
        if (queryTask == null) {
            queryTask = new Runnable() {
                @Override
                public void run() {
                    SQLiteDatabase db;
                    if (DbOpenHelper.get() != null) {
                        db = DbOpenHelper.get().getReadableDatabase();
                        Cursor cursor = db.rawQuery("SELECT " +
                                "_ID," +
                                "NAME," +
                                "SORT_ID," +
                                "(SELECT COUNT(1) FROM TODOLIST T  WHERE T.MANIFEST=M._ID) AS NUM " +
                                "FROM MANIFEST M ORDER BY SORT_ID", null);
                        mManifestList.clear();
                        while (cursor.moveToNext()) {
                            ManifestBean bean = new ManifestBean();
                            bean.id = cursor.getLong(cursor.getColumnIndex(TodoListContract.ManifestColumns._ID));
                            bean.sortId = cursor.getInt(cursor.getColumnIndex(TodoListContract.ManifestColumns.SORT_ID));
                            bean.name = cursor.getString(cursor.getColumnIndex(TodoListContract.ManifestColumns.NAME));
                            bean.num = cursor.getInt(cursor.getColumnIndex(TodoListContract.ManifestColumns.NUM));
                            mManifestList.add(bean);
                        }
                        if (mClickPos != -1 && mClickPos < mManifestList.size()) {
                            mManifestList.get(mClickPos).selected = true;
                        }
                        mHandler.sendEmptyMessage(WHAT_QUERY);
                        Cursor cursorToday = db.rawQuery("SELECT COUNT(1) FROM TODOLIST T  WHERE T.MANIFEST=0", null);
                        int num = 0;
                        while (cursorToday.moveToNext()) {
                            num = cursorToday.getInt(0);
                        }
                        Message msg = Message.obtain();
                        msg.obj = num;
                        msg.what = WHAT_QUERY_TODAY;
                        mHandler.sendMessage(msg);
                    }
                }
            };
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                DbThreadPool.getThreadPool().exeute(queryTask);
            }
        }, 500);
    }

    /**
     * 更改排序
     */
    private void updateSort(final int from, final int to) {
        Runnable updateTask = new Runnable() {
            @Override
            public void run() {
                if (mManifestList == null || mManifestList.size() == 0 || mManifestList.size() - 1 < to)
                    return;
                ContentResolver resolver = App.get().getContentResolver();
                ContentValues contentValues = new ContentValues();
                String where = TodoListContract.ManifestColumns._ID + "=?";
                for (int i = from; i <= to; i++) {
                    contentValues.clear();
                    ManifestBean bean = mManifestList.get(i);
                    bean.sortId = i;
                    contentValues.put(TodoListContract.ManifestColumns.SORT_ID, bean.sortId);
                    resolver.update(TodoListContract.ManifestColumns.CONTENT_URI_UPDATE, contentValues, where, new String[]{bean.id + ""});
                }
            }
        };
        DbThreadPool.getThreadPool().exeute(updateTask);
    }

    /**
     * 删除清单，并且删除下面关联的全部TO-DO
     */
    private void deleteValue(final long id) {
        Runnable deleteTask = new Runnable() {
            @Override
            public void run() {
                App.get().getContentResolver().delete(TodoListContract.ManifestColumns.CONTENT_URI_DELETE, TodoListContract.ManifestColumns._ID + "=?", new String[]{String.valueOf(id)});
                App.get().getContentResolver().delete(TodoListContract.TodoListColumns.CONTENT_URI_DELETE, TodoListContract.TodoListColumns.MANIFEST + "=?", new String[]{String.valueOf(id)});
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
    public void onEvent(InsertManifestEvent event) {
        if (event != null) {
            mAdapter.addData(event.bean);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ClearAllEvent event) {
        if (event != null) {
            title_bar.setImgSecond(0);
            title_bar.setTitle(btn_today.getText().toString());
            fl_header.setBackgroundResource(R.color.colorAccent);
            tv_today_num.setText("");
            switchFragment(FIRST);
            mManifestList.clear();
            mAdapter.notifyDataSetChanged();
            mClickPos = -1;
        }
    }

    /**
     * 更新Todo的事件，直接填充列表
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EditManifestEvent event) {
        if (event != null) {
            if (mClickPos != -1) {
                title_bar.setTitle(event.bean.name);
                mAdapter.setData(mClickPos, event.bean);
            }
        }
    }

    /**
     * 接收删除Todo的事件，先删，再改变排序
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(DeleteManifestEvent event) {
        if (event != null) {
            if (mClickPos == event.pos) {
                mClickPos = -1;
                title_bar.setImgSecond(0);
                title_bar.setTitle(btn_today.getText().toString());
                fl_header.setBackgroundResource(R.color.colorAccent);
                switchFragment(FIRST);
            }
            mAdapter.remove(event.pos);
            if (event.pos < mManifestList.size()) {
                updateSort(event.pos, mManifestList.size() - 1);
            }
            if (mManifestList.size() == 0) {
                queryValue();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_manifest_add:
                new XPopup.Builder(MainActivity.this)
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
                                fab.show();
                            }

                            @Override
                            public boolean onBackPressed() {
                                return false;
                            }
                        })
                        .asCustom(new AddManifestPopup(MainActivity.this, mManifestList.size())).show();
                break;
            case R.id.fl_header:
                if (mClickPos == -1) {
                    return;
                }
                title_bar.setImgSecond(0);
                title_bar.setTitle(btn_today.getText().toString());
                fl_header.setBackgroundResource(R.color.colorAccent);
                switchFragment(FIRST);
                if (mClickPos != -1) {
                    mManifestList.get(mClickPos).selected = false;
                    mAdapter.setData(mClickPos, mManifestList.get(mClickPos));
                }
                mClickPos = -1;
                break;
            case R.id.btn_settings:
                SettingsActivity.startActivity(this);
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
        App.get().getApplicationContext().getContentResolver().unregisterContentObserver(mObserver);
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
