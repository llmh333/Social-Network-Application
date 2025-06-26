package com.example.projectbase.constant;

public class UrlConstant {

  public static class Auth {
    private static final String PRE_FIX = "/auth";
    public static final String REGISTER = PRE_FIX + "/register";
    public static final String LOGIN = PRE_FIX + "/login";
    public static final String LOGIN_GOOGLE = PRE_FIX + "/login/google";
    public static final String LOGIN_FACEBOOK = PRE_FIX + "/login/facebook";
    public static final String LOGOUT = PRE_FIX + "/logout";
    public static final String SIGNUP = PRE_FIX +"/signup";
    public static final String REFRESH_TOKEN = PRE_FIX + "/refresh-token";
    public static final String ME = PRE_FIX + "/me";
    public static final String UPLOAD_PROFILE_PICTURE = PRE_FIX + "/upload-profile-picture";
    public static final String OAUTH2_INFO = PRE_FIX + "/oauth2-info";

    private Auth() {
    }
  }

  public static class OAUTH2_INFO {
    public static final String PRE_FIX = "/oauth2";
    public static final String REDIRECT_OAUTH2_GOOGLE =  PRE_FIX + "/authorization/google";
    public static final String REDIRECT_OAUTH2_FACEBOOK = PRE_FIX + "/authorization/facebook";
    public static final String OAUTH2_TOKEN_INFO = PRE_FIX + "/info/token";
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

  public static class Media {
    private static final String PRE_FIX = "/media";
    public static final String GET_MEDIA_BY_RESOURCE_TYPE = PRE_FIX + "/";
    public static final String GET_MEDIA_BY_PUBLIC_ID = PRE_FIX + "/{publicId}";
    public static final String GET_AUDIO_BY_TITLE_OR_CATEGORY_OR_SINGER = PRE_FIX + "/audio";
    public static final String UPLOAD_MEDIA_VIDEO = PRE_FIX + "/upload/video";
    public static final String UPLOAD_MEDIA_IMAGE = PRE_FIX + "/upload/image";
    public static final String UPLOAD_MEDIA_AUDIO = PRE_FIX + "/upload/audio";
    public static final String UPLOAD_MULTI_MEDIA_IMAGE = PRE_FIX + "/upload/multi/image";
    public static final String DELETE_MEDIA = PRE_FIX + "/delete";
  }

  public static class Post {
    private static final String PRE_FIX = "/posts";
    public static final String CREATE_POST_IMAGE   = PRE_FIX + "/image";
    public static final String CREATE_POST_VIDEO  = PRE_FIX + "/video";
    public static final String CREATE_POST_AUDIO   = PRE_FIX + "/audio";
    public static final String CREATE_POST_MULTI_IMAGES   = PRE_FIX + "/images";
    public static final String GET_ALL_POST_BY_TITLE    = PRE_FIX + "/search";
    public static final String GET_POST      = PRE_FIX + "/{id}";
    public static final String UPDATE_POST   = PRE_FIX + "/{id}";
    public static final String DELETE_POST   = PRE_FIX + "/{id}";
    public static final String REACTION_FOR_POST = PRE_FIX + "/{postId}/reaction";
    public static final String CANCEL_REACTION_OF_POST = PRE_FIX + "/{postId}/reaction";
    public static final String GET_REACTIONS = PRE_FIX + "/{postId}/reactions";

    private Post() {}
  }

  public static class Reaction {

    private Reaction() {}
  }

  public static class Follow {

    public static final String PRE_FIX = "/follows";
    public static final String EXECUTING_FOLLOW = PRE_FIX + "/following" ;
    public static final String UNFOLLOW = PRE_FIX + "/unfollowing";
    public static final String GET_FOLLOWINGS = PRE_FIX + "/me/followings";
    public static final String GET_FOLLOWERS = PRE_FIX + "/me/followers";

    private Follow() {}
  }
}
