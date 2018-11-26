/*
 * Chat Service
 *
 * Copyright (c) 2018-2019 Under Armour [https://www.underarmour.com/]
 *
 * The  information  contained  herein  is  the  confidential  and  proprietary
 * information of  Under Armour.  This information is protected,  among others,
 * by the patent,  copyright,  trademark,  and trade secret laws of  the United
 * States and its several states.  Any use,  copying, or reverse engineering is
 * strictly prohibited. By  viewing or receiving this information, you  consent
 * to the foregoing.
 */
package com.underarmour.interview.chat.config;

//import com.monitorjbl.json.JsonViewSupportFactoryBean;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

/**
 * Service level configurations. Enable frameworks, expose beans
 *
 * @author david.moore
 */
@EnableScheduling
@EnableAsync
@EnableZuulProxy
@EnableCaching
@Configuration
public class ChatConfig {

    /**
     * ModelMapper bean used to convert HotMessage < - > ColdMessage
     *
     * @return ModelMapper object
     */
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
