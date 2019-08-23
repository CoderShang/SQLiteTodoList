package com.shang.todolist.event;

/**
 * 编辑Todo的通知事件
 */
public class EditTodoEvent {
    public int id;

    public EditTodoEvent() {
    }

    public EditTodoEvent(int id) {
        this.id = id;
    }
}
