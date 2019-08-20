package com.shang.todolist;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDataBaseHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "DBtest.db";
    public static final int DB_VERSION = 1;
    public static final String TABLE_COMPANY = "TODOLIST";

    private static MyDataBaseHelper helper;

    public static MyDataBaseHelper get() {
        return helper;
    }

    public MyDataBaseHelper(Context context) {
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
                createAllTables(db);
                break;
            default:
        }
    }

    private void createAllTables(SQLiteDatabase db) {
        createCompany(db);
    }

    private void createCompany(SQLiteDatabase db) {
        StringBuilder logRecordTable = new StringBuilder();
        logRecordTable.append("CREATE TABLE IF NOT EXISTS COMPANY(");
        logRecordTable.append("ID INT PRIMARY KEY     NOT NULL,");
        logRecordTable.append("NAME           TEXT    NOT NULL,");
        logRecordTable.append("AGE            INT     NOT NULL,");
        logRecordTable.append("ADDRESS        CHAR(50),");
        logRecordTable.append("SALARY         REAL);");
        db.execSQL(logRecordTable.toString());
    }
    /**
     * 清空全部表数据
     */
    public void clearTable() {
        SQLiteDatabase database = getWritableDatabase();
        try {
            database.beginTransaction();
            database.execSQL("delete from " + MyDataBaseHelper.TABLE_COMPANY + ";");
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
        }
    }
}