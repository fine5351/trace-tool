package com.example.api.controller;

import com.example.core.service.CoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final CoreService coreService;

    @Autowired
    public ApiController(CoreService coreService) {
        this.coreService = coreService;
    }

    @GetMapping("/info")
    public String getInfo() {
        return "API is running and using: " + coreService.getServiceInfo();
    }
}