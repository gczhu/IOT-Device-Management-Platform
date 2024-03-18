package com.zju.bs.springboot.entity;

/**
 * @author 祝广程
 * @version 1.0
 */
public class ModifyInfo {
    private String name;
    private String type;
    private String description;
    private String username;
    private int device_id;

    public ModifyInfo(String name, String type, String description, String username, int device_id) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.username = username;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getDevice_id() {
        return device_id;
    }

    public void setDevice_id(int device_id) {
        this.device_id = device_id;
    }

    @Override
    public String toString() {
        return "ModifyInfo{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", username='" + username + '\'' +
                ", device_id='" + device_id + '\'' +
                '}';
    }
}
