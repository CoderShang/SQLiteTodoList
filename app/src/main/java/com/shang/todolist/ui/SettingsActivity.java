package com.shang.todolist.ui;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.shang.todolist.R;
import com.shang.todolist.db.DbOpenHelper;
import com.shang.todolist.event.ClearAllEvent;
import com.shang.todolist.ui.widget.TitleBar;

import org.greenrobot.eventbus.EventBus;

/**
 * 新增 & 编辑 待办事项的页面
 */
public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {
    private TitleBar title_bar;
    private EditText et_title, et_desc;
    private TextView tv_alarm;
    private CheckBox cb_mark;
    private RelativeLayout rl_clear_all, rl_about;
    private LinearLayout root_layout;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
        context.startActivity(intent);
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (SettingsActivity.this == null) {
                return false;
            }
            int what = msg.what;
            switch (what) {
                case 1:
                    break;
                default:
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Intent intent = getIntent();
        title_bar = findViewById(R.id.title_bar);
        rl_clear_all = findViewById(R.id.rl_clear_all);
        rl_about = findViewById(R.id.rl_about);
        root_layout = findViewById(R.id.root_layout);
        title_bar.setImgLeft(R.drawable.ic_arrow_back);
        title_bar.setImgRight(0);
        title_bar.setLeftTitle(R.string.set_up);
        title_bar.setListener(new TitleBar.TitleBarListener() {
            @Override
            public void barLeft() {
                finish();
            }

            @Override
            public void barSecond() {
            }

            @Override
            public void barRight() {
            }
        });
        rl_clear_all.setOnClickListener(this);
        rl_about.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_clear_all:
                DbOpenHelper.get().clearTable();
                EventBus.getDefault().post(new ClearAllEvent());
                break;
            case R.id.rl_about:
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
