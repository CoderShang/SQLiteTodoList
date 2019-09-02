package com.shang.todolist.ui.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BasePopupView;
import com.lxj.xpopup.enums.PopupPosition;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.shang.todolist.R;
import com.shang.todolist.UiUtils;
import com.shang.todolist.db.ManifestBean;
import com.shang.todolist.db.TodoBean;

import java.util.Calendar;
import java.util.Date;

/**
 * 添加清单的底部View
 */
public class AddManifestView extends LinearLayout implements View.OnClickListener {
    public EditText et_comment;
    private ImageView btn_add;
    private LinearLayout ll_add;
    private ManifestBean mBean;
    private OnAddListener listener;

    public AddManifestView(Context context) {
        super(context);
        init(context);
    }

    public AddManifestView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AddManifestView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void clearUI() {
        et_comment.setText("");
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.add_bottom_popup, this, true);
        ll_add = findViewById(R.id.ll_add);
        et_comment = findViewById(R.id.et_comment);
        btn_add = findViewById(R.id.btn_add);
        ll_add.setOnClickListener(this);
        btn_add.setOnClickListener(this);
        btn_add.setEnabled(false);
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

    public interface OnAddListener {
        void onCreated(ManifestBean bean);
    }

    public void setListener(OnAddListener listener) {
        this.listener = listener;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add:
                if (listener != null) {
                    mBean = new ManifestBean(Calendar.getInstance().getTimeInMillis(), 0,
                            et_comment.getText().toString(), 0);
                    listener.onCreated(mBean);
                }
                break;
            default:
                break;
        }
    }
}
