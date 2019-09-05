package com.shang.todolist.event;

import com.shang.todolist.db.TodoBean;

/**
 * 添加Todo的通知事件
 */
public class InsertTodoEvent {
    public TodoBean bean;

    public InsertTodoEvent(TodoBean bean) {
        this.bean = bean;
    }
}
