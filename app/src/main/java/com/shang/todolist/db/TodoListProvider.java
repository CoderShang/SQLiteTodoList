package com.shang.todolist.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.shang.todolist.App;

/**
 * 待办列表的数据提供者
 */
public class TodoListProvider extends ContentProvider {
    private static final String TAG = TodoListProvider.class.getSimpleName();
    private final static int TODOLIST = 0;

    private static final UriMatcher sUriMatcher;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(TodoListContract.AUTHORITY, "todolist", TODOLIST);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db;
        if (DbOpenHelper.get() != null) {
            db = DbOpenHelper.get().getReadableDatabase();
        } else {
            Log.d(TAG, "mOpenHelper is null!!!");
            return null;
        }
        Cursor cursor = db.query(DbOpenHelper.TABLE_TODOLIST, projection, selection, selectionArgs, null, null, sortOrder, null);
        if (cursor == null) {
            Log.d(TAG, "TodoList.query: failed");
        } else {
            cursor.setNotificationUri(App.get().getContentResolver(), uri);
        }

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case TODOLIST:
                return "vnd.android.cursor.dir/todolist";
            default:
                throw new IllegalArgumentException("Unknown URL");
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        long rowId;
        SQLiteDatabase db = DbOpenHelper.get().getWritableDatabase();
        Uri uriResult;
        switch (sUriMatcher.match(uri)) {
            case TODOLIST:
                rowId = db.insert(DbOpenHelper.TABLE_TODOLIST, TodoListContract.TodoListColumns._ID, values);
                uriResult = ContentUris.withAppendedId(TodoListContract.TodoListColumns.CONTENT_URI, rowId);
                break;
            default:
                throw new IllegalArgumentException("Cannot insert from URL: " + uri);
        }
        getContext().getContentResolver().notifyChange(uriResult, null);
        return uriResult;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int count;
        SQLiteDatabase db = DbOpenHelper.get().getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case TODOLIST:
                count = db.delete(DbOpenHelper.TABLE_TODOLIST, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Cannot delete from URL: " + uri);
        }
//        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int count;
        SQLiteDatabase db = DbOpenHelper.get().getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case TODOLIST:
                count = db.update(DbOpenHelper.TABLE_TODOLIST, values, selection, selectionArgs);
                break;
            default: {
                throw new UnsupportedOperationException(
                        "Cannot update URL: " + uri);
            }
        }
//        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
