package com.shang.todolist.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.shang.todolist.R;
import com.shang.todolist.ui.widget.TitleBar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final int FIRST = 0;
    public static final int SECOND = 1;
    public static final int THIRD = 2;
    private TitleBar title_bar;
    private FloatingActionButton fab;
    private TextView btn_github;
    private DrawerLayout drawer_layout;
    private FrameLayout root_layout;
    private FragmentManager fragmentManager;
    private FragmentTransaction transaction;
    private Fragment fragment1;
    private Fragment fragment2;
    private Fragment fragment3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        title_bar = findViewById(R.id.title_bar);
        drawer_layout = findViewById(R.id.drawer_layout);
        btn_github = findViewById(R.id.btn_github);
        root_layout = findViewById(R.id.root_layout);
        fab = findViewById(R.id.fab_add);
        btn_github.setOnClickListener(this);
        title_bar.setImgLeft(R.drawable.ic_todo);
        title_bar.setImgRight(R.drawable.app_logo);
        title_bar.setListener(new TitleBar.TitleBarListener() {
            @Override
            public void barLeft() {
                drawer_layout.openDrawer(Gravity.LEFT);
            }

            @Override
            public void barSecond() {

            }

            @Override
            public void barRight() {

            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if (mTodoList != null) {
//                    mListSize = mTodoList.size();
//                } else {
//                    mListSize = 0;
//                }
                EditActivity.startActivity(MainActivity.this, 0);
            }
        });

        initLayout();
    }

    private void initLayout() {
        /**  初始化底部按钮 */
        fragmentManager = getSupportFragmentManager();
        transaction = fragmentManager.beginTransaction();
//        transaction.setCustomAnimations(
//                R.anim.fade_in,
//                R.anim.fade_out);
        if (fragment1 == null) {
            fragment1 = new TodoFragment();
            transaction.add(R.id.main_container, fragment1);
        } else {
            transaction.show(fragment1);
        }
        transaction.commitAllowingStateLoss();
    }

    /**
     * 隐藏所有Fragment
     */
    public void hideAllFragment(FragmentTransaction transaction) {
//        transaction.setCustomAnimations(
//                R.anim.fade_in,
//                R.anim.fade_out);
        if (fragment1 != null) {
            transaction.hide(fragment1);
        }
        if (fragment2 != null) {
            transaction.hide(fragment2);
        }
        if (fragment3 != null) {
            transaction.hide(fragment3);
        }
    }

    private void switchFragment(int position) {
        transaction = fragmentManager.beginTransaction();
        hideAllFragment(transaction);
        switch (position) {
            case FIRST:
                if (fragment1 == null) {
                    fragment1 = new TodoFragment();
                    transaction.add(R.id.main_container, fragment1);
                } else {
                    transaction.show(fragment1);
                }
                break;
            case SECOND:
                if (fragment2 == null) {
                    fragment2 = new TodoFragment();
                    transaction.add(R.id.main_container, fragment2);
                } else {
                    transaction.show(fragment2);
                }
                break;
            case THIRD:
                if (fragment3 == null) {
                    fragment3 = new TodoFragment();
                    transaction.add(R.id.main_container, fragment3);
                } else {
                    transaction.show(fragment3);
                }
                break;
            default:
                break;
        }
        transaction.commitAllowingStateLoss();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_github:
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse("https://github.com/CoderShang");
                intent.setData(content_url);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
    /**
     * 临时注释掉底部bar以及布局，以后扩展时再放开
     */
//    private BottomNavigationBar bottomNavigationBar;
//    implements BottomNavigationBar.OnTabSelectedListener
//    private int lastSelectedPosition = FIRST;
//        bottomNavigationBar = findViewById(R.id.bottom_navigation_bar);
//        bottomNavigationBar.setTabSelectedListener(this);
//        bottomNavigationBar.setMode(BottomNavigationBar.MODE_FIXED);
//        bottomNavigationBar.setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_STATIC);
//        bottomNavigationBar
//                .addItem(new BottomNavigationItem(R.drawable.ic_todo, "To-do"))
//                .addItem(new BottomNavigationItem(R.drawable.ic_pie_chart, "Chart"))
//                .addItem(new BottomNavigationItem(R.drawable.ic_settings, "Settings"))
//                .setFirstSelectedPosition(lastSelectedPosition)
//                .initialise();
//    @Override
//    public void onTabSelected(int position) {
//        lastSelectedPosition = position;
//        switchFragment(position);
//    }
//
//    @Override
//    public void onTabUnselected(int position) {
//
//    }
//
//    @Override
//    public void onTabReselected(int position) {
//
//    }

}
