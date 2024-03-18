package com.zju.bs.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

/**
 * @author 祝广程
 * @version 1.0
 */
public class Device {
    @TableId(type = IdType.AUTO)
    private int device_id;
    private String name;
    private String type;
    private int state;
    private String description;
    private double longitude;
    private double latitude;
    private String username;
    private int value;
    private String created_time;

    public Device(int device_id, String name, String type, int state, String description, double longitude, double latitude, String username, int value, String created_time) {
        this.device_id = device_id;
        this.name = name;
        this.type = type;
        this.state = state;
        this.description = description;
        this.longitude = longitude;
        this.latitude = latitude;
        this.username = username;
        this.value = value;
        this.created_time = created_time;
    }

    public Device(String name, String type, int state, String description, double longitude, double latitude, String username, int value, String created_time) {
        this.name = name;
        this.type = type;
        this.state = state;
        this.description = description;
        this.longitude = longitude;
        this.latitude = latitude;
        this.username = username;
        this.value = value;
        this.created_time = created_time;
    }

    public int getDevice_id() {
        return device_id;
    }

    public void setDevice_id(int device_id) {
        this.device_id = device_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getCreated_time() {
        return created_time;
    }

    public void setCreated_time(String created_time) {
        this.created_time = created_time;
    }

    @Override
    public String toString() {
        return "Device{" +
                "device_id=" + device_id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", state=" + state +
                ", description='" + description + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", username='" + username + '\'' +
                ", value=" + value +
                ", created_time='" + created_time + '\'' +
                '}';
    }
}
