package com.laioffer.jupiter.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
//return user name
//session 不需要通过body返回，在header里返回
//do not need json creator: backend 生成的java convert to json and return to front end
//opposite to login request body
//java -> json
public class LoginResponseBody {
    @JsonProperty("user_id")
    private final String userId;

    @JsonProperty("name")
    private final String name;

    public LoginResponseBody(String userId, String name) {
        this.userId = userId;
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }
}
