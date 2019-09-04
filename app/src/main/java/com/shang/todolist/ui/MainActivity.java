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
import com.shang.todolist.ui.adapter.ItemDragAndSwipeCallback;
import com.shang.todolist.ui.adapter.MainAdapter;
import com.shang.todolist.ui.widget.AddManifestPopup;
import com.shang.todolist.ui.widget.TitleBar;
import com.shang.todolist.ui.widget.AddTodoPopup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final int FIRST = 0;
    public static final int SECOND = 1;
    public static final int THIRD = 2;
    public static final int WHAT_QUERY = 1;
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
    private int lastPos = -1;
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
                default:
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        switchFragment(0);
        queryValue();
    }

    private void initListener() {
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
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab.hide();
                int sort = 0;
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
        btn_github.setOnClickListener(this);
        // 注册数据改变的监听器
        mObserver = new ContentObserver(mHandler) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                if (uri == null) {
                    return;
                }
                if (uri.getPath().contains(TodoListContract.ManifestColumns.addPath)) {
                    Log.d("Insert:新增了一条数据！", uri.getPath());
                } else if (uri.getPath().contains(TodoListContract.ManifestColumns.deletePath)) {
                    Log.d("Delete:有删除被数据啦！", uri.getPath());
                } else if (uri.getPath().contains(TodoListContract.ManifestColumns.updatePath)) {
                    Log.d("Update：有数据被修改啦！", uri.getPath());
                }
            }
        };
        App.get().getApplicationContext().getContentResolver()
                .registerContentObserver(TodoListContract.ManifestColumns.CONTENT_URI,
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
//                canvas.drawColor(ContextCompat.getColor(mContext, R.color.colorAccent));
//                canvas.drawText("Just some text", 0, 40, paint);
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
                title_bar.setTitle(mManifestList.get(position).name);
                mManifestList.get(position).selected = true;
                view.setBackgroundResource(R.color.colorAccent);
                if (mClickPos != -1) {
                    mManifestList.get(mClickPos).selected = false;
                    mAdapter.notifyItemChanged(mClickPos);
                } else {
                    fl_header.setBackgroundResource(android.R.color.transparent);
                }
                mClickPos = position;
                // 刷新Fragment
                switchFragment(1);
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
                fragment = TodoFragment.newInstance(mManifestList.get(mClickPos).id);
                break;
            default:
                break;
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
                        mHandler.sendEmptyMessage(WHAT_QUERY);
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
     * 单条删除
     */
    private void deleteValue(final long id) {
        Runnable deleteTask = new Runnable() {
            @Override
            public void run() {
                App.get().getContentResolver().delete(TodoListContract.ManifestColumns.CONTENT_URI_DELETE, TodoListContract.ManifestColumns._ID + "=?", new String[]{String.valueOf(id)});
            }
        };
        DbThreadPool.getThreadPool().exeute(deleteTask);
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
                title_bar.setTitle(btn_today.getText().toString());
                fl_header.setBackgroundResource(R.color.colorAccent);
                switchFragment(0);
                if (mClickPos != -1) {
                    mManifestList.get(mClickPos).selected = false;
                    mAdapter.notifyItemChanged(mClickPos);
                }
                mClickPos = -1;
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
}
