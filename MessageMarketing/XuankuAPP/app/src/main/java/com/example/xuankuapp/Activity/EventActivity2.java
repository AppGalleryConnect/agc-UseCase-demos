package com.example.xuankuapp.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;

import com.example.xuankuapp.R;
import com.example.xuankuapp.model.UserInfo;
import com.example.xuankuapp.model.UserInfoAction;
import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectUser;
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery;

import java.util.ArrayList;
import java.util.Date;

/**
 * EventActivity2 display the content information of activity 2.
 *
 * @since 2022-08-02
 * <p>
 * Copyright (c) HuaWei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */
public class EventActivity2 extends AppCompatActivity implements UserInfoAction.UiCallBack{

    private Button bt_purchase;
    private Button bt_purchase2;
    private Button bt_purchase3;
    private Button bt_purchase4;

    private Handler mHandler = null;
    private UserInfoAction userInfoAction;
    private ArrayList<UserInfo> list = new ArrayList<>();
    private String uid = null;
    private String phoneNumber = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event2);
        bt_purchase = findViewById(R.id.bt_purchase);
        bt_purchase2 = findViewById(R.id.bt_purchase2);
        bt_purchase3 = findViewById(R.id.bt_purchase3);
        bt_purchase4 = findViewById(R.id.bt_purchase4);

        /**
         * Obtain the uid and phone number of a user who has logged in.
         */
        AGConnectUser user = AGConnectAuth.getInstance().getCurrentUser();
        if (user != null){
            uid = user.getUid();
            phoneNumber = user.getPhone();
        }

        /**
         * Click Buy to open the cloud database.
         */
        bt_purchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDB();
            }
        });

        bt_purchase2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDB();
            }
        });

        bt_purchase3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDB();
            }
        });

        bt_purchase4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDB();
            }
        });
    }

    /**
     * Handler asynchronous, open the cloud database method.
     */
    private void openDB(){
        mHandler = new Handler(Looper.getMainLooper());
        mHandler.post(() -> {
            userInfoAction = new UserInfoAction();
            userInfoAction.addCallBacks(EventActivity2.this);
            userInfoAction.openCloudDBZoneV2();
        });
    }

    /**
     * Insert Database Method.
     */
    private void insertDB(){
        Date date = new Date();
        int size = list.size();
        Boolean event1 = false;
        if (size == 0){
            event1 = false;
        }else {
            event1 = list.get(0).getEvent1();
        }
        Boolean finalEvent = event1;
        mHandler.post(() -> {
            userInfoAction.upsertUserInfo(userInfoAction.buildUserInfo(
                    uid, phoneNumber, date, finalEvent, true));
        });
    }

    /**
     * Purchase Success Method.
     */
    private void purchaseC(){
        AlertDialog dialog = new AlertDialog.Builder(EventActivity2.this)
                .setMessage("Congratulations on your successful participation in the event.")
                .setPositiveButton("Confirms", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        redirect();
                    }
                }).create();
        dialog.show();
    }

    /**
     * Purchase Failure Method.
     */
    private void purchaseF(){
        AlertDialog dialog = new AlertDialog.Builder(EventActivity2.this)
                .setMessage("You have already participated in this activity. " +
                        "You cannot participate in this activity again.")
                .setPositiveButton("Confirms", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        redirect();
                    }
                }).create();
        dialog.show();
    }

    /**
     * Switching to the Main Page.
     */
    private void redirect() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("uid", uid);
        intent.putExtra("phoneNumber", phoneNumber);
        startActivity(intent);
        finish();
    }

    /**
     * Save all queried data to arrayList and check whether the current user is
     * in the cloud database.
     */
    @Override
    public void onQuery(ArrayList<UserInfo> userInfos) {
        list.clear();
        list.addAll(userInfos);

        //list.size is 0,The login user does not have a cloud database and can purchase it.
        //list.size is 1,Check whether activity 2 has participated.
        //If this parameter is set to false, activity 2 can be purchased.
        int size = list.size();
        if(size == 0 ||(size == 1 && !list.get(0).getEvent2())){
            insertDB();
            purchaseC();
        }else{
            purchaseF();
        }
    }

    /**
     * When you click Buy, open the cloud database, subscribe to the cloud database,
     * and query the current user information based on the user UID.
     */
    @Override
    public void onSubscribe(ArrayList<UserInfo> userInfos) {
        //userInfoAction.queryAll();

        CloudDBZoneQuery<UserInfo> query = CloudDBZoneQuery
                .where(UserInfo.class).equalTo("uid", uid);
        userInfoAction.queryUserInfo(query);
    }
}