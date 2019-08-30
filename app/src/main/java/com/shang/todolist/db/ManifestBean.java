package com.shang.todolist.db;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 清单 实体类
 */
public class ManifestBean implements Parcelable {
    //待办序号，主键=创建时间
    public long id;
    //排序的ID
    public int sortId;
    //待办事项标题
    public String name = "";
    //待办具体描述
    public int folder;

    public ManifestBean() {
    }

    public ManifestBean(long id, int sortId, String name, int folder) {
        this.id = id;
        this.sortId = sortId;
        this.name = name;
        this.folder = folder;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeInt(this.sortId);
        dest.writeString(this.name);
        dest.writeInt(this.folder);
    }

    protected ManifestBean(Parcel in) {
        this.id = in.readLong();
        this.sortId = in.readInt();
        this.name = in.readString();
        this.folder = in.readInt();
    }

    public static final Creator<ManifestBean> CREATOR = new Creator<ManifestBean>() {
        @Override
        public ManifestBean createFromParcel(Parcel source) {
            return new ManifestBean(source);
        }

        @Override
        public ManifestBean[] newArray(int size) {
            return new ManifestBean[size];
        }
    };
}
