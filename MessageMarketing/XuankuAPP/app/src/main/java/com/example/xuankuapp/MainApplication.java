package com.example.xuankuapp;

import android.app.Application;

import com.example.xuankuapp.model.UserInfoAction;
import com.huawei.agconnect.AGConnectInstance;

/**
 * MainApplication,processes initialization operations during startup .
 *
 * @since 2022-08-02
 * <p>
 * Copyright (c) HuaWei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */
public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        /**
         * initialize UserInfoAction.
         */
        initializeAction();
    }

    /**
     * Initialize the UserInfoAction method to create a new Action object.
     */
    private void initializeAction(){
        UserInfoAction.initAGConnectCloudDB(this);
        UserInfoAction action = new UserInfoAction();
        action.createObjectType();
        AGConnectInstance.initialize(this);
    }
}
