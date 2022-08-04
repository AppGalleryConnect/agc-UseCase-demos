package com.huawei.agc.subscribedemo.data.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Intent transfer data model
 *
 * @date 2022/08/02
 */
public class IntentMsg implements Parcelable {

    private String articleId;
    private String title;
    private String content;

    private String authorId;

    public IntentMsg() {
    }

    public String getArticleId() {
        return articleId;
    }

    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public IntentMsg(Parcel in) {
        articleId = in.readString();
        title = in.readString();
        content = in.readString();
        authorId = in.readString();
    }

    public static final Creator<IntentMsg> CREATOR = new Creator<IntentMsg>() {
        @Override
        public IntentMsg createFromParcel(Parcel in) {
            return new IntentMsg(in);
        }

        @Override
        public IntentMsg[] newArray(int size) {
            return new IntentMsg[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(articleId);
        parcel.writeString(title);
        parcel.writeString(content);
        parcel.writeString(authorId);
    }
}
