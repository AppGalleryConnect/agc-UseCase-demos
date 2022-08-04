package com.huawei.agconnect.usecase;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.huawei.agconnect.appmessaging.AGConnectAppMessagingCallback;
import com.huawei.agconnect.appmessaging.AGConnectAppMessagingDisplay;
import com.huawei.agconnect.appmessaging.model.AppMessage;

/**
 * CustomActivity to display the customized message layout.
 *
 * @since 2022-06-15
 * <p>
 * Copyright (c) HuaWei Technologies Co., Ltd. 2012-2021. All rights reserved.
 */
public class CustomActivity implements AGConnectAppMessagingDisplay {
    private static final String TAG = "CustomView";
    MainActivity activity;

    public CustomActivity(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    public void displayMessage(@NonNull AppMessage appMessage, @NonNull AGConnectAppMessagingCallback callback) {
        Log.d(TAG, appMessage.getId() + "");
        showDialog(appMessage, callback);
    }

    /**
     * Display the customized message pop-up window.
     */
    private void showDialog(@NonNull final AppMessage appMessage, @NonNull final AGConnectAppMessagingCallback callback) {
        View view = LayoutInflater.from(activity).inflate(R.layout.activity_custom, null, false);
        final AlertDialog dialog = new AlertDialog.Builder(activity).setView(view).create();
        Button click = view.findViewById(R.id.click);
        TextView id = view.findViewById(R.id.id);
        id.setText("推荐活动: " + "优惠活动"+activity.eventNumber);
        click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onMessageDismiss(appMessage, AGConnectAppMessagingCallback.DismissType.CLICK);
                dialog.dismiss();
                activity.redirect(activity.eventNumber);
            }
        });
        dialog.show();
        dialog.getWindow().setLayout((getScreenWidth(activity) / 4 * 3), LinearLayout.LayoutParams.WRAP_CONTENT);
        callback.onMessageDisplay(appMessage);
    }


    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }
}
