package com.sunny.diptish_website_crawler.util;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScheduledService {

    @Scheduled(fixedRate = 10000, initialDelay = 30000)
    private void printHappyMessage(){
        System.out.println("Diptish 1 number chutiya hai..!!  :) :) ");
    }
}
