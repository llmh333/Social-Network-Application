package com.example.projectbase.constant;

public class UrlConstant {

  public static class Auth {
    private static final String PRE_FIX = "/auth";

    public static final String LOGIN = PRE_FIX + "/login";
    public static final String LOGOUT = PRE_FIX + "/logout";
    public static final String SIGNUP = PRE_FIX +"/signup";
    private Auth() {
    }
  }

  public static class User {
    private static final String PRE_FIX = "/user";

    public static final String GET_USERS = PRE_FIX;
    public static final String GET_USER = PRE_FIX + "/{userId}";
    public static final String GET_CURRENT_USER = PRE_FIX + "/current";

    public static final String CREATE_USER = PRE_FIX + "/create";
    public static final String GET_ALL_USERS = PRE_FIX + "/all";
    public static final String UPDATE_USERNAME = PRE_FIX + "/update/{id}";
    public static final String DELETE_USER= PRE_FIX + "/delete/{id}";
    private User() {
    }
  }

}
