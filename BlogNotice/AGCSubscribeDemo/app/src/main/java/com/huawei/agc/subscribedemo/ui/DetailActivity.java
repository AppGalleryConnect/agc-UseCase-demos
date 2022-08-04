package com.huawei.agc.subscribedemo.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.huawei.agc.subscribedemo.MainApplication;
import com.huawei.agc.subscribedemo.R;
import com.huawei.agc.subscribedemo.data.model.IntentMsg;
import com.huawei.agc.subscribedemo.databinding.ActivityDetailBinding;
import com.huawei.agc.subscribedemo.db.Article;
import com.huawei.agc.subscribedemo.db.ArticleDBAction;
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
 * DetailActivity, Look Article Details
 *
 * @date 2022/08/02
 * <p>
 * Copyright (c) HuaWei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */
public class DetailActivity extends AppCompatActivity implements ArticleDBAction.UiCallBack {

    private ActivityDetailBinding binding;
    private Context context;

    private IntentMsg msg;

    private Handler mHandler = null;

    private AGConnectFunction function;
    private ArticleDBAction articleDBAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = this;

        msg = getIntent().getParcelableExtra("IntentMsg");

        if (msg == null) {
            Log.i("--------", "msg error");
            return;
        }

        //Initialize cloud function
        function = AGConnectFunction.getInstance();

        mHandler = new Handler(Looper.getMainLooper());

        articleDBAction = new ArticleDBAction();
        articleDBAction.addCallBacks(this);

        mHandler.post(() -> {
            articleDBAction.openCloudDBZoneV2();

            queryUser(msg.getAuthorId());
        });


        binding.btnGo.setOnClickListener(view -> {
            if (binding.btnGo.getText().toString().equals(getString(R.string.follow))) {
                handle();
            }
        });


    }

    /**
     *  Trigger the cloud function and insert the user's follow information into the cloud database
     */
    private void handle() {
        HashMap<String, String> map = new HashMap();
        map.put("type", "1");
        map.put("articleId", msg.getArticleId());
        map.put("authorId", msg.getAuthorId());
        map.put("phone", MainApplication.phone);
        map.put("uid", MainApplication.uid);

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
                                    binding.btnGo.setText(R.string.followed);
                                    Toast.makeText(context, getString(R.string.follow_ok), Toast.LENGTH_LONG).show();
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


    /**
     * Trigger the cloud function to query whether to follow the author
     *
     * @param authorId
     */
    private void queryUser(String authorId) {
        HashMap<String, String> map = new HashMap();
        map.put("type", "2");
        map.put("authorId", authorId);
        map.put("phone", MainApplication.phone);

        function.wrap("insert-article-$latest").call(map)//insert-article
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
                                    binding.btnGo.setText(isSubscription ? getString(R.string.followed) : getString(R.string.follow));
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
    public void onQuery(ArrayList<Article> articles) {
        Log.i("--------", "onQuery: " + new Gson().toJson(articles));

        if (articles != null && !articles.isEmpty()) {
            Article article = articles.get(0);

            binding.nick.setText("nick " + article.getId());
//        binding.btnGo.setText(msg.getAuthorId() + "");
            binding.title.setText(article.getTitle() + "");
            binding.content.setText(article.getContent() + "");
        }

    }

    @Override
    public void onUpdate(ArrayList<Article> articles) {
        Log.i("--------", "onUpdate: " + new Gson().toJson(articles));

    }

    @Override
    public void onSubscribe(ArrayList<Article> articles) {
        Log.i("--------", "onSubscribe: ");

        //查询指定文章
        articleDBAction.queryArticleById(msg.getArticleId(), Integer.valueOf(msg.getAuthorId()));
    }

    @Override
    public void onPhotoErrorMessage(String errorMessage) {
        Log.i("--------", "onPhotoErrorMessage: " + errorMessage);

    }

}