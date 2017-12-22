package com.github.liyang.androidjsbridge;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import cn.meiauto.matwidget.listview.BaseListAdapter;
import cn.meiauto.matwidget.listview.BaseViewHolder;
import cn.meiauto.matwidget.listview.IConvertView;

/**
 * author : LiYang
 * email  : yang.li@meiauto.cn
 * time   : 2017/12/15
 */
public class WelcomeActivity extends AppCompatActivity {

    private BaseListAdapter<Go> mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        mAdapter = new BaseListAdapter<>(this, android.R.layout.simple_list_item_1, new IConvertView<Go>() {
            @Override
            public void convertDataToView(BaseViewHolder holder, Go go, int position) {
                holder.setText(android.R.id.text1, go.name);
            }
        });
        ListView listView = findViewById(R.id.lv_welcome);
        listView.setAdapter(mAdapter);

        mAdapter.add(new Go("MainActivity", MainActivity.class));
        mAdapter.add(new Go("WebActivity", WebActivity.class));
        mAdapter.add(new Go("FragActivity", FragActivity.class));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(WelcomeActivity.this, mAdapter.getItem(position).clazz));
            }
        });
    }

    class Go {
        String name;
        Class clazz;

        Go(String name, Class clazz) {
            this.name = name;
            this.clazz = clazz;
        }
    }
}
