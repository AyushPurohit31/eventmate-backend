package com.eventmate.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/check-access")
    public String checkAccess() {
        return "Access token is valid. Controller is accessible.";
    }
}
