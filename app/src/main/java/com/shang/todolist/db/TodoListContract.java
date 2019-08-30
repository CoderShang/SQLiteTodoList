
package com.shang.todolist.db;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * 与TodoList表的契约类
 *
 * @author shang
 */
public final class TodoListContract {

    public static final String AUTHORITY = "com.shang.todolist";


    private TodoListContract() {
    }

    public interface TodoListColumns extends BaseColumns {
        String queryPath = "/todolist";
        String addPath = "/todolist/add";
        String deletePath = "/todolist/delete";
        String updatePath = "/todolist/update";
        /**
         * The content:// style URL for this table.
         */
        Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + queryPath);
        Uri CONTENT_URI_ADD = Uri.parse("content://" + AUTHORITY + addPath);
        Uri CONTENT_URI_DELETE = Uri.parse("content://" + AUTHORITY + deletePath);
        Uri CONTENT_URI_UPDATE = Uri.parse("content://" + AUTHORITY + updatePath);

        String _ID = "_ID";
        /**
         * 用于排序的ID
         */
        String SORT_ID = "SORT_ID";
        /**
         * 待办事项标题
         */
        String TITLE = "TITLE";
        /**
         * 待办具体描述
         */
        String DESCRIPTION = "DESCRIPTION";
        /**
         * 待办时间
         */
        String ALARM = "ALARM";
        /**
         * 状态 0代办，1已处理
         */
        String STATUS = "STATUS";
        /**
         * 是否被重点标记 1:被标记  0:无标记
         */
        String MARK = "MARK";
        String MANIFEST = "MANIFEST";
    }

    public interface ManifestColumns extends BaseColumns {
        /**
         * The content:// style URL for this table.
         */
        Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/manifest");

        String _ID = "_ID";
        /**
         * 用于排序的ID
         */
        String SORT_ID = "SORT_ID";
        /**
         * 清单名称
         */
        String NAME = "NAME";
        /**
         * 文件夹ID
         */
        String FOLDER = "FOLDER";
    }
}
