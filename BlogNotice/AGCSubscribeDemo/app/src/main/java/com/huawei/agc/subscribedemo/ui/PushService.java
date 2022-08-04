package com.huawei.agc.subscribedemo.ui;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.huawei.agc.subscribedemo.R;
import com.huawei.hms.push.HmsMessageService;
import com.huawei.hms.push.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

public class PushService extends HmsMessageService {

    private final String TAG = "------------PushService";

    @Override
    public void onNewToken(String token, Bundle bundle) {
        // get token
        Log.i(TAG, "have received refresh token " + token);

        // Judge whether the token is empty
        if (!TextUtils.isEmpty(token)) {
            refreshedTokenToServer(token);
        }
    }

    private void refreshedTokenToServer(String token) {
        Log.i(TAG, "sending token to server. token:" + token);
    }

    @Override
    public void onMessageReceived(RemoteMessage message) {
        Log.i(TAG, "onMessageReceived is called");

        // Judge whether the message is empty
        if (message == null) {
            Log.e(TAG, "Received message entity is null!");
            return;
        }

        // Get message content
        Log.i(TAG, "get Data: " + message.getData()
                + "\n getFrom: " + message.getFrom()
                + "\n getTo: " + message.getTo()
                + "\n getMessageId: " + message.getMessageId()
                + "\n getSentTime: " + message.getSentTime()
                + "\n getDataMap: " + message.getDataOfMap()
                + "\n getMessageType: " + message.getMessageType()
                + "\n getTtl: " + message.getTtl()
                + "\n getToken: " + message.getToken());

        Boolean judgeWhetherIn10s = false;
        // If the message is not processed within 10 seconds, you need to create a new task to process it yourself
        if (judgeWhetherIn10s) {
            startWorkManagerJob(message);
        } else {
            // Process messages in 10 seconds
            processWithin10s(message);
        }
    }

    private void startWorkManagerJob(RemoteMessage message) {
        Log.d(TAG, "Start new job processing.");
    }

    private void processWithin10s(RemoteMessage message) {
        Log.d(TAG, "Processing now.");

        if (!TextUtils.isEmpty(message.getData())) {
            try {
                JSONObject object = new JSONObject(message.getData());
                String title = object.optString("title") + "";
                String content = object.optString("content") + "";
                String articleId = object.optString("articleId") + "";
                String authorId = object.optString("authorId") + "";

                NotificationUtils.sendNotify(this, title, content, articleId, authorId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
