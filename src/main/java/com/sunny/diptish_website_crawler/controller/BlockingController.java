package com.sunny.diptish_website_crawler.controller;

import com.sunny.diptish_website_crawler.model.FileRequest;
import com.sunny.diptish_website_crawler.service.DiptishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class BlockingController {

    @Autowired
    DiptishService diptishService;

    @GetMapping("/profile-link-file")
    public String getHomeMessage(){
        return diptishService.getProfileLinksFile();
    }

    @PostMapping("/email-address-file")
    public String getEmailAddressFile(@RequestBody FileRequest fileRequest) throws IOException {
        return diptishService.createProcessedFile(fileRequest);
    }
}
