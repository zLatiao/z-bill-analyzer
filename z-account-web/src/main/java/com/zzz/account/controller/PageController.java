package com.zzz.account.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
    @GetMapping("/index")
    public String index() {
        return "index.html";
    }

    @GetMapping("/visualization")
    public String visualization() {
        return "visualization.html";
    }

    @GetMapping("/index-old")
    public String indexOld() {
        return "index-old.html";
    }

    @GetMapping("/upload")
    public String uploadPage() {
        return "upload.html";
    }
}
