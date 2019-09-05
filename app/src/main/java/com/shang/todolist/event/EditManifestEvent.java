package com.shang.todolist.event;

import com.shang.todolist.db.ManifestBean;

/**
 * 编辑清单的通知事件
 */
public class EditManifestEvent {
    public ManifestBean bean;

    public EditManifestEvent(ManifestBean bean) {
        this.bean = bean;
    }
}
