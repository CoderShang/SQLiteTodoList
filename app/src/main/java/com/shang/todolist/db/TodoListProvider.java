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
    private final static int TODOLIST = 101;
    private final static int TODOLIST_ADD = 102;
    private final static int TODOLIST_DELETE = 103;
    private final static int TODOLIST_UPDATE = 104;
    private final static int MANIFEST = 201;
    private final static int MANIFEST_ADD = 202;
    private final static int MANIFEST_DELETE = 203;
    private final static int MANIFEST_UPDATE = 204;
    private final static int ALARM = 301;

    private static final UriMatcher sUriMatcher;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(TodoListContract.AUTHORITY, "todolist", TODOLIST);
        sUriMatcher.addURI(TodoListContract.AUTHORITY, "todolist/add", TODOLIST_ADD);
        sUriMatcher.addURI(TodoListContract.AUTHORITY, "todolist/delete", TODOLIST_DELETE);
        sUriMatcher.addURI(TodoListContract.AUTHORITY, "todolist/update", TODOLIST_UPDATE);
        sUriMatcher.addURI(TodoListContract.AUTHORITY, "manifest", MANIFEST);
        sUriMatcher.addURI(TodoListContract.AUTHORITY, "manifest/add", MANIFEST_ADD);
        sUriMatcher.addURI(TodoListContract.AUTHORITY, "manifest/delete", MANIFEST_DELETE);
        sUriMatcher.addURI(TodoListContract.AUTHORITY, "manifest/update", MANIFEST_UPDATE);
        sUriMatcher.addURI(TodoListContract.AUTHORITY, "alarm", ALARM);
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
        Cursor cursor;
        switch (sUriMatcher.match(uri)) {
            case TODOLIST:
                cursor = db.query(DbOpenHelper.TABLE_TODOLIST, projection, selection, selectionArgs, null, null, sortOrder, null);
                break;
            case MANIFEST:
                cursor = db.query(DbOpenHelper.TABLE_MANIFEST, projection, selection, selectionArgs, null, null, sortOrder, null);
                break;
            default:
                throw new IllegalArgumentException("Unknown URL");
        }

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
            case TODOLIST_ADD:
                return "vnd.android.cursor.dir/todolist/add";
            case TODOLIST_DELETE:
                return "vnd.android.cursor.dir/todolist/delete";
            case TODOLIST_UPDATE:
                return "vnd.android.cursor.dir/todolist/update";
            case MANIFEST:
                return "vnd.android.cursor.dir/manifest";
            case MANIFEST_ADD:
                return "vnd.android.cursor.dir/manifest/add";
            case MANIFEST_DELETE:
                return "vnd.android.cursor.dir/manifest/delete";
            case MANIFEST_UPDATE:
                return "vnd.android.cursor.dir/manifest/update";
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
            case TODOLIST_ADD:
                rowId = db.insert(DbOpenHelper.TABLE_TODOLIST, TodoListContract.TodoListColumns._ID, values);
                uriResult = ContentUris.withAppendedId(TodoListContract.TodoListColumns.CONTENT_URI_ADD, rowId);
                break;
            case MANIFEST_ADD:
                rowId = db.insert(DbOpenHelper.TABLE_MANIFEST, TodoListContract.ManifestColumns._ID, values);
                uriResult = ContentUris.withAppendedId(TodoListContract.ManifestColumns.CONTENT_URI, rowId);
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
            case TODOLIST_DELETE:
                count = db.delete(DbOpenHelper.TABLE_TODOLIST, selection, selectionArgs);
                break;
            case MANIFEST_DELETE:
                count = db.delete(DbOpenHelper.TABLE_MANIFEST, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Cannot delete from URL: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int count;
        SQLiteDatabase db = DbOpenHelper.get().getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case TODOLIST_UPDATE:
                count = db.update(DbOpenHelper.TABLE_TODOLIST, values, selection, selectionArgs);
                break;
            case MANIFEST_UPDATE:
                count = db.update(DbOpenHelper.TABLE_MANIFEST, values, selection, selectionArgs);
                break;
            default: {
                throw new UnsupportedOperationException(
                        "Cannot update URL: " + uri);
            }
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
