package com.shang.todolist;

/**
 * 刷新TodoList的通知事件
 */
public class RefreshEvent {
    public int id;

    public RefreshEvent() {
    }

    public RefreshEvent(int id) {
        this.id = id;
    }
}
