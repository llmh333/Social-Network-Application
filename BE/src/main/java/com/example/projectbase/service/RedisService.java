package com.example.projectbase.service;

public interface RedisService {

    public void save(String key, String value);


    public String get(String key);

    public void delete(String key);
}
