package com.saumik.TaskForge.common.email;

public interface EmailService {

    void sendEmail(
            String to,
            String subject,
            String body
    );
}