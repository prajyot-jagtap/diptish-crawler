package com.sunny.diptish_website_crawler.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class DiptishClientConfiguration {

    @Bean
    public RestClient getRestClient(){
        return RestClient.create();
    }

}
