package com.shang.todolist.db;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 待办事项 映射的实体类
 */
public class TodoBean implements Parcelable {
    //待办序号，主键=创建时间
    public long id;
    //排序的ID
    public int sortId;
    //待办事项标题
    public String title = "";
    //待办具体描述
    public String description = "";
    //待办时间
    public long alarm;
    public int remind;
    //状态 0代办(false)，1已处理(true)
    public boolean status;
    //标记优先级 0:红色    1:橙色   2:蓝色    3:普通任务
    public int mark;
    public long manifest;


    public TodoBean() {
    }

    public TodoBean(long id, int sortId, String title, String description, long alarm, int remind, boolean status, int mark, long manifest) {
        this.id = id;
        this.sortId = sortId;
        this.title = title;
        this.description = description;
        this.alarm = alarm;
        this.remind = remind;
        this.status = status;
        this.mark = mark;
        this.manifest = manifest;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeInt(this.sortId);
        dest.writeString(this.title);
        dest.writeString(this.description);
        dest.writeLong(this.alarm);
        dest.writeInt(this.remind);
        dest.writeByte(this.status ? (byte) 1 : (byte) 0);
        dest.writeInt(this.mark);
        dest.writeLong(this.manifest);
    }

    protected TodoBean(Parcel in) {
        this.id = in.readLong();
        this.sortId = in.readInt();
        this.title = in.readString();
        this.description = in.readString();
        this.alarm = in.readLong();
        this.remind = in.readInt();
        this.status = in.readByte() != 0;
        this.mark = in.readInt();
        this.manifest = in.readLong();
    }

    public static final Creator<TodoBean> CREATOR = new Creator<TodoBean>() {
        @Override
        public TodoBean createFromParcel(Parcel source) {
            return new TodoBean(source);
        }

        @Override
        public TodoBean[] newArray(int size) {
            return new TodoBean[size];
        }
    };
}
