package com.huawei.agconnect.usecase;

import android.app.Application;
import com.huawei.agconnect.usecase.model.UserInfoAction;
import com.huawei.agconnect.AGConnectInstance;

/**
 * MainApplication to Initialize the UserInfoAction.
 *
 * @since 2022-06-15
 * <p>
 * Copyright (c) HuaWei Technologies Co., Ltd. 2012-2021. All rights reserved.
 */
public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
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
