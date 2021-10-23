package com.demo.opengles.main;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by meikai on 2021/10/04.
 */
public class BaseActivity extends AppCompatActivity {

    protected Activity activity = this;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String title = getIntent().getStringExtra("title");
        if (TextUtils.isEmpty(title)) {
            setTitle(getClass().getSimpleName());
        } else {
            setTitle(title);
        }

    }

}
