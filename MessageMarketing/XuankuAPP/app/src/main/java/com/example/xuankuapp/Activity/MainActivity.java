package com.example.xuankuapp.Activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.xuankuapp.R;
import com.example.xuankuapp.model.UserInfo;
import com.example.xuankuapp.model.UserInfoAction;
import com.example.xuankuapp.util.HttpUtil;
import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectUser;
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery;
import com.huawei.agconnect.crash.AGConnectCrash;
import com.huawei.agconnect.function.AGCFunctionException;
import com.huawei.agconnect.function.AGConnectFunction;
import com.huawei.agconnect.function.FunctionResult;
import com.huawei.agconnect.remoteconfig.AGConnectConfig;
import com.huawei.agconnect.remoteconfig.ConfigValues;
import com.huawei.hmf.tasks.OnCompleteListener;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.analytics.HiAnalytics;
import com.huawei.hms.analytics.HiAnalyticsInstance;
import com.huawei.hms.analytics.HiAnalyticsTools;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * MainActivity is the main interface after you enter the application to receive App Messaging.
 *
 * @since 2022-08-02
 * <p>
 * Copyright (c) HuaWei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */
public class MainActivity extends AppCompatActivity implements UserInfoAction.UiCallBack{

    private TextView tv_greeting;
    private Button bt_crash;
    private Button bt_exception;
    private Button bt_customReport;
    private Button bt_getOnlineConfig;
    private EditText et_year;
    private Button bt_cloud;
    private Button bt_signOut;
    private Button bt_network;

    private static final String GREETING_KEY = "GREETING_KEY";
    private static final String SET_BOLD_KEY = "SET_BOLD_KEY";
    private AGConnectConfig config;
    private static final String TAG = "XuankuAPP";
    private Handler mHandler = null;
    private UserInfoAction userInfoAction;
    private ArrayList<UserInfo> list = new ArrayList<>();
    private String uid = null;
    private String phoneNumber = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_greeting = findViewById(R.id.tv_greeting);
        bt_crash = findViewById(R.id.bt_crash);
        bt_exception = findViewById(R.id.bt_exception);
        bt_customReport = findViewById(R.id.bt_customReport);
        bt_getOnlineConfig = findViewById(R.id.bt_getOnlineConfig);
        et_year = findViewById(R.id.et_year);
        bt_cloud = findViewById(R.id.bt_cloud);
        bt_signOut = findViewById(R.id.bt_signOut);
        bt_network = findViewById(R.id.bt_network);

        /**
         * Obtain the uid and phone number of a user who has logged in.
         */
        AGConnectUser user = AGConnectAuth.getInstance().getCurrentUser();
        if (user != null){
            uid = user.getUid();
            phoneNumber = user.getPhone();
        }

        /**
         * The handler is asynchronous. The cloud database is opened.
         */
        mHandler = new Handler(Looper.getMainLooper());
        mHandler.post(() -> {
            userInfoAction = new UserInfoAction();
            userInfoAction.addCallBacks(this);
            userInfoAction.openCloudDBZoneV2();
        });

        /**
         * Update Greetings.
         */
        updateGreeting();

