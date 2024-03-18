package com.zju.bs.springboot.entity;

/**
 * @author 祝广程
 * @version 1.0
 */
public class QueryInfo {
    private int page;
    private int limit;
    private String id;
    private String name;
    private String type;
    private String state;
    private String startDate;
    private String endDate;
    private String sort;

    public QueryInfo(int page, int limit, String id, String name, String type, String state, String startDate, String endDate, String sort) {
        this.page = page;
        this.limit = limit;
        this.id = id;
        this.name = name;
        this.type = type;
        this.state = state;
        this.startDate = startDate;
        this.endDate = endDate;
        this.sort = sort;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    @Override
    public String toString() {
        return "QueryInfo{" +
                "page=" + page +
                ", limit=" + limit +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", state=" + state +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", sort='" + sort + '\'' +
                '}';
    }
}
