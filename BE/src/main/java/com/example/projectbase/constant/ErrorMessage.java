package com.example.projectbase.constant;

public class ErrorMessage {

  public static final String ERR_EXCEPTION_GENERAL = "exception.general";
  public static final String UNAUTHORIZED = "exception.unauthorized";
  public static final String FORBIDDEN = "exception.forbidden";
  public static final String FORBIDDEN_UPDATE_DELETE = "exception.forbidden.update-delete";

  //error validation dto
  public static final String INVALID_SOME_THING_FIELD = "invalid.general";
  public static final String INVALID_FORMAT_SOME_THING_FIELD = "invalid.general.format";
  public static final String INVALID_SOME_THING_FIELD_IS_REQUIRED = "invalid.general.required";
  public static final String NOT_BLANK_FIELD = "invalid.general.not-blank";
  public static final String INVALID_FORMAT_PASSWORD = "invalid.password-format";
  public static final String INVALID_DATE = "invalid.date-format";
  public static final String INVALID_DATE_FEATURE = "invalid.date-future";
  public static final String INVALID_DATETIME = "invalid.datetime-format";

  public static class Auth {
    public static final String ERR_INCORRECT_USERNAME = "exception.auth.incorrect.username";
    public static final String ERR_INCORRECT_PASSWORD = "exception.auth.incorrect.password";
    public static final String ERR_ACCOUNT_NOT_ENABLED = "exception.auth.account.not.enabled";
    public static final String ERR_ACCOUNT_LOCKED = "exception.auth.account.locked";
    public static final String INVALID_REFRESH_TOKEN = "exception.auth.invalid.refresh.token";
    public static final String EXPIRED_REFRESH_TOKEN = "exception.auth.expired.refresh.token";
  }

  public static class User {
    public static final String ERR_NOT_FOUND_USERNAME = "exception.user.not.found.username";
    public static final String ERR_NOT_FOUND_ID = "exception.user.not.found.id";
    public static final String ERR_NOT_FOUND_EMAIL = "exception.user.not.found.email";
  }

  public static class Media {
    public static final String ERR_NOT_FOUND_MEDIA = "exception.media.not.found";
    public static final String ERR_INVALID_MEDIA_TYPE = "exception.media.upload.invalid.format";
    public static final String ERR_MAX_SIZE_UPLOAD_VIDEO = "exception.media.upload.maxsize_video";
    public static final String ERR_MAX_SIZE_UPLOAD_AUDIO = "exception.media.upload.maxsize_audio";
    public static final String ERR_MAX_SIZE_UPLOAD_IMAGE = "exception.media.upload.maxsize_image";
    public static final String ERR_MAX_SIZE_REQUEST_MEDIA = "exception.media.upload.maxsize_media";
  }

  public static class Post {
    public static final String ERR_NOT_FOUND_ID = "exception.post.not_found_by_id";
    public static final String ERR_INVALID_DATA = "exception.post.invalid_data";
    public static final String ERR_SAVE_FAILED = "exception.post.save_failed";
    public static final String ERR_DELETE_FAILED = "exception.post.delete_failed";
  }
}
