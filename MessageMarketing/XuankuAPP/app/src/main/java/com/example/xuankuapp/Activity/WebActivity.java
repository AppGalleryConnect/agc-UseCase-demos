package com.example.xuankuapp.Activity;

import android.os.Bundle;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.xuankuapp.R;

/**
 * WebActivity test the WebView of an Embedded Web Page.
 *
 * @since 2022-08-02
 * <p>
 * Copyright (c) HuaWei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */
public class WebActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        WebView webView =(WebView) findViewById(R.id.webview);
        webView.loadUrl("https://xuankuapp.dra.agchosting.link");
        webView.getSettings().setJavaScriptEnabled(true);//Support javascript
    }
}