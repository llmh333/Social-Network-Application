package com.example.projectbase.service;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface UserSessionService {

    public void updateLastActivity(String username) throws JsonProcessingException;
}
