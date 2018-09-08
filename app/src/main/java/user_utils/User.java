package user_utils;

import java.io.Serializable;

public class User implements Serializable{

    private String mUserId;
    private String mUserName;
    private String mEmail;
    private String mUserPhotoUrl;

    public User(String mUserName, String mUserPhotoUrl, String mEmail) {
        this.mUserName = mUserName;
        this.mUserPhotoUrl = mUserPhotoUrl;
        this.mEmail = mEmail;
    }

    public User() {}

    public String getName() {
        return mUserName;
    }

    public void setName(String name) {
        mUserName = name;
    }

    public String getImage() {
        return mUserPhotoUrl;
    }

    public void setImage(String image) {
        mUserPhotoUrl = image;
    }

    public String getmEmail() {
        return mEmail;
    }

    public void setEmail(String mEmail) {
        this.mEmail = mEmail;
    }

    public String getUid() {
        return mUserId;
    }

    public void setUserId(String mUserId) {
        this.mUserId = mUserId;
    }
}
