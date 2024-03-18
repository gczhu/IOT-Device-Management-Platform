package com.zju.bs.springboot.entity;

/**
 * @author 祝广程
 * @version 1.0
 */
public class MQTTMsgInfo {
    private int msg_id;
    private String device_name;
    private String info;
    private int value;
    private int alert;
    private double longitude;
    private double latitude;
    private String time;

    public MQTTMsgInfo(int msg_id, String device_name, String info, int value, int alert, double longitude, double latitude, String time) {
        this.msg_id = msg_id;
        this.device_name = device_name;
        this.info = info;
        this.value = value;
        this.alert = alert;
        this.longitude = longitude;
        this.latitude = latitude;
        this.time = time;
    }

    public int getMsg_id() {
        return msg_id;
    }

    public void setMsg_id(int msg_id) {
        this.msg_id = msg_id;
    }

    public String getDevice_name() {
        return device_name;
    }

    public void setDevice_name(String device_name) {
        this.device_name = device_name;
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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "MQTTMsgInfo{" +
                "msg_id=" + msg_id +
                ", device_name='" + device_name + '\'' +
                ", info='" + info + '\'' +
                ", value=" + value +
                ", alert=" + alert +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", time='" + time + '\'' +
                '}';
    }
}
