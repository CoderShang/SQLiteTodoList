package com.shang.todolist.db;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 待办事项 映射的实体类
 */
public class TodoListBean implements Parcelable {
    //待办序号，主键自增长
    public int id;
    //前一条ID
    public int frontId;
    //后一条ID
    public int behindId;
    //待办事项标题
    public String title = "";
    //待办具体描述
    public String description = "";
    //待办时间
    public long alarm;
    //创建时间
    public long createTime;
    //状态 0代办(false)，1已处理(true)
    public boolean status;
    //是否被重点标记 1:被标记  0:无标记
    public int mark;


    public TodoListBean() {
    }


    public TodoListBean(int id, int frontId, int behindId, String title, String description, long alarm, long createTime, boolean status, int mark) {
        this.id = id;
        this.frontId = frontId;
        this.behindId = behindId;
        this.title = title;
        this.description = description;
        this.alarm = alarm;
        this.createTime = createTime;
        this.status = status;
        this.mark = mark;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeInt(this.frontId);
        dest.writeInt(this.behindId);
        dest.writeString(this.title);
        dest.writeString(this.description);
        dest.writeLong(this.alarm);
        dest.writeLong(this.createTime);
        dest.writeByte(this.status ? (byte) 1 : (byte) 0);
        dest.writeInt(this.mark);
    }

    protected TodoListBean(Parcel in) {
        this.id = in.readInt();
        this.frontId = in.readInt();
        this.behindId = in.readInt();
        this.title = in.readString();
        this.description = in.readString();
        this.alarm = in.readLong();
        this.createTime = in.readLong();
        this.status = in.readByte() != 0;
        this.mark = in.readInt();
    }

    public static final Creator<TodoListBean> CREATOR = new Creator<TodoListBean>() {
        @Override
        public TodoListBean createFromParcel(Parcel source) {
            return new TodoListBean(source);
        }

        @Override
        public TodoListBean[] newArray(int size) {
            return new TodoListBean[size];
        }
    };
}
