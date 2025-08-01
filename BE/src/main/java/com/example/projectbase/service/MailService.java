package com.example.projectbase.service;

public interface MailService {
    public boolean sendEmailWithObject(String receivedEmail, Object object, String subject);
}
