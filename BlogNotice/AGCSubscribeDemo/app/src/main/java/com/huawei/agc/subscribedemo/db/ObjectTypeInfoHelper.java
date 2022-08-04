package com.huawei.agc.subscribedemo.db;

import com.huawei.agconnect.cloud.database.CloudDBZoneObject;
import com.huawei.agconnect.cloud.database.ObjectTypeInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Cloud database ObjectTypeInfoHelper
 *
 * @date 2022/08/02
 */
public class ObjectTypeInfoHelper {
    private static final int FORMAT_VERSION = 2;
    private static final int OBJECT_TYPE_VERSION = 16;

    public static ObjectTypeInfo getObjectTypeInfo() {
        ObjectTypeInfo objectTypeInfo = new ObjectTypeInfo();
        objectTypeInfo.setFormatVersion(FORMAT_VERSION);
        objectTypeInfo.setObjectTypeVersion(OBJECT_TYPE_VERSION);
        List<Class<? extends CloudDBZoneObject>> objectTypeList = new ArrayList<>();
        Collections.addAll(objectTypeList, Article.class, SubscriptionRecord.class);
        objectTypeInfo.setObjectTypes(objectTypeList);
        return objectTypeInfo;
    }
}
