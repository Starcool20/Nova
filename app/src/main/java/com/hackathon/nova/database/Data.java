package com.hackathon.nova.database;

public class Data {
    private String name;
    private String key;

    public Data(String name, String key) {
        this.name = name;
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        return "Name: " + name + ", Key: " + key;
    }
}