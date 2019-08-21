package com.shang.todolist;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.shang.todolist.db.TodoListBean;

import java.util.List;

/**
 * 待办列表的适配器
 */
public class TodoListAdapter extends BaseQuickAdapter<TodoListBean, BaseViewHolder> {
    private Drawable drawable;
    public boolean isDelete;
    public boolean isSort;

    public TodoListAdapter(@Nullable List<TodoListBean> data) {
        super(R.layout.item_todo, data);
        drawable = App.get().getResources().getDrawable(R.drawable.ic_mark);
    }

    @Override
    protected void convert(final BaseViewHolder helper, TodoListBean item) {
        TextView tv_title = helper.getView(R.id.tv_title);
        ImageView iv_alarm = helper.getView(R.id.iv_alarm);
        CheckBox cb_status = helper.getView(R.id.cb_status);
        tv_title.setText(item.title);
        if (item.mark == 1) {
            tv_title.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        } else {
            tv_title.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        }
        if (TextUtils.isEmpty(item.description)) {
            helper.setGone(R.id.tv_desc, false);
        } else {
            helper.setGone(R.id.tv_desc, true);
            helper.setText(R.id.tv_desc, item.description);
        }
        helper.setGone(R.id.iv_drag, false);
        helper.setGone(R.id.iv_delete, false);
        if (isDelete) {
            helper.setGone(R.id.iv_delete, true);
            isSort = false;
            iv_alarm.setVisibility(View.GONE);
            cb_status.setVisibility(View.GONE);
        } else {
            cb_status.setVisibility(View.VISIBLE);
        }
        if (isSort) {
            helper.setGone(R.id.iv_drag, true);
            isDelete = false;
            iv_alarm.setVisibility(View.GONE);
            cb_status.setVisibility(View.GONE);
        } else {
            cb_status.setVisibility(View.VISIBLE);
        }
        if (item.alarm == 0) {
            iv_alarm.setVisibility(View.GONE);
        } else {
            if (!isDelete && !isSort) {
                iv_alarm.setVisibility(View.VISIBLE);
            }
        }
        cb_status.setEnabled(true);
        if (item.status == 0) {
            cb_status.setChecked(false);
        } else if (item.status == 1) {
            cb_status.setChecked(true);
        } else {
            cb_status.setEnabled(false);
        }
        helper.addOnClickListener(R.id.iv_drag);
        helper.addOnClickListener(R.id.iv_delete);
        helper.addOnClickListener(R.id.cb_status);
    }

    public void setDelete() {
        isDelete = !isDelete;
        notifyDataSetChanged();
    }

    public void setSort() {
        isSort = !isSort;
        notifyDataSetChanged();
    }
}
