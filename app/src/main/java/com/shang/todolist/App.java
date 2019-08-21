package com.shang.todolist;

import android.app.Application;

import com.shang.todolist.db.DbOpenHelper;

public class App extends Application {
    private static App app;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        new DbOpenHelper(app);
    }

    public static App get() {
        return app;
    }
}
