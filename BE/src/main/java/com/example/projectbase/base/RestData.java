package com.example.projectbase.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RestData<T> {

  private RestStatus status;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private T message;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private T data;
  private LocalDateTime timestamp;

  public RestData(T data) {
    this.status = RestStatus.SUCCESS;
    this.data = data;
    this.timestamp = LocalDateTime.now();
  }

  public static RestData<?> successWithMessage(Object message) {
    return new RestData<>(RestStatus.SUCCESS,message, null, LocalDateTime.now());
  }

  public static RestData<?> error(Object message) {
    return new RestData<>(RestStatus.ERROR, message, null, LocalDateTime.now());
  }

}
