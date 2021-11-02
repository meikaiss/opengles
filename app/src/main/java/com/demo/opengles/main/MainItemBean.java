package com.demo.opengles.main;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainItemBean {
    public String name;
    public Class<? extends AppCompatActivity> clz;
    public List<String> permissionList = new ArrayList<>();

    public MainItemBean(String name, Class<? extends AppCompatActivity> clz) {
        this.name = name;
        this.clz = clz;
    }

    public MainItemBean(String name, Class<? extends AppCompatActivity> clz,
                        String permission) {
        this.name = name;
        this.clz = clz;
        this.permissionList.add(permission);
    }

    public MainItemBean(String name, Class<? extends AppCompatActivity> clz,
                        String permission1, String permission2) {
        this.name = name;
        this.clz = clz;
        this.permissionList.add(permission1);
        this.permissionList.add(permission2);
    }

    public MainItemBean(String name, Class<? extends AppCompatActivity> clz,
                        String permission1, String permission2, String permission3) {
        this.name = name;
        this.clz = clz;
        this.permissionList.add(permission1);
        this.permissionList.add(permission2);
        this.permissionList.add(permission3);
    }
}
