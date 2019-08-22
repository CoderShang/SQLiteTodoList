package com.shang.todolist;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;

public class MainActivity extends AppCompatActivity implements BottomNavigationBar.OnTabSelectedListener {
    private BottomNavigationBar bottomNavigationBar;
    private Fragment fragment1;
    private Fragment fragment2;
    private Fragment fragment3;
    private int lastSelectedPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigationBar = findViewById(R.id.bottom_navigation_bar);
        bottomNavigationBar.setTabSelectedListener(this);
        bottomNavigationBar.setMode(BottomNavigationBar.MODE_SHIFTING);
        bottomNavigationBar.setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_STATIC);
        bottomNavigationBar
                .addItem(new BottomNavigationItem(R.drawable.ic_todo, "待办"))
                .addItem(new BottomNavigationItem(R.drawable.ic_pie_chart, "统计"))
                .addItem(new BottomNavigationItem(R.drawable.ic_settings, "设置"))
                .setFirstSelectedPosition(lastSelectedPosition)
                .initialise();
        fragment1 = new TodoFragment();
        fragment2 = new ChartFragment();
        fragment3 = new SettingsFragment();
        switchFragment(lastSelectedPosition);
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

    private void switchFragment(int position) {
        switch (position) {
            case 0:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_container, fragment1).commitAllowingStateLoss();
                break;
            case 1:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_container, fragment2).commitAllowingStateLoss();
                break;
            case 2:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_container, fragment3).commitAllowingStateLoss();
                break;
            default:
                break;
        }
    }
}
