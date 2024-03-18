package com.zju.bs.springboot.entity;

/**
 * @author 祝广程
 * @version 1.0
 */
public class CreateInfo {
    private String name;
    private String type;
    private String description;
    private String username;

    public CreateInfo(String name, String type, String description, String username) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.username = username;
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

    @Override
    public String toString() {
        return "CreateInfo{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}
