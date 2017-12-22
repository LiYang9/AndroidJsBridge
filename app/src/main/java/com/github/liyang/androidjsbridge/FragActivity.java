package com.github.liyang.androidjsbridge;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * author : LiYang
 * email  : yang.li@meiauto.cn
 * time   : 2017/12/19
 */
public class FragActivity extends AppCompatActivity implements WebFragment.Fragment2Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frag);
        setTitle("FragActivity");

        getSupportFragmentManager().beginTransaction().addToBackStack("web").add(R.id.fl_frag, WebFragment.newInstance("file:///android_asset/check.html")).commit();
    }

    @Override
    public void send() {
        getSupportFragmentManager().beginTransaction().addToBackStack("web").add(R.id.fl_frag, WebFragment.newInstance("https://www.baidu.com/")).commit();
    }
}
