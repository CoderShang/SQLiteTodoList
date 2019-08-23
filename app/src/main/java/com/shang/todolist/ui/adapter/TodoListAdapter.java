package com.shang.todolist.ui.adapter;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.billy.android.swipe.SmartSwipe;
import com.billy.android.swipe.SmartSwipeWrapper;
import com.billy.android.swipe.SwipeConsumer;
import com.billy.android.swipe.consumer.SlidingConsumer;
import com.billy.android.swipe.consumer.TranslucentSlidingConsumer;
import com.billy.android.swipe.listener.SimpleSwipeListener;
import com.chad.library.adapter.base.BaseItemDraggableAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.shang.todolist.R;
import com.shang.todolist.db.TodoListBean;

import java.util.List;

/**
 * 待办列表的适配器
 */
public class TodoListAdapter extends BaseItemDraggableAdapter<TodoListBean, BaseViewHolder> {

    public TodoListAdapter(@Nullable List<TodoListBean> data) {
        super(R.layout.item_todo, data);
    }

    @Override
    protected void convert(final BaseViewHolder helper, TodoListBean item) {
        CardView card_view = helper.getView(R.id.card_view);
        ImageView iv_alarm = helper.getView(R.id.iv_alarm);
        CheckBox cb_status = helper.getView(R.id.cb_status);
        helper.setText(R.id.tv_title, item.title);
        if (item.mark == 1) {
            helper.setGone(R.id.iv_mark, true);
        } else {
            helper.setGone(R.id.iv_mark, false);
        }
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
//        helper.addOnClickListener(R.id.btn_delete);
        //侧滑删除
//        SmartSwipe.wrap(card_view)
//                .addConsumer(new TranslucentSlidingConsumer())
//                .enableHorizontal() //启用左右两侧侧滑
//                .addListener(new SimpleSwipeListener() {
//                    @Override
//                    public void onSwipeOpened(SmartSwipeWrapper wrapper, SwipeConsumer consumer, int direction) {
//                        //侧滑打开时，移除
//                        ViewParent parent = wrapper.getParent();
//                        if (parent instanceof ViewGroup) {
//                            ((ViewGroup) parent).removeView(wrapper);
//                        }
//                        //adapter.removeItem(getAdapterPosition());// 也可用作从recyclerView中移除该项
//                    }
//                })
//        ;
    }
}
