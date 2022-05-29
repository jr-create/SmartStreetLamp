package com.wjr.cdc.bean;

import java.io.Serializable;

/**
 * @author 29375-wjr
 * @Package: com.wjr.cdc.bean
 * @ClassName: TempSource
 * @create 2022-05-13 11:36
 * @Description:
 */
public class TempSource implements Serializable {
    String Id;
    String name;
    long timestamp;

    @Override
    public String toString() {
        return "TempSource{" +
                "Id='" + Id + '\'' +
                ", name='" + name + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }

    public TempSource(String id, String name, long timestamp) {
        Id = id;
        this.name = name;
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
