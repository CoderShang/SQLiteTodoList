package com.shang.todolist.event;

import com.shang.todolist.db.TodoBean;

/**
 * 编辑Todo的通知事件
 */
public class EditTodoEvent {
    public TodoBean bean;

    public EditTodoEvent(TodoBean bean) {
        this.bean = bean;
    }
}
