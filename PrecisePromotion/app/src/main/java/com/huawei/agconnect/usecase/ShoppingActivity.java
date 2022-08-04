package com.huawei.agconnect.usecase;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.huawei.agconnect.applinking.AGConnectAppLinking;

/**
 * ShoppingActivity to display the details of purchase completion.
 *
 * @since 2022-06-15
 * <p>
 * Copyright (c) HuaWei Technologies Co., Ltd. 2012-2021. All rights reserved.
 */
public class ShoppingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping);

        /**
         * receive AppLinking
         */
        AGConnectAppLinking.getInstance().getAppLinking(this).addOnSuccessListener(resolvedLinkData -> {
            Uri deepLink = null;
            if (resolvedLinkData!= null) {
                deepLink = resolvedLinkData.getDeepLink();
                Log.i("AppLinkingCodeLab", "Open From App Linkin g: " + deepLink.toString());
            } }).addOnFailureListener(e -> {
                            Log.w("MainActivity", "getAppLinking:onFailure", e);
            });
    }
}

