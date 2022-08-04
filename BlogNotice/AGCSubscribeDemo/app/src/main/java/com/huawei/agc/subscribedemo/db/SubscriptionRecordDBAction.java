package com.huawei.agc.subscribedemo.db;

import android.content.Context;
import android.util.Log;

import com.huawei.agconnect.cloud.database.AGConnectCloudDB;
import com.huawei.agconnect.cloud.database.CloudDBZone;
import com.huawei.agconnect.cloud.database.CloudDBZoneConfig;
import com.huawei.agconnect.cloud.database.CloudDBZoneObjectList;
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery;
import com.huawei.agconnect.cloud.database.CloudDBZoneSnapshot;
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException;
import com.huawei.hmf.tasks.Task;

import java.util.ArrayList;

/**
 *
 * Operation subscriptionRecord database auxiliary class
 *
 * @date 2022/08/02
 */
public class SubscriptionRecordDBAction {
    private static final String TAG = "--------RecordDBAction";
    private static final String ZONE_NAME = "ArticleZone";
    private final AGConnectCloudDB mCloudDB;
    private CloudDBZone mCloudDBZone;
    private CloudDBZoneConfig mConfig;
    private UiCallBack callBack = UiCallBack.DEFAULT;

    public SubscriptionRecordDBAction() {
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

    public void queryAll() {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
            return;
        }
        Task<CloudDBZoneSnapshot<SubscriptionRecord>> queryTask = mCloudDBZone.executeQuery(
                CloudDBZoneQuery.where(SubscriptionRecord.class),
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
        queryTask.addOnSuccessListener(snapshot -> {
            Log.w(TAG, "query all articles success");
            processQueryResult(snapshot);
        }).addOnFailureListener(e -> {
            callBack.onPhotoErrorMessage("queryAll Error: " + e.getMessage());
        });
    }

    /**
     * Query photos with condition
     *
     * @param query query condition
     */
    public void query(CloudDBZoneQuery<SubscriptionRecord> query) {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
            return;
        }
        Task<CloudDBZoneSnapshot<SubscriptionRecord>> queryTask = mCloudDBZone.executeQuery(
                query, CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
        queryTask.addOnSuccessListener(snapshot -> {
            Log.i(TAG, "query article Success");
            processQueryResult(snapshot);
        }).addOnFailureListener(e -> {
            Log.e(TAG, "query Failed: " + e.getMessage());
            callBack.onPhotoErrorMessage("queryUser Error: " + e.getMessage());
        });
    }

    private void processQueryResult(CloudDBZoneSnapshot<SubscriptionRecord> snapshot) {
        CloudDBZoneObjectList<SubscriptionRecord> snapshotObjects = snapshot.getSnapshotObjects();
        ArrayList<SubscriptionRecord> articles = new ArrayList<>();
        try {
            while (snapshotObjects.hasNext()) {
                SubscriptionRecord article = snapshotObjects.next();
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
     * Upsert photoTable
     *
     * @param record photoInfo added or modified from local
     */
    public void upsert(SubscriptionRecord record) {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
            return;
        }
        Task<Integer> upsertTask = mCloudDBZone.executeUpsert(record);
        upsertTask.addOnSuccessListener(cloudDBZoneResult -> {
            ArrayList<SubscriptionRecord> articles = new ArrayList<>();
            articles.add(record);

            Log.i(TAG, "Upsert " + cloudDBZoneResult + " records");
            callBack.onUpdate(articles);
        }).addOnFailureListener(e -> callBack.onPhotoErrorMessage("upsert Error: " + e.getMessage()));
    }

    public interface UiCallBack {
        UiCallBack DEFAULT = new UiCallBack() {
            @Override
            public void onQuery(ArrayList<SubscriptionRecord> articles) {
                Log.i(TAG, "Using default onQuery");
            }

            @Override
            public void onUpdate(ArrayList<SubscriptionRecord> articles) {
                Log.i(TAG, "Using default onUpdate");
            }

            @Override
            public void onSubscribe(ArrayList<SubscriptionRecord> articles) {
                Log.i(TAG, "Using default onSubscribe");
            }

            @Override
            public void onPhotoErrorMessage(String errorMessage) {
                Log.i(TAG, "default errorMessage");
            }
        };

        void onQuery(ArrayList<SubscriptionRecord> articles);

        void onUpdate(ArrayList<SubscriptionRecord> articles);

        void onSubscribe(ArrayList<SubscriptionRecord> articles);

        void onPhotoErrorMessage(String errorMessage);
    }

    public SubscriptionRecord build(int id, int authorId, String phone, String uid) {
        SubscriptionRecord record = new SubscriptionRecord();
        record.setId(id);
        record.setAuthorId(authorId);
        record.setPhone(phone);
        record.setUid(uid);
        return record;
    }
}
