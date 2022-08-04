package com.huawei.agc.subscribedemo.db;

import com.huawei.agconnect.cloud.database.CloudDBZoneObject;
import com.huawei.agconnect.cloud.database.annotations.PrimaryKeys;

import java.io.Serializable;

/**
 * Article model
 *
 * @date 2022/08/02
 */
@PrimaryKeys({"id"})
public final class Article extends CloudDBZoneObject {

    private String id;

    private String title;

    private String content;

    private Integer authorId;

    public Article() {
        super(Article.class);
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setAuthorId(Integer authorId) {
        this.authorId = authorId;
    }

    public Integer getAuthorId() {
        return authorId;
    }

}