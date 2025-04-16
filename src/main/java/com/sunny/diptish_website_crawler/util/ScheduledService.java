package com.sunny.diptish_website_crawler.util;

import com.sunny.diptish_website_crawler.service.DiptishService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScheduledService {

    private static final Logger logger = LogManager.getLogger(ScheduledService.class);

    @Scheduled(fixedRate = 10000, initialDelay = 30000)
    private void printHappyMessage(){
        String message = "): ):  !!..iah ayituhc rebmun 1 hsitpiD";
        logger.info(new StringBuffer(message).reverse());
    }
}
