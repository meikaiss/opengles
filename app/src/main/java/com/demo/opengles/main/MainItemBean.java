package com.demo.opengles.main;

import androidx.appcompat.app.AppCompatActivity;

public class MainItemBean {
    public String name;
    public Class<? extends AppCompatActivity> clz;

    public MainItemBean(String name, Class<? extends AppCompatActivity> clz) {
        this.name = name;
        this.clz = clz;
    }
}
