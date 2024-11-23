package com.bednarz.flashcardsapi.ai;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class WebConfig {

    @Bean
    RestTemplate openAIRestTemplate(){
        return new RestTemplate();
    }

}
