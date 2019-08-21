package com.shang.todolist.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 继承SQLiteOpenHelper实现的数据库帮助类
 * 用与创建库 & 表，以及升级数据库的管理
 */
public class DbOpenHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "todo_list.db";
    public static final int DB_VERSION = 1;
    public static final String TABLE_TODOLIST = "TODOLIST";

    private static DbOpenHelper helper;

    public static DbOpenHelper get() {
        return helper;
    }

    public DbOpenHelper(Context context) {
        // 传递数据库名与版本号给父类
        super(context, DB_NAME, null, DB_VERSION);
        helper = this;
    }

    /**
     * 第一次使用数据库时自动建表
     *
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        createAllTables(db);
    }

    /**
     * 之后升级数据库时调用
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 根据版本号进行升级数据库
        switch (oldVersion) {
            case 1:
                // 创建新版本app所缺失的表...
                break;
            case 2:
                // 创建新版本app所缺失的表...
                break;
            default:
        }
    }

    private void createAllTables(SQLiteDatabase db) {
        createCompany(db);
    }

    private void createCompany(SQLiteDatabase db) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE IF NOT EXISTS TODOLIST(");
        sql.append("_ID  INTEGER PRIMARY KEY AUTOINCREMENT,");
        sql.append("ORDER_ID     INTEGER    NOT NULL,");
        sql.append("TITLE        TEXT    NOT NULL,");
        sql.append("DESCRIPTION  TEXT,");
        sql.append("ALARM        INTEGER64,");
        sql.append("CREATE_TIME  INTEGER64,");
        sql.append("STATUS       INTEGER,");
        sql.append("MARK         INTEGER);");
        db.execSQL(sql.toString());
    }

    /**
     * 清空全部表数据
     */
    public void clearTable() {
        SQLiteDatabase database = getWritableDatabase();
        try {
            database.beginTransaction();
            database.execSQL("delete from " + DbOpenHelper.TABLE_TODOLIST + ";");
//            database.execSQL("delete from xxx;");
//            database.execSQL("delete from xxx;");
//            database.execSQL("delete from xxx;");
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
        }
    }
}