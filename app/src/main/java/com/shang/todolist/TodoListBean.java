package com.shang.todolist;

/**
 * 待办事项 映射的实体类
 */
public class TodoListBean {
    //待办序号，主键自增长
    public int id=1;
    //用与排序的ID
    public int orderId;
    //待办事项标题
    public String title="待办标题";
    //待办具体描述
    public String description="详细信息详细信息详细信息详细信息详细信息详细信息详细信息详细信息详细信息详细信息详细信息详细信息详细信息详细信息详细信息详细信息详细信息详细信息详细信息详细信息详细信息详细信息详细信息详细信息详细信息详细信息详细信息详细信息详细信息详细信息";
    //待办时间
    public String alarm;
    //创建时间
    public String createTime;
    //状态 0代办，1已处理，-1已删除，-2已过期
    public int status;
    //是否被重点标记 1:被标记  0:无标记
    public int mark;
}
