package com.huawei.agc.subscribedemo.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;
import com.huawei.agc.subscribedemo.MainApplication;
import com.huawei.agc.subscribedemo.R;
import com.huawei.agc.subscribedemo.data.model.IntentMsg;
import com.huawei.agc.subscribedemo.databinding.ActivityMainBinding;
import com.huawei.agc.subscribedemo.db.Article;
import com.huawei.agc.subscribedemo.db.ArticleDBAction;
import com.huawei.agc.subscribedemo.ui.login.LoginActivity;
import com.huawei.agconnect.function.AGCFunctionException;
import com.huawei.agconnect.function.AGConnectFunction;
import com.huawei.agconnect.function.FunctionResult;
import com.huawei.hmf.tasks.OnCompleteListener;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * MainActivity, Show article list page
 *
 * @date 2022/08/02
 * <p>
 * Copyright (c) HuaWei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */
public class MainActivity extends AppCompatActivity implements ArticleDBAction.UiCallBack {

    private ActivityMainBinding binding;
    private Context context;
    private RvAdapter adapter;
    private ArrayList<Article> list = new ArrayList<>();

    private Handler mHandler = null;
    private ArticleDBAction articleDBAction;

    //Used to simulate inserting article data fields
    private String articleId = "1", authorId = "1", title = "要像对待生命一样关爱海洋";
    private String content = "6月8日，是第十四个“世界海洋日”暨第十五个“全国海洋宣传日”。关爱海洋，人海和谐，落实海洋可持续发展目标，才能为子孙后代留下一片碧海蓝天。";

    private AGConnectFunction function;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = this;
        getIntentData(getIntent());

        //Initialize cloud function
        function = AGConnectFunction.getInstance();

        mHandler = new Handler(Looper.getMainLooper());

        articleDBAction = new ArticleDBAction();
        articleDBAction.addCallBacks(this);

        mHandler.post(() -> {
            articleDBAction.openCloudDBZoneV2();
        });

        binding.btnAdd.setOnClickListener(view -> mHandler.post(() -> {
            //Simulate adding and inserting article data in cloud database
            articleDBAction.upsertArticle(articleDBAction.buildArticle(articleId, title, content, authorId));
        }));

        binding.refresh.setOnRefreshListener(() -> articleDBAction.queryAll());


        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        binding.recycler.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        adapter = new RvAdapter(this, list, binding.recycler);
        binding.recycler.setAdapter(adapter);
        adapter.setOnItemClickListener((view, position, data) -> {
            IntentMsg msg = new IntentMsg();
            msg.setArticleId(data.getId());
            msg.setAuthorId(data.getAuthorId() + "");

            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("IntentMsg", msg);
            startActivity(intent);
        });

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        getIntentData(intent);
    }

    private void getIntentData(Intent intent) {
        if (intent != null) {
            // Get the value in data
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                for (String key : bundle.keySet()) {
                    String content = bundle.getString(key);
                    Log.i("--------", "receive data from push, key = " + key + ", content = " + content);

                    if (key.equals("uid")) {
                        MainApplication.uid = content;
                    } else if (key.equals("phone")) {
                        MainApplication.phone = content;
                    } else {

                    }
                }
            }
        } else {
            Log.i("--------", "intent is null");
        }
    }

    /**
     *  Trigger the cloud function, query the list of users who have followed the author, and send a push
     */
    private void queryUser() {
        HashMap<String, String> map = new HashMap();
        map.put("type", "2");
        map.put("authorId", authorId);
        map.put("phone", MainApplication.phone);

        function.wrap("insert-article-$latest").call(map)
                .addOnCompleteListener(new OnCompleteListener<FunctionResult>() {
                    @Override
                    public void onComplete(Task<FunctionResult> task) {
                        if (task.isSuccessful()) {
                            String value = task.getResult().getValue();
                            Log.i("--------", "value ：" + value);

                            try {
                                JSONObject object = new JSONObject(value);
                                String message = (String) object.get("message");
                                //Cloud function returned successfully
                                if (message.equals("ok")) {
                                    boolean isSubscription = (boolean) object.get("isSubscription");
                                    //The currently logged in user is a subscribed user. Send SMS and push to remind the user to check
                                    if (isSubscription) {
                                        //Send push notification
                                        NotificationUtils.sendNotify(context, getString(R.string.update_remind), "您关注的博主更新新的内容啦，点击查看", articleId, authorId);

                                        //SMS word https://developer.huawei.com/consumer/cn/doc/development/AppGallery-connect-Guides/agc-sms-sendmessage-0000001085858011#section6675181013235
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        } else {
                            Exception e = task.getException();
                            if (e instanceof AGCFunctionException) {
                                AGCFunctionException functionException = (AGCFunctionException) e;
                                int errCode = functionException.getCode();
                                String message = functionException.getMessage();

                                Log.i("--------", "errCode ：" + errCode + "     message ：" + message);
                            }

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.i("--------", "onFailure ：" + e.getMessage());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Exit close cloud database link
        articleDBAction.closeCloudDBZone();
    }

    @Override
    public void onQuery(ArrayList<Article> articles) {
        Log.i("--------", "onQuery: " + new Gson().toJson(articles));
        list.clear();
        list.addAll(articles);

        //Update data
        adapter.update();

        binding.refresh.setRefreshing(false);
    }

    //TODO
    @Override
    public void onUpdate(ArrayList<Article> articles) {
        Log.i("--------", "onUpdate: " + new Gson().toJson(articles));

        //Trigger the cloud function after adding an article
        queryUser();
    }

    @Override
    public void onSubscribe(ArrayList<Article> articles) {
        Log.i("--------", "onSubscribe: ");

        //Query all articles after opening the cloud database
        articleDBAction.queryAll();
    }

    @Override
    public void onPhotoErrorMessage(String errorMessage) {
        Log.i("--------", "onPhotoErrorMessage: " + errorMessage);

        binding.refresh.setRefreshing(false);
    }

}