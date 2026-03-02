package com.nhom3.ct240.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello from CT240 Backend - Nhóm 3! Server đang chạy tốt.";
    }
}