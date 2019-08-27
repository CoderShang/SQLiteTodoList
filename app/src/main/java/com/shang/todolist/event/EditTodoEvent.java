package com.shang.todolist.event;

/**
 * 编辑Todo的通知事件
 */
public class EditTodoEvent {
    public long id;

    public EditTodoEvent() {
    }

    public EditTodoEvent(long id) {
        this.id = id;
    }
}
