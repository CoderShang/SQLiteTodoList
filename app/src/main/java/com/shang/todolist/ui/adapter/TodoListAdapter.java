package com.shang.todolist.ui.adapter;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseItemDraggableAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.shang.todolist.App;
import com.shang.todolist.R;
import com.shang.todolist.db.TodoListBean;

import java.util.List;

/**
 * 待办列表的适配器
 */
public class TodoListAdapter extends BaseItemDraggableAdapter<TodoListBean, BaseViewHolder> {
    private Drawable drawable;

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

        helper.setGone(R.id.tv_desc, true);
        helper.setText(R.id.tv_desc, item.sortId+"");

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
//        helper.addOnClickListener(R.id.btn_delete);
    }
}
