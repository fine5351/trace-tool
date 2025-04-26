package com.example.core.service;

import org.springframework.stereotype.Service;

@Service
public class CoreService {

    public String getServiceInfo() {
        return "Core Service is running";
    }
}