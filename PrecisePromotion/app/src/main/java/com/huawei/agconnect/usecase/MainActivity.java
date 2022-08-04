package com.huawei.agconnect.usecase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.huawei.agconnect.appmessaging.AGConnectAppMessaging;
import com.huawei.agconnect.appmessaging.AGConnectAppMessagingCallback;
import com.huawei.agconnect.appmessaging.AGConnectAppMessagingOnClickListener;
import com.huawei.agconnect.appmessaging.AGConnectAppMessagingOnDismissListener;
import com.huawei.agconnect.appmessaging.AGConnectAppMessagingOnDisplayListener;
import com.huawei.agconnect.appmessaging.model.Action;
import com.huawei.agconnect.appmessaging.model.AppMessage;
import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectUser;
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery;
import com.huawei.agconnect.common.api.AGCInstanceID;
import com.huawei.agconnect.remoteconfig.AGConnectConfig;
import com.huawei.agconnect.usecase.model.UserInfo;
import com.huawei.agconnect.usecase.model.UserInfoAction;
import com.huawei.hms.analytics.HiAnalytics;
import com.huawei.hms.analytics.HiAnalyticsInstance;
import com.huawei.hms.analytics.HiAnalyticsTools;
import java.util.ArrayList;

/**
 * MainActivity to display the details  of events.
 *
 * @since 2022-06-152
 * <p>
 * Copyright (c) HuaWei Technologies Co., Ltd. 2012-2021. All rights reserved.
 */
public class MainActivity extends AppCompatActivity implements UserInfoAction.UiCallBack{
    private static final String TAG = "MainActivity";
    private AGConnectAppMessaging appMessaging;
    private TextView textView;
    private AGConnectConfig config;
    public String eventNumber;
    private Handler mHandler = null;
    private String uid = null;
    private String phoneNumber = null;
    private UserInfoAction userInfoAction;
    private ArrayList<UserInfo> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textview);
        Button btn_event1 = findViewById(R.id.event1);
        Button btn_event2 = findViewById(R.id.event2);
        //Obtaining the aaid
        String aaid = AGCInstanceID.getInstance(this).getId();
        textView.setText("aaid:"+aaid);
        Log.d(TAG, "getAAID success:" + aaid );

        appMessagingConfig();

        //Obtain the uid and phone number of a user who has logged in.
        AGConnectUser user = AGConnectAuth.getInstance().getCurrentUser();
        if (user != null){
            uid = user.getUid();
            phoneNumber = user.getPhone();
        }

        //The handler is asynchronous. The cloud database is opened.
        mHandler = new Handler(Looper.getMainLooper());
        mHandler.post(() -> {
            userInfoAction = new UserInfoAction();
            userInfoAction.addCallBacks(this);
            userInfoAction.openCloudDBZoneV2();
        });

        //Obtaining a Remote Configuration Object Instance
        config = AGConnectConfig.getInstance();
        getRemoteCongfig();

        btn_event1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                redirect("1");
            }
        });

        btn_event2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                redirect("2");
            }
        });
    }


    public void appMessagingConfig() {
        //Initializing the AGConnectAppMessaging Instance
        appMessaging = AGConnectAppMessaging.getInstance();
        //Forcibly requesting server message data
        AGConnectAppMessaging.getInstance().setForceFetch("AppOnForeground");
        //Message display listener
        appMessaging.addOnDisplayListener(new AGConnectAppMessagingOnDisplayListener() {
            @Override
            public void onMessageDisplay(AppMessage appMessage) {
                Toast.makeText(MainActivity.this, "Message showed", Toast.LENGTH_LONG).show();
            }
        });
        //Message Click Listener
        appMessaging.addOnClickListener(new AGConnectAppMessagingOnClickListener() {
            @Override
            public void onMessageClick(@NonNull AppMessage appMessage, @NonNull Action action) {
                Toast.makeText(MainActivity.this, "Button Clicked", Toast.LENGTH_LONG).show();
            }
        });
        //Message disappear listener
        appMessaging.addOnDismissListener(new AGConnectAppMessagingOnDismissListener() {
            @Override
            public void onMessageDismiss(AppMessage appMessage, AGConnectAppMessagingCallback.DismissType dismissType) {
                Toast.makeText(MainActivity.this, "Message Dismiss, dismiss type: " + dismissType, Toast.LENGTH_LONG).show();
            }
        });

        //Add Custom Layout
        CustomActivity customActivity = new CustomActivity(MainActivity.this);
        appMessaging.addCustomView(customActivity);
    }

    public void redirect(String eventNumber) {
        Intent intent = new Intent();
        switch (eventNumber){
            case "1":
                intent.setClass(MainActivity.this, Event1Activity.class);
                break;
            case "2":
                intent.setClass(MainActivity.this, Event2Activity.class);
                break;
        }
        startActivity(intent);
    }

    /**
     * Get Remote Configuration
     */
    public void getRemoteCongfig() {
        //Fetch remote configuration updates at application startup
        SharedPreferences preferences = this.getApplicationContext().getSharedPreferences("Remote_Config", MODE_PRIVATE);
        long fetchInterval = 12 * 60 * 60L;
        if (preferences.getBoolean("DATA_OLD", false)) {
            fetchInterval = 0;
        }
        config.fetch(fetchInterval).addOnSuccessListener(configValues -> {
            config.apply(configValues);
            //Obtains the value of the remote configuration.
            String value = config.getValueAsString("event_number");
            Log.i(TAG, "RemoteConfig Success: " + value);
            eventNumber = value;
        }).addOnFailureListener(e1 ->
                Log.e(TAG, "getRemoteConfig failed: " + e1.getMessage())
        );
    }

    /**
     * Log in to the home page and trigger the event method in the application message.
     */
    private void appMessage(){
        //Enabling the SDK Log Function
        HiAnalyticsTools.enableLog();
        HiAnalyticsInstance instance = HiAnalytics.getInstance(this);
        //User-defined tracing point, intra-application message triggering event
        Bundle bundle = new Bundle();
        bundle.putString("uid", uid);
        bundle.putString("phoneNumber", phoneNumber);
        instance.onEvent("ShowAppMessaging", bundle);
    }

    /**
     * Save all the queried data to arrayList and check whether the current user is in the cloud database.
     * *If the value of list is 0, the login user does not have a cloud database and can purchase the cloud database.
     * If the value of list is 1, you need to check whether activity 1 has participated. If the value is false, activity 1 can be purchased.
     * @param userInfos
     */
    @Override
    public void onQuery(ArrayList<UserInfo> userInfos) {
        list.clear();
        list.addAll(userInfos);
        int size = list.size();
        Log.i(TAG,"list size ="+size);
        if(size == 0 ||(size == 1 && !list.get(0).getEvent1()) || (size == 1 && !list.get(0).getEvent2())){
            appMessage();
        }
    }


    /**
     * Subscribe to and query all data on the page.
     */
    @Override
    public void onSubscribe(ArrayList<UserInfo> userInfos) {
        //Obtain all user information based on user UID screening.
        CloudDBZoneQuery<UserInfo> query = CloudDBZoneQuery
                .where(UserInfo.class).equalTo("uid", uid);
        userInfoAction.queryUserInfo(query);
    }
}