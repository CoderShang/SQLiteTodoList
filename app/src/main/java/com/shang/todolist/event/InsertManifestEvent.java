package com.shang.todolist.event;

import com.shang.todolist.db.ManifestBean;

/**
 * 添加清单的通知事件
 */
public class InsertManifestEvent {
    public ManifestBean bean;

    public InsertManifestEvent(ManifestBean bean) {
        this.bean = bean;
    }
}
