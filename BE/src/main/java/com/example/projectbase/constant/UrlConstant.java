package com.example.projectbase.constant;

public class UrlConstant {

  public static class Auth {
    private static final String PRE_FIX = "/auth";
    public static final String REGISTER = PRE_FIX + "/register";
    public static final String LOGIN = PRE_FIX + "/login";
    public static final String LOGOUT = PRE_FIX + "/logout";
    public static final String OAUTH2_LOGIN = PRE_FIX + "/oauth2-login";
    public static final String REFRESH_TOKEN = PRE_FIX + "/refresh-token";
    public static final String ME = PRE_FIX + "/me";
    public static final String UPLOAD_PROFILE_PICTURE = PRE_FIX + "/upload-profile-picture";
    public static final String OAUTH2_INFO = PRE_FIX + "/oauth2-info";

    private Auth() {
    }
  }

  public static class User {
    private static final String PRE_FIX = "/user";

    public static final String GET_USERS = PRE_FIX;
    public static final String GET_USER = PRE_FIX + "/{userId}";
    public static final String GET_CURRENT_USER = PRE_FIX + "/current";
    private User() {
    }
  }

  public static class Post {
    private static final String PRE_FIX = "/posts";

    public static final String CREATE_POST   = PRE_FIX;
    public static final String GET_POSTS     = PRE_FIX;
    public static final String GET_POST      = PRE_FIX + "/{id}";
    public static final String UPDATE_POST   = PRE_FIX + "/{id}";
    public static final String DELETE_POST   = PRE_FIX + "/{id}";

    private Post() {}
  }
}