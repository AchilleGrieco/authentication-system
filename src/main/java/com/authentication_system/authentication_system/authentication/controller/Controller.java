package com.authentication_system.authentication_system.authentication.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

    @GetMapping("/secured")
    public String secured() {
        return "Secured";
    }

}
