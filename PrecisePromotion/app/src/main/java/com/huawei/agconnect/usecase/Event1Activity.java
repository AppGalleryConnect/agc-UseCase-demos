package com.huawei.agconnect.usecase;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectUser;
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery;
import com.huawei.agconnect.usecase.model.UserInfo;
import com.huawei.agconnect.usecase.model.UserInfoAction;
import com.huawei.hms.analytics.HiAnalytics;
import com.huawei.hms.analytics.HiAnalyticsInstance;
import com.huawei.hms.analytics.HiAnalyticsTools;
import java.util.ArrayList;

/**
 * Event1Activity to display the details and Method of event1.
 *
 * @since 2022-06-15
 * <p>
 * Copyright (c) HuaWei Technologies Co., Ltd. 2012-2021. All rights reserved.
 */
public class Event1Activity extends AppCompatActivity implements UserInfoAction.UiCallBack{

    private static final String TAG = "Event1Activity";
    private Handler mHandler = null;
    private UserInfoAction userInfoAction;
    private ArrayList<UserInfo> list = new ArrayList<>();
    private String uid = null;
    private String phoneNumber = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event1);

        AGConnectUser user = AGConnectAuth.getInstance().getCurrentUser();
        if (user != null){
            uid = user.getUid();
            phoneNumber = user.getPhone();
        }

        if (getIntent() != null) {
            Button button = findViewById(R.id.buy);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openDB();
                }
            });
        }
    }

    /**
     * Go to the main page.
     */
    private void redirect() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("uid", uid);
        intent.putExtra("phoneNumber", phoneNumber);
        startActivity(intent);
        finish();
    }

    /**
     * The handler is asynchronous. The method of opening the cloud database
     */
    private void openDB(){
        mHandler = new Handler(Looper.getMainLooper());
        mHandler.post(() -> {
            userInfoAction = new UserInfoAction();
            userInfoAction.addCallBacks(Event1Activity.this);
            userInfoAction.openCloudDBZoneV2();
        });
    }

    /**
     * Method of inserting data into the database
     */
    private void insertDB(){
        int size = list.size();
        Boolean event2 = false;
        if (size == 0){
            event2 = false;
        }else {
            event2 = list.get(0).getEvent2();
        }
        Boolean finalEvent = event2;
        mHandler.post(() -> {
            userInfoAction.upsertUserInfo(userInfoAction.buildUserInfo(
                    uid, phoneNumber, true, finalEvent));
        });
    }

    /**
     * Buying Success Method
     */
    private void purchaseC(){
        AlertDialog dialog = new AlertDialog.Builder(Event1Activity.this)
                .setMessage("Congratulations on your successful participation in the event.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        redirect();
                    }
                }).create();
        dialog.show();
        //Enabling the SDK Log Function
        HiAnalyticsTools.enableLog();
        HiAnalyticsInstance instance = HiAnalytics.getInstance(Event1Activity.this);
        //User-defined tracing point. Event triggered by successful participation in activity 1
        Bundle bundle = new Bundle();
        bundle.putString("uid", uid);
        bundle.putString("phoneNumber", phoneNumber);
        instance.onEvent("AttendEvent", bundle);
    }

    /**
     * Purchase Failure Method
     */
    private void purchaseF(){
        AlertDialog dialog = new AlertDialog.Builder(Event1Activity.this)
                .setMessage("You have already participated in this event. You cannot participate in this activity again.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        redirect();
                    }
                }).create();
        dialog.show();
    }

    /**
     *If the value of list is 0, the login user does not have a cloud database and can purchase the cloud database.
     *If the value of list is 1, you need to check whether activity 1 has participated. If the value is false, activity 1 can be purchased.
     */
    @Override
    public void onQuery(ArrayList<UserInfo> userInfos) {
        list.clear();
        list.addAll(userInfos);
        int size = list.size();
        if(size == 0 ||(size == 1 && !list.get(0).getEvent1())){
            insertDB();
            purchaseC();
        }else{
            purchaseF();
        }
    }

    @Override
    public void onSubscribe(ArrayList<UserInfo> userInfos) {
        Log.i(TAG,"uid:"+uid);
        CloudDBZoneQuery<UserInfo> query = CloudDBZoneQuery.where(UserInfo.class).equalTo("uid", uid);
        userInfoAction.queryUserInfo(query);
    }
}