        /**
         * Make Crash.
         */
        bt_crash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeCrash();
            }
        });

        /**
         * Make Exception.
         */
        bt_exception.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeException();
            }
        });

        /**
         * Custom Reports.
         */
        bt_customReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customReport();
            }
        });

        /**
         * Obtaining Online Configurations.
         */
        getOnlineConfig();
        /**
         * Click Get and Apply Remote Configuration.
         */
        bt_getOnlineConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchAndApply();
            }
        });

        /**
         * Cloud function.
         */
        bt_cloud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cloudFunction();
            }
        });

        /**
         * Log out of the current account.
         */
        bt_signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        /**
         * Sending a network request.
         */
        bt_network.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendNetworkRequest();
            }
        });
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
        instance.onEvent("loginToMain", bundle);
    }

    /**
     * Method for updating greetings.
     */
    private void updateGreeting(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                tv_greeting.setText("Hello," + phoneNumber);
            }
        },200);
    }

    /**
     * Method for making crash.
     */
    private void makeCrash(){
        AGConnectCrash.getInstance().testIt(MainActivity.this);
    }

    /**
     * Method for making exception.
     */
    private void makeException(){
        try {
            throw new Exception();
        }catch (Exception e){
            AGConnectCrash.getInstance().recordException(e);
        }
    }

    /**
     * Method for customing Reports.
     */
    private void customReport(){
        AGConnectCrash.getInstance().setUserId("testuser");
        AGConnectCrash.getInstance().log(Log.DEBUG,"set debug log.");
        AGConnectCrash.getInstance().log(Log.INFO,"set info log.");
        AGConnectCrash.getInstance().log(Log.WARN,"set warning log.");
        AGConnectCrash.getInstance().log(Log.ERROR,"set error log.");
        AGConnectCrash.getInstance().setCustomKey("stringKey", "Hello world");
        AGConnectCrash.getInstance().setCustomKey("booleanKey", false);
        AGConnectCrash.getInstance().setCustomKey("doubleKey", 1.1);
        AGConnectCrash.getInstance().setCustomKey("floatKey", 1.1f);
        AGConnectCrash.getInstance().setCustomKey("intKey", 0);
        AGConnectCrash.getInstance().setCustomKey("longKey", 11L);
    }

    /**
     * Method for obtaining Online Configuration.
     */
    private void getOnlineConfig(){

        Map map = new HashMap<>();
        map.put("custom key","custom value");
        map.put("key","value");
        AGConnectConfig.getInstance().setCustomAttributes(map);
        map = AGConnectConfig.getInstance().getCustomAttributes();


        config = AGConnectConfig.getInstance();
        config.applyDefault(R.xml.remote_config);
        tv_greeting.setText(config.getValueAsString(GREETING_KEY));
        Boolean isBold = config.getValueAsBoolean(SET_BOLD_KEY);
        if (isBold){
            tv_greeting.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        }
    }

    /**
     * Method for clicking to Get and Apply Remote Configuration.
     */
    private void fetchAndApply(){
        config.fetch(0).addOnSuccessListener(new OnSuccessListener<ConfigValues>() {
            @Override
            public void onSuccess(ConfigValues configValues) {
                config.apply(configValues);
                //The UI is updated successfully.
                updateUI();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                tv_greeting.setText("fetch setting failed:" + e.getMessage());
            }
        });
    }

    /**
     * Update UI Method.
     */
    private void updateUI(){
        String text = config.getValueAsString(GREETING_KEY);
        Boolean isBold = config.getValueAsBoolean(SET_BOLD_KEY);
        tv_greeting.setText(text);
        if (isBold){
            tv_greeting.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        }
    }

    /**
     * Cloud Function Method.
     */
    private void cloudFunction(){
        String inputText = et_year.getText().toString();
        if (inputText.equals("") || !isInputLegit(inputText)){
            tv_greeting.setText("The entered year is incorrect.");
            return;
        }
        AGConnectFunction function = AGConnectFunction.getInstance();
        HashMap<String, String> map = new HashMap<>();
        map.put("year", inputText);

        function.wrap("testfunction-$latest").call(map)
                .addOnCompleteListener(new OnCompleteListener<FunctionResult>() {
                    @Override
                    public void onComplete(Task<FunctionResult> task) {
                        if (task.isSuccessful()){
                            String value = task.getResult().getValue();
                            try {
                                JSONObject object = new JSONObject(value);
                                String result = (String)object.get("result");
                                tv_greeting.setText(result);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Log.i(TAG, value);
                        } else {
                            Exception e = task.getException();
                            if (e instanceof AGCFunctionException){
                                AGCFunctionException functionException = (AGCFunctionException)e;
                                int errCode = functionException.getCode();
                                String message = functionException.getMessage();
                                Log.e(TAG, "errorCode: " + errCode + ", message: " + message);
                            }
                        }
                    }
                });
    }

    /**
     * Check whether a valid method is entered.
     */
    private boolean isInputLegit(@NotNull String input){
        for (int i = 0; i < input.length(); i++){
            System.out.println(input.charAt(i));
            if (!Character.isDigit(input.charAt(i))){
                return false;
            }
        }
        return true;
    }

    /**
     * Logging Out of an Account.
     */
    private void signOut(){
        AGConnectAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Method of sending network requests.
     */
    private void sendNetworkRequest(){
        Log.d("apmsAndroidDemo", "send network request.");
        HttpUtil.oneRequest();
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
        if(size == 0 ||(size == 1 && !list.get(0).getEvent1()) ||
                (size == 1 && !list.get(0).getEvent2())){
            appMessage();
        }
    }

    /**
     * Subscribe to and query all data on the page.
     */
    @Override
    public void onSubscribe(ArrayList<UserInfo> userInfos) {
        //userInfoAction.queryAll();

        //Obtain all user information based on user UID screening.
        CloudDBZoneQuery<UserInfo> query = CloudDBZoneQuery
                .where(UserInfo.class).equalTo("uid", uid);
        userInfoAction.queryUserInfo(query);
    }
}






















