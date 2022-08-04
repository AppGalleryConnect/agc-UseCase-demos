package com.huawei.agc.subscribedemo.db;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.huawei.agconnect.cloud.database.AGConnectCloudDB;
import com.huawei.agconnect.cloud.database.CloudDBZone;
import com.huawei.agconnect.cloud.database.CloudDBZoneConfig;
import com.huawei.agconnect.cloud.database.CloudDBZoneObjectList;
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery;
import com.huawei.agconnect.cloud.database.CloudDBZoneSnapshot;
import com.huawei.agconnect.cloud.database.ListenerHandler;
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException;
import com.huawei.hmf.tasks.Task;

import java.util.ArrayList;

/**
 * Operation article database auxiliary class
 *
 * @date 2022/08/02
 */
public class ArticleDBAction {

    private static final String TAG = "--------ArticleDBAction";
    private static final String ZONE_NAME = "ArticleZone";
    private final AGConnectCloudDB mCloudDB;
    private CloudDBZone mCloudDBZone;
    private CloudDBZoneConfig mConfig;
    private UiCallBack callBack = UiCallBack.DEFAULT;

    public ArticleDBAction() {
        mCloudDB = AGConnectCloudDB.getInstance();
    }

    public void addCallBacks(UiCallBack callBack) {
        this.callBack = callBack;
    }

    public static void initAGConnectCloudDB(Context context) {
        AGConnectCloudDB.initialize(context);
    }

    public void createObjectType() {
        try {
            mCloudDB.createObjectType(ObjectTypeInfoHelper.getObjectTypeInfo());
        } catch (AGConnectCloudDBException e) {
            Log.e(TAG, "createObjectType: " + e.getMessage());
            callBack.onPhotoErrorMessage("createObjectType Failed: " + e.getMessage());
        }
    }

    /**
     *  Open CloudDBZone
     */
    public void openCloudDBZoneV2() {
        mConfig = new CloudDBZoneConfig(ZONE_NAME,
                CloudDBZoneConfig.CloudDBZoneSyncProperty.CLOUDDBZONE_CLOUD_CACHE,
                CloudDBZoneConfig.CloudDBZoneAccessProperty.CLOUDDBZONE_PUBLIC);
        mConfig.setPersistenceEnabled(true);
        Task<CloudDBZone> openDBZoneTask = mCloudDB.openCloudDBZone2(mConfig, true);
        openDBZoneTask.addOnSuccessListener(cloudDBZone -> {
            Log.i(TAG, "Open cloudDBZone success");
            mCloudDBZone = cloudDBZone;
            // Add subscription after opening cloudDBZone success
            addSubscription();
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Open cloudDBZone failed for " + e.getMessage());
            callBack.onPhotoErrorMessage("OpenCloudDBZone Failed: " + e.getMessage());
        });
    }

    /**
     * Call AGConnectCloudDB.closeCloudDBZone
     */
    public void closeCloudDBZone() {
        try {
//            mPhotoRegister.remove();
            mCloudDB.closeCloudDBZone(mCloudDBZone);
        } catch (AGConnectCloudDBException e) {
            Log.w(TAG, "closeCloudDBZone: " + e.getMessage());
        }
    }

    /**
     * Call AGConnectCloudDB.deleteCloudDBZone
     */
    public void deleteCloudDBZone() {
        try {
            mCloudDB.deleteCloudDBZone(mConfig.getCloudDBZoneName());
        } catch (AGConnectCloudDBException e) {
            Log.w(TAG, "deleteCloudDBZone: " + e.getMessage());
        }
    }

    /**
     * Add mSnapshotListener to monitor data changes from storage
     */
    public void addSubscription() {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
            return;
        }

        callBack.onSubscribe(null);
    }

    /**
     * Query all article in storage from cloud side with CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
     */
    public void queryAll() {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
            return;
        }
        Task<CloudDBZoneSnapshot<Article>> queryTask = mCloudDBZone.executeQuery(
                CloudDBZoneQuery.where(Article.class),
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
        queryTask.addOnSuccessListener(snapshot -> {
            Log.w(TAG, "query all articles success");
            processQueryResult(snapshot);
        }).addOnFailureListener(e -> {
            callBack.onPhotoErrorMessage("queryAll Error: " + e.getMessage());
        });
    }

    /**
     * Query article by id in storage from cloud side with CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
     *
     * @param articleId
     * @param authorId
     */
    public void queryArticleById(String articleId, Integer authorId) {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
            return;
        }

        CloudDBZoneQuery<Article> query = CloudDBZoneQuery.where(Article.class)
                .equalTo("id", articleId).equalTo("authorId", authorId);

        Task<CloudDBZoneSnapshot<Article>> queryTask = mCloudDBZone.executeQuery(query,
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
        queryTask.addOnSuccessListener(snapshot -> {
            Log.w(TAG, "query all articles success");
            processQueryResult(snapshot);
        }).addOnFailureListener(e -> {
            callBack.onPhotoErrorMessage("queryAll Error: " + e.getMessage());
        });
    }

    private void processQueryResult(CloudDBZoneSnapshot<Article> snapshot) {
        CloudDBZoneObjectList<Article> snapshotObjects = snapshot.getSnapshotObjects();
        ArrayList<Article> articles = new ArrayList<>();
        try {
            while (snapshotObjects.hasNext()) {
                Article article = snapshotObjects.next();
                articles.add(article);
            }
        } catch (AGConnectCloudDBException e) {
            Log.w(TAG, "processQueryResult: " + e.getMessage());
        } finally {
            snapshot.release();
        }
        callBack.onQuery(articles);
    }

    /**
     * Upsert article
     *
     * @param article added or modified from local
     */
    public void upsertArticle(Article article) {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
            return;
        }
        Task<Integer> upsertTask = mCloudDBZone.executeUpsert(article);
        upsertTask.addOnSuccessListener(cloudDBZoneResult -> {
            ArrayList<Article> articles = new ArrayList<>();
            articles.add(article);

            Log.i(TAG, "Upsert " + cloudDBZoneResult + " records");
            callBack.onUpdate(articles);
        }).addOnFailureListener(e -> callBack.onPhotoErrorMessage("upsert Error: " + e.getMessage()));
    }

    public interface UiCallBack {
        UiCallBack DEFAULT = new UiCallBack() {
            @Override
            public void onQuery(ArrayList<Article> articles) {
                Log.i(TAG, "Using default onQuery");
            }

            @Override
            public void onUpdate(ArrayList<Article> articles) {
                Log.i(TAG, "Using default onUpdate");
            }

            @Override
            public void onSubscribe(ArrayList<Article> articles) {
                Log.i(TAG, "Using default onSubscribe");
            }

            @Override
            public void onPhotoErrorMessage(String errorMessage) {
                Log.i(TAG, "default errorMessage");
            }
        };

        void onQuery(ArrayList<Article> articles);

        void onUpdate(ArrayList<Article> articles);

        void onSubscribe(ArrayList<Article> articles);

        void onPhotoErrorMessage(String errorMessage);
    }

    public Article buildArticle(String id, String title, String content, String authorId) {
        Article article = new Article();
        article.setId(id);
        article.setTitle(title);
        article.setContent(content);
        article.setAuthorId(Integer.parseInt(authorId));
        return article;
    }

}
