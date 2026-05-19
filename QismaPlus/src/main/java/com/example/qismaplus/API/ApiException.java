package com.example.qismaplus.API;

public class ApiException extends  RuntimeException{

    public ApiException(String message) {
        super(message);
    }
}