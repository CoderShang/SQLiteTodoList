package com.shang.todolist.ui.adapter;

import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

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
        TextView tv_title = helper.getView(R.id.tv_title);
        TextView tv_desc = helper.getView(R.id.tv_desc);
        tv_title.setText(item.title);
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
        int card_color;
        int status_color;
        if (item.status) {
            status_color = ContextCompat.getColor(mContext, R.color.gray_disable);
            tv_title.setTextColor(status_color);
            tv_desc.setTextColor(status_color);
            iv_alarm.setImageTintList(ColorStateList.valueOf(status_color));
            cb_status.setChecked(true);
            cb_status.setButtonTintList(ColorStateList.valueOf(status_color));
            tv_title.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            tv_desc.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            card_color = R.color.finish;
            tv_title.setTypeface(null, Typeface.BOLD_ITALIC);
            tv_desc.setTypeface(null, Typeface.ITALIC);
        } else {
            status_color = ContextCompat.getColor(mContext, R.color.white_color);
            tv_title.setTextColor(status_color);
            tv_desc.setTextColor(ContextCompat.getColor(mContext, R.color.desc_color));
            iv_alarm.setImageTintList(ColorStateList.valueOf(status_color));
            cb_status.setChecked(false);
            cb_status.setButtonTintList(ColorStateList.valueOf(status_color));
            tv_title.getPaint().setFlags(0);
            tv_desc.getPaint().setFlags(0);
            tv_title.setTypeface(null, Typeface.BOLD);
            tv_desc.setTypeface(null, Typeface.NORMAL);
//            txt2.getPaint().setFlags(Paint.FAKE_BOLD_TEXT_FLAG);
//            //添加下划线
//            txt3.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
            switch (item.mark) {
                case 0:
                    card_color = R.color.urgent_red;
                    break;
                case 1:
                    card_color = R.color.urgent_orange;
                    break;
                case 2:
                    card_color = R.color.urgent_blue;
                    break;
                default:
                    card_color = R.color.urgent_normal;
                    break;
            }
        }
        tv_title.getPaint().setAntiAlias(true);
        tv_desc.getPaint().setAntiAlias(true);
        card_view.setCardBackgroundColor(ContextCompat.getColor(mContext, card_color));
        helper.addOnClickListener(R.id.card_view);
        helper.addOnClickListener(R.id.cb_status);
    }
}
