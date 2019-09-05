package com.shang.todolist.event;

/**
 * 删除了清单的通知事件
 */
public class DeleteManifestEvent {
    public int pos;

    public DeleteManifestEvent(int pos) {
        this.pos = pos;
    }
}
