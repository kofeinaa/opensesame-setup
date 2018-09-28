package com.droid.opensesame.setup;

public class Row {

    private String header;
    private String description;

    public Row() {
    }

    public Row(String title, String description) {
        this.header = title;
        this.description = description;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
