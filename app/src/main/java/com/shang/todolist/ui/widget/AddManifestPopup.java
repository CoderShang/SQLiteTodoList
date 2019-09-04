package com.shang.todolist.ui.widget;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.lxj.xpopup.core.BottomPopupView;
import com.shang.todolist.App;
import com.shang.todolist.R;
import com.shang.todolist.db.DbThreadPool;
import com.shang.todolist.db.ManifestBean;
import com.shang.todolist.db.TodoListContract;
import com.shang.todolist.event.EditTodoEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;

/**
 * Description: 添加清单的底部弹窗
 */
public class AddManifestPopup extends BottomPopupView implements View.OnClickListener {
    public EditText et_comment;
    private ImageView btn_add, btn_delete;
    private LinearLayout ll_add;
    private ManifestBean mBean;
    private int maxNum;

    public AddManifestPopup(@NonNull Context context, ManifestBean bean) {
        super(context);
        this.mBean = bean;
    }

    public AddManifestPopup(@NonNull Context context, int maxNum) {
        super(context);
        this.maxNum = maxNum;
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.add_manifest_popup;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        initView();
    }

    private void initView() {
        ll_add = findViewById(R.id.ll_add);
        et_comment = findViewById(R.id.et_comment);
        btn_add = findViewById(R.id.btn_add);
        btn_delete = findViewById(R.id.btn_delete);
        ll_add.setOnClickListener(this);
        btn_add.setOnClickListener(this);
        btn_delete.setOnClickListener(this);
        btn_add.setEnabled(false);
        if (mBean != null) {
            btn_add.setImageResource(R.drawable.ic_done);
            btn_delete.setVisibility(VISIBLE);
            et_comment.setText(mBean.name);
        } else {
            btn_add.setImageResource(R.drawable.ic_add_circle);
            btn_delete.setVisibility(GONE);
        }
        et_comment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    btn_add.setEnabled(false);
                    btn_add.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.gray_999)));
                } else {
                    btn_add.setEnabled(true);
                    btn_add.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.colorPrimary)));
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add:
                if (mBean == null) {
                    insertValue(new ManifestBean(Calendar.getInstance().getTimeInMillis(), maxNum + 1, et_comment.getText().toString()));
                } else {
                    mBean.name = et_comment.getText().toString();
                    updateValue(mBean);
                }
                break;
            case R.id.btn_delete:
                deleteValue(mBean.id);
                break;
            default:
                break;
        }
        dismiss();
    }

    private void insertValue(final ManifestBean addBean) {
        Runnable updateTask = new Runnable() {
            @Override
            public void run() {
                ContentValues contentValues = new ContentValues();
                contentValues.put(TodoListContract.ManifestColumns._ID, addBean.id);
                contentValues.put(TodoListContract.ManifestColumns.SORT_ID, addBean.sortId);
                contentValues.put(TodoListContract.ManifestColumns.NAME, addBean.name);
                App.get().getContentResolver().insert(TodoListContract.ManifestColumns.CONTENT_URI_ADD, contentValues);
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

    private void updateValue(final ManifestBean updateBean) {
        Runnable updateTask = new Runnable() {
            @Override
            public void run() {
                ContentValues contentValues = new ContentValues();
                contentValues.put(TodoListContract.ManifestColumns.NAME, updateBean.name);
                App.get().getContentResolver().update(TodoListContract.ManifestColumns.CONTENT_URI_UPDATE, contentValues, TodoListContract.ManifestColumns._ID + "=?", new String[]{String.valueOf(updateBean.id)});
            }
        };
        DbThreadPool.getThreadPool().exeute(updateTask);
        EventBus.getDefault().post(new EditTodoEvent(updateBean.id));
    }
}