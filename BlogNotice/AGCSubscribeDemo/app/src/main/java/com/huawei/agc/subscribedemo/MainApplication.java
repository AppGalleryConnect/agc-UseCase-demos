package com.huawei.agc.subscribedemo;

import android.annotation.SuppressLint;
import android.app.Application;

import com.huawei.agc.subscribedemo.db.ArticleDBAction;
import com.huawei.agc.subscribedemo.db.SubscriptionRecordDBAction;
import com.huawei.agconnect.AGConnectInstance;
import com.huawei.agconnect.auth.AGConnectAuth;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * MainApplication, processes initialization operations during startup.
 *
 * @date 2022-08-02
 * <p>
 * Copyright (c) HuaWei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */
public class MainApplication extends Application {

    public static String uid = "", phone = "";

    @Override
    public void onCreate() {
        super.onCreate();


        // signOut Current User
        AGConnectAuth.getInstance().signOut();

        ArticleDBAction.initAGConnectCloudDB(this);
        ArticleDBAction action = new ArticleDBAction();
        action.createObjectType();
//        SubscriptionRecordDBAction recordDBAction = new SubscriptionRecordDBAction();
//        recordDBAction.createObjectType();
        AGConnectInstance.initialize(this);

    }
}
