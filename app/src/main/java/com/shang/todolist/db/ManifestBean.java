package com.shang.todolist.db;


/**
 * 清单 实体类
 */
public class ManifestBean {
    //清单，主键=创建时间
    public long id;
    //排序的ID
    public int sortId;
    //清单名称
    public String name = "";
    //清单中待办的数量
    public int num;
    public boolean selected;

    public ManifestBean() {
    }

    public ManifestBean(long id, int sortId, String name) {
        this.id = id;
        this.sortId = sortId;
        this.name = name;
    }
}
