package com.example.xuankuapp.model;

import android.content.Context;
import android.util.Log;

import com.huawei.agconnect.cloud.database.AGConnectCloudDB;
import com.huawei.agconnect.cloud.database.CloudDBZone;
import com.huawei.agconnect.cloud.database.CloudDBZoneConfig;
import com.huawei.agconnect.cloud.database.CloudDBZoneObjectList;
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery;
import com.huawei.agconnect.cloud.database.CloudDBZoneSnapshot;
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;

import java.util.ArrayList;
import java.util.Date;

/**
 * EventActivity for processing information about users who participate in activities.
 *
 * @since 2022-08-02
 * <p>
 * Copyright (c) HuaWei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */
public class UserInfoAction {

    private static final String TAG = "--------UserInfoAction";
    private CloudDBZoneConfig mConfig;
    private static final String ZONE_NAME = "XuankuAPP";
    private final AGConnectCloudDB mCloudDB;
    private CloudDBZone mCloudDBZone;
    private UiCallBack callBack = UiCallBack.DEFAULT;


    public UserInfoAction() {
        mCloudDB = AGConnectCloudDB.getInstance();
    }

    public void addCallBacks(UiCallBack callBack) {
        this.callBack = callBack;
    }

    public static void initAGConnectCloudDB(Context context) {
        AGConnectCloudDB.initialize(context);
    }

    /**
     * Creating an Object Type Method.
     */
    public void createObjectType() {
        try {
            mCloudDB.createObjectType(ObjectTypeInfoHelper.getObjectTypeInfo());
        } catch (AGConnectCloudDBException e) {
            Log.e(TAG, "createObjectType: " + e.getMessage());
        }
    }

    /**
     * Open the cloud database and add the subscription.
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
        });
    }

    /**
     * Add Subscription Method.
     */
    public void addSubscription() {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
            return;
        }
        callBack.onSubscribe(null);
    }

    /**
     * Inserting User Information to the Cloud Database.
     */
    public void upsertUserInfo(UserInfo userInfo) {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
            return;
        }
        Task<Integer> upsertTask = mCloudDBZone.executeUpsert(userInfo);
        upsertTask.addOnSuccessListener(cloudDBZoneResult -> {
            ArrayList<UserInfo> userInfos = new ArrayList<>();
            userInfos.add(userInfo);
            Log.i(TAG, "Upsert " + cloudDBZoneResult + " records");
        }).addOnFailureListener(e -> {
            Log.e(TAG, "upsert Error: " + e.getMessage());
        });
    }

    /**
     * Method of Constructing User Information.
     */
    public UserInfo buildUserInfo(String uid, String phoneNumber, Date time,
                                  Boolean event1, Boolean event2) {
        UserInfo userInfo = new UserInfo();
        userInfo.setUid(uid);
        userInfo.setPhoneNumber(phoneNumber);
        userInfo.setTime(time);
        userInfo.setEvent1(event1);
        userInfo.setEvent2(event2);
        return userInfo;
    }

    /**
     * The snapshot is successfully queried and the process query result is successfully queried.
     */
    public void queryAll() {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
            return;
        }
        Task<CloudDBZoneSnapshot<UserInfo>> queryTask = mCloudDBZone.executeQuery(
                CloudDBZoneQuery.where(UserInfo.class),
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
        queryTask.addOnSuccessListener(snapshot -> {
            Log.w(TAG, "queryAll userInfos success");
            processQueryResult(snapshot);
        }).addOnFailureListener(e -> {
            Log.e(TAG,"queryAll Error: " + e.getMessage());
        });
    }

    /**
     * User information screening method.
     */
    public void queryUserInfo(CloudDBZoneQuery<UserInfo> query) {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
            return;
        }
        Task<CloudDBZoneSnapshot<UserInfo>> queryTask = mCloudDBZone.executeQuery(query,
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
        queryTask.addOnSuccessListener(new OnSuccessListener<CloudDBZoneSnapshot<UserInfo>>() {
            @Override
            public void onSuccess(CloudDBZoneSnapshot<UserInfo> snapshot) {
                Log.w(TAG, "Query list from cloud userInfos success");
                processQueryResult(snapshot);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Query list from cloud failed Error: " + e.getMessage());
            }
        });
    }

    /**
     * Process query result method, which is used to output the user information list.
     */
    private void processQueryResult(CloudDBZoneSnapshot<UserInfo> snapshot) {
        CloudDBZoneObjectList<UserInfo> userInfoCursor = snapshot.getSnapshotObjects();
        ArrayList<UserInfo> userInfoList = new ArrayList<>();
        try {
            while (userInfoCursor.hasNext()) {
                UserInfo userInfo = userInfoCursor.next();
                userInfoList.add(userInfo);
            }
        } catch (AGConnectCloudDBException e) {
            Log.w(TAG, "processQueryResult: " + e.getMessage());
        }finally {
            snapshot.release();
        }
        callBack.onQuery(userInfoList);
    }

    public interface UiCallBack {
        UiCallBack DEFAULT = new UiCallBack() {
            @Override
            public void onQuery(ArrayList<UserInfo> userInfos) {
                Log.i(TAG, "Using default onQuery");
            }

            @Override
            public void onSubscribe(ArrayList<UserInfo> userInfos) {
                Log.i(TAG, "Using default onSubscribe");
            }
        };
        void onQuery(ArrayList<UserInfo> userInfos);

        void onSubscribe(ArrayList<UserInfo> userInfos);
    }
}


