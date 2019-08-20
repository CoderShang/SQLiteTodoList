package com.shang.todolist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Date;

/**
 * 新增 & 编辑 待办事项的页面
 */
public class EditActivity extends AppCompatActivity {
    public static final String KEY_ID = "ID";
    private Toolbar mToolbar;
    private EditText et_title, et_desc;
    private TextView tv_alarm;
    private CheckBox cb_mark;
    private LinearLayout root_layout;
    private int searchId; //主键ID，0说明是新添加，有值则编辑

    public static void startActivity(Context context, int searchId) {
        Intent intent = new Intent(context, EditActivity.class);
        intent.putExtra(KEY_ID, searchId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        mToolbar = findViewById(R.id.toolbar);
        root_layout = findViewById(R.id.root_layout);
        et_title = findViewById(R.id.et_title);
        et_desc = findViewById(R.id.et_desc);
        tv_alarm = findViewById(R.id.tv_alarm);
        cb_mark = findViewById(R.id.cb_mark);
        setSupportActionBar(mToolbar);
        Intent intent = getIntent();
        if (intent != null) {
            searchId = intent.getIntExtra(KEY_ID, 0);
        }
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //设置点击事件
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                submit();
                return false;
            }
            //查询主键ID填充数据
        });
    }

    /**
     * 获取值新增到数据库中
     */
    private void submit() {
        String title = et_title.getText().toString();
        if (TextUtils.isEmpty(title)) {
            Snackbar.make(root_layout, "标题不可为空！", Snackbar.LENGTH_LONG).show();
            return;
        }
        String desc = et_desc.getText().toString();
        String alarm = tv_alarm.getText().toString();
        long createTime = new Date().getTime();
        int isMark = cb_mark.isChecked() ? 1 : 0;
        //调用数据库 执行 新增 或 修改 操作
        if (searchId == 0) {

        } else {

        }
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);

        if (searchId != 0) {
            mToolbar.setTitle("编辑");
            menu.findItem(R.id.action_delete).setVisible(true);
        } else {
            mToolbar.setTitle("新增一个待办");
            menu.findItem(R.id.action_delete).setVisible(false);
        }
        return true;
    }
}
