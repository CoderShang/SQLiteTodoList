package com.shang.todolist;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseQuickAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private CoordinatorLayout root_layout;
    private Toolbar mToolbar;
    private TodoListAdapter mAdapter;
    private List<TodoListBean> mTodoList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        root_layout = findViewById(R.id.root_layout);
        mToolbar = findViewById(R.id.toolbar);
        //初始化ToolBar
        setSupportActionBar(mToolbar);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_sort:
                        Snackbar.make(root_layout, "排序", Snackbar.LENGTH_LONG).show();
                        break;
                    case R.id.action_query:
                        Snackbar.make(root_layout, "查询", Snackbar.LENGTH_LONG).show();
                        break;
                    case R.id.action_settings:
                        Snackbar.make(root_layout, "设置", Snackbar.LENGTH_LONG).show();
                        break;
                    case R.id.action_help:
                        Intent intent = new Intent();
                        intent.setAction("android.intent.action.VIEW");
                        Uri content_url = Uri.parse("https://github.com/CoderShang");
                        intent.setData(content_url);
                        startActivity(intent);
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        FloatingActionButton fab = findViewById(R.id.fab_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditActivity.startActivity(MainActivity.this, 0);
            }
        });
        //初始化RecyclerView
        RecyclerView rv_todo_list = findViewById(R.id.rv_todo_list);
        rv_todo_list.setLayoutManager(new LinearLayoutManager(this));
        mTodoList.add(new TodoListBean());
        mTodoList.add(new TodoListBean());
        mTodoList.add(new TodoListBean());
        mTodoList.add(new TodoListBean());
        mTodoList.add(new TodoListBean());
        mTodoList.add(new TodoListBean());
        mTodoList.add(new TodoListBean());
        mTodoList.add(new TodoListBean());
        mTodoList.add(new TodoListBean());
        mTodoList.add(new TodoListBean());
        mTodoList.add(new TodoListBean());
        mTodoList.add(new TodoListBean());
        mTodoList.add(new TodoListBean());
        mAdapter = new TodoListAdapter(mTodoList);
        View view = new View(this);
        ViewGroup.LayoutParams llp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UiUtils.dip2px(100));
        view.setLayoutParams(llp);
        mAdapter.setFooterView(view);
        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                EditActivity.startActivity(MainActivity.this, mTodoList.get(position).id);
            }
        });
        mAdapter.bindToRecyclerView(rv_todo_list);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
}
