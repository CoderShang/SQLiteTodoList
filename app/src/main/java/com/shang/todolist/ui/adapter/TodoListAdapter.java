package com.shang.todolist.ui.adapter;

import android.content.res.ColorStateList;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseItemDraggableAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.shang.todolist.R;
import com.shang.todolist.db.TodoBean;

import java.util.List;

/**
 * 待办列表的适配器
 */
public class TodoListAdapter extends BaseItemDraggableAdapter<TodoBean, BaseViewHolder> {

    public TodoListAdapter(@Nullable List<TodoBean> data) {
        super(R.layout.item_todo, data);
    }

    @Override
    protected void convert(final BaseViewHolder helper, TodoBean item) {
        CardView card_view = helper.getView(R.id.card_view);
        ImageView iv_alarm = helper.getView(R.id.iv_alarm);
        CheckBox cb_status = helper.getView(R.id.cb_status);
        helper.setText(R.id.tv_title, item.title);
        int markId;
        switch (item.mark) {
            case 0:
                markId = R.drawable.ic_urgent_1;
                break;
            case 1:
                markId = R.drawable.ic_urgent_2;
                break;
            case 2:
                markId = R.drawable.ic_urgent_3;
                break;
            default:
                markId = R.drawable.ic_urgent;
                break;
        }
        helper.setImageResource(R.id.iv_mark, markId);
        if (TextUtils.isEmpty(item.description)) {
            helper.setGone(R.id.tv_desc, false);
        } else {
            helper.setGone(R.id.tv_desc, true);
            helper.setText(R.id.tv_desc, item.description);
        }
        if (item.alarm == 0) {
            iv_alarm.setVisibility(View.GONE);
        } else {
            iv_alarm.setVisibility(View.VISIBLE);
        }
        cb_status.setEnabled(true);
        if (item.status) {
            cb_status.setChecked(true);
        } else {
            cb_status.setChecked(false);
        }
        helper.addOnClickListener(R.id.card_view);
        helper.addOnClickListener(R.id.cb_status);
    }
}
