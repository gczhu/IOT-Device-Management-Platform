package com.zju.bs.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

/**
 * @author 祝广程
 * @version 1.0
 */
public class MQTTMsg {
    @TableId(type = IdType.AUTO)
    private int msg_id;
    private int device_id;
    private String info;
    private int value;
    private int alert;
    private double longitude;
    private double latitude;
    private String timestamp;
    private String time;

    public MQTTMsg(int msg_id, int device_id, String info, int value, int alert, double longitude, double latitude, String timestamp, String time) {
        this.msg_id = msg_id;
        this.device_id = device_id;
        this.info = info;
        this.value = value;
        this.alert = alert;
        this.longitude = longitude;
        this.latitude = latitude;
        this.timestamp = timestamp;
        this.time = time;
    }

    public int getMsg_id() {
        return msg_id;
    }

    public void setMsg_id(int msg_id) {
        this.msg_id = msg_id;
    }

    public int getDevice_id() {
        return device_id;
    }

    public void setDevice_id(int device_id) {
        this.device_id = device_id;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getAlert() {
        return alert;
    }

    public void setAlert(int alert) {
        this.alert = alert;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "MQTTMsg{" +
                "msg_id=" + msg_id +
                ", device_id=" + device_id +
                ", info='" + info + '\'' +
                ", value=" + value +
                ", alert=" + alert +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", timestamp='" + timestamp +
                ", time='" + time + '\'' +
                '}';
    }
}
