package com.example.xuankuapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.xuankuapp.R;
import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectAuthCredential;
import com.huawei.agconnect.auth.AGConnectUser;
import com.huawei.agconnect.auth.PhoneAuthProvider;
import com.huawei.agconnect.auth.SignInResult;
import com.huawei.agconnect.auth.VerifyCodeResult;
import com.huawei.agconnect.auth.VerifyCodeSettings;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hmf.tasks.TaskExecutors;
import com.huawei.hms.analytics.HiAnalytics;
import com.huawei.hms.analytics.HiAnalyticsInstance;
import com.huawei.hms.analytics.HiAnalyticsTools;

import java.util.Locale;

/**
 * KotlinActivity test the kotlin function of AGC remote configuration and cloud functions.
 *
 * @since 2022-08-02
 * <p>
 * Copyright (c) HuaWei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText et_countryCode;
    private EditText et_phoneNumber;
    private EditText et_verifyCode;
    private Button bt_obtain;
    private Button bt_phoneLogin;

    private static final String TAG = "XuankuAPP";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        et_countryCode = findViewById(R.id.et_countryCode);
        et_phoneNumber = findViewById(R.id.et_phoneNumber);
        et_verifyCode = findViewById(R.id.et_verifyCode);
        bt_obtain = findViewById(R.id.bt_obtain);
        bt_phoneLogin = findViewById(R.id.bt_phoneLogin);

        /**
         * Check whether a user has logged in. If yes, the home page is displayed.
         * Otherwise, the login page is displayed.
         */
        checkUser();

        /**
         * Initialization
         */
        initialize();

        /**
         * Send a verification code.
         */
        bt_obtain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerifyCode();
            }
        });

        /**
         * Phone number login.
         */
        bt_phoneLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneLogin();
            }
        });

    }

    /**
     * Checking the Login User.
     */
    private void checkUser(){
        AGConnectUser user = AGConnectAuth.getInstance().getCurrentUser();
        if (user != null){
            Toast.makeText(this,"User Detected, Waiting for Autologon",
                    Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    String uid = user.getUid();
                    String phoneNumber = user.getPhone();
                    Intent intent = new Intent(LoginActivity.this,
                            MainActivity.class);
                    intent.putExtra("uid", uid);
                    intent.putExtra("phoneNumber", phoneNumber);
                    //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|
                    // Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            },1000*2);
        }
    }

    /**
     * Initialization Method.
     */
    private void initialize(){
        // Enabling the SDK Log Function
        HiAnalyticsTools.enableLog();
        HiAnalyticsInstance instance = HiAnalytics.getInstance(this);
    }

    /**
     * Verification Code Sending Method.
     */
    public void sendVerifyCode() {
        VerifyCodeSettings settings = VerifyCodeSettings.newBuilder()
                .action(VerifyCodeSettings.ACTION_REGISTER_LOGIN)
                .sendInterval(30)
                .locale(Locale.SIMPLIFIED_CHINESE)
                .build();
        String countryCode = et_countryCode.getText().toString().trim();
        String phoneNumber = et_phoneNumber.getText().toString().trim();
        if (notEmptyString(countryCode) && notEmptyString(phoneNumber)) {
            Task<VerifyCodeResult> task = PhoneAuthProvider.requestVerifyCode(
                    countryCode, phoneNumber, settings);
            task.addOnSuccessListener(TaskExecutors.uiThread(),
                    new OnSuccessListener<VerifyCodeResult>() {
                @Override
                public void onSuccess(VerifyCodeResult verifyCodeResult) {
                    Toast.makeText(LoginActivity.this,
                            "The verification code has been sent.",
                            Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(TaskExecutors.uiThread(), new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(LoginActivity.this,
                            "Failed to send the verification code." + e,
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "requestVerifyCode fail:" + e);
                }
            });
        } else {
            Toast.makeText(LoginActivity.this,
                    "Please enter the phone number and country code.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Method for determining whether the string is not empty.
     */
    public Boolean notEmptyString(String string){
        if (string == null || "".equals(string)){
            return false;
        }else {
            return true;
        }
    }

    /**
     * Phone Number Login Method.
     */
    public void phoneLogin(){
        String countryCode = et_countryCode.getText().toString().trim();
        String phoneNumber = et_phoneNumber.getText().toString().trim();
        String verifyCode = et_verifyCode.getText().toString().trim();
        //Login logic
        AGConnectAuthCredential credential = PhoneAuthProvider.credentialWithVerifyCode(
                countryCode,
                phoneNumber,
                null, // password, can be null
                verifyCode);

        AGConnectAuth.getInstance().signIn(credential)
                .addOnSuccessListener(new OnSuccessListener<SignInResult>() {
                    @Override
                    public void onSuccess(SignInResult signInResult) {
                        //Obtaining Login Information
                        String uid = signInResult.getUser().getUid();
                        String phoneNumber = signInResult.getUser().getPhone();
                        redirect(uid,phoneNumber);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(LoginActivity.this, "Login failed."
                                        + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Login error, error:" + e.getMessage());
                    }
                });
    }

    /**
     * Page Redirection Method.
     */
    private void redirect(String uid,String phoneNumber) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("uid", uid);
        intent.putExtra("phoneNumber", phoneNumber);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}