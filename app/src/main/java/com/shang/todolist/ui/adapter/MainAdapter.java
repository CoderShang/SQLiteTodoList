package com.shang.todolist.ui.adapter;


import android.support.annotation.Nullable;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseItemDraggableAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.shang.todolist.R;
import com.shang.todolist.db.ManifestBean;

import java.util.List;

/**
 * 主页清单列表的数据适配器
 */
public class MainAdapter extends BaseItemDraggableAdapter<ManifestBean, BaseViewHolder> {

    public MainAdapter(@Nullable List<ManifestBean> data) {
        super(R.layout.item_left_fun, data);
    }

    @Override
    protected void convert(final BaseViewHolder helper, ManifestBean item) {
        TextView btn_manifest = helper.getView(R.id.btn_manifest);
        TextView tv_num = helper.getView(R.id.tv_num);
        btn_manifest.setText(item.name);
        if (item.num > 0) {
            tv_num.setText(String.valueOf(item.num));
        } else {
            tv_num.setText("");
        }


    }
}
