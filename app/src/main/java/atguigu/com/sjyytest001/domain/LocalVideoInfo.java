package atguigu.com.sjyytest001.domain;

/**
 * Created by ASUS on 2017/5/19.
 */

public class LocalVideoInfo {

    private String name ;
    private long duration;
    private long size;
    private String data;

    public LocalVideoInfo(String name, long size, long duration, String data) {
        this.name = name;
        this.size = size;
        this.duration = duration;
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "LocalVideoInfo{" +
                "name='" + name + '\'' +
                ", duration=" + duration +
                ", size=" + size +
                ", data='" + data + '\'' +
                '}';
    }
}
