package com.saumik.TaskForge.common.exception;

public class AccessDeniedException extends ApiException {
    public AccessDeniedException(String message) {
        super(message);
    }
}