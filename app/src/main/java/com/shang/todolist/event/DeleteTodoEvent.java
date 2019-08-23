package com.shang.todolist.event;

/**
 * 删除了Todo的通知事件
 */
public class DeleteTodoEvent {
    public int pos;

    public DeleteTodoEvent() {
    }

    public DeleteTodoEvent(int pos) {
        this.pos = pos;
    }
}
