package com.example.projectbase.constant;

public class UrlConstant {

  public static class Auth {
    private static final String PRE_FIX = "/auth";

    public static final String LOGIN = PRE_FIX + "/login";
    public static final String LOGOUT = PRE_FIX + "/logout";

    private Auth() {
    }
  }

  public static class User {
    private static final String PRE_FIX = "/users";

    public static final String GET_USERS = PRE_FIX;
    public static final String GET_USER = PRE_FIX + "/{userId}";
    public static final String GET_CURRENT_USER = PRE_FIX + "/current";

    private User() {
    }
  }

  public static class Media {
    private static final String PRE_FIX = "/media";
    public static final String GET_MEDIAS = PRE_FIX;
    public static final String UPLOAD_MEDIA_VIDEO = PRE_FIX + "/upload/video";
    public static final String UPLOAD_MEDIA_IMAGE = PRE_FIX + "/upload/image";
    public static final String UPLOAD_MEDIA_AUDIO = PRE_FIX + "/upload/audio";
    public static final String UPLOAD_MULTI_MEDIA_IMAGE = PRE_FIX + "/upload/multi/image";
    public static final String DELETE_MEDIA = PRE_FIX + "/delete";
  }

}
