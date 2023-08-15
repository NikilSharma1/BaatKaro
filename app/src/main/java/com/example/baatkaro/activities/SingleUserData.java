package com.example.baatkaro.activities;

import java.io.Serializable;

public class SingleUserData implements Serializable {
    private String name;
    private String email;
    private String image;
    private String token;//needed for FCM
    private String id;

    public SingleUserData(String name,String email,String image,String token){
        this.email=email;
        this.name=name;
        this.image=image;
        this.token=token;
    }
    public SingleUserData(){
    }
    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getImage() {
        return image;
    }

    public String getToken() {
        return token;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
