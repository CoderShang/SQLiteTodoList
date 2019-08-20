package com.shang.todolist;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

/**
 * 待办列表的适配器
 */
public class TodoListAdapter extends BaseQuickAdapter<TodoListBean, BaseViewHolder> {
    private Drawable drawable;

    public TodoListAdapter(@Nullable List<TodoListBean> data) {
        super(R.layout.item_todo, data);
        drawable = App.get().getResources().getDrawable(R.drawable.ic_mark);
    }

    @Override
    protected void convert(final BaseViewHolder helper, TodoListBean item) {
        TextView tv_title = helper.getView(R.id.tv_title);
        ImageButton ib_alarm = helper.getView(R.id.ib_alarm);
        CheckBox cb_status = helper.getView(R.id.cb_status);
        tv_title.setText(item.title);
        if (item.mark == 1) {
            tv_title.setCompoundDrawables(drawable, null, null, null);
        } else {
            tv_title.setCompoundDrawables(null, null, null, null);
        }
        if (TextUtils.isEmpty(item.alarm)) {
            ib_alarm.setVisibility(View.GONE);
        } else {
            ib_alarm.setVisibility(View.VISIBLE);
        }
        cb_status.setEnabled(true);
        if (item.status == 0) {
            cb_status.setChecked(false);
        } else if (item.status == 1) {
            cb_status.setChecked(true);
        } else {
            cb_status.setEnabled(false);
        }
        helper.setText(R.id.tv_desc, item.description);

//        item.getAlarm()
//        item.getStatus()
    }
}
