package com.shang.todolist;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;

public class MainActivity extends AppCompatActivity implements BottomNavigationBar.OnTabSelectedListener {
    public static final int FIRST = 0;
    public static final int SECOND = 1;
    public static final int THIRD = 2;
    private FragmentManager fragmentManager;
    private FragmentTransaction transaction;
    private BottomNavigationBar bottomNavigationBar;
    private Fragment fragment1;
    private Fragment fragment2;
    private Fragment fragment3;
    private int lastSelectedPosition = FIRST;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigationBar = findViewById(R.id.bottom_navigation_bar);
        bottomNavigationBar.setTabSelectedListener(this);
        bottomNavigationBar.setMode(BottomNavigationBar.MODE_FIXED);
        bottomNavigationBar.setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_STATIC);
        bottomNavigationBar
                .addItem(new BottomNavigationItem(R.drawable.ic_todo, "To-do"))
                .addItem(new BottomNavigationItem(R.drawable.ic_pie_chart, "Chart"))
                .addItem(new BottomNavigationItem(R.drawable.ic_settings, "Settings"))
                .setFirstSelectedPosition(lastSelectedPosition)
                .initialise();
        initLayout();
    }

    @Override
    public void onTabSelected(int position) {
        lastSelectedPosition = position;
        switchFragment(position);
    }

    @Override
    public void onTabUnselected(int position) {

    }

    @Override
    public void onTabReselected(int position) {

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
                    fragment2 = new ChartFragment();
                    transaction.add(R.id.main_container, fragment2);
                } else {
                    transaction.show(fragment2);
                }
                break;
            case THIRD:
                if (fragment3 == null) {
                    fragment3 = new SettingsFragment();
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
}
