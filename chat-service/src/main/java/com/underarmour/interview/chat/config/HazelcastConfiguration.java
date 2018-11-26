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

import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizeConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Hazelcast Configuration for distributed caches
 *
 * @author david.moore
 */
@Configuration
public class HazelcastConfiguration {

    /**
     * Central hazelcast configuration, creating the hot storage cache
     *
     * @return Configuration
     */
    @Bean
    public Config hazelcastConfig() {
        return new Config().setInstanceName("hazelcast-instance")
                .addMapConfig(new MapConfig().setName("hotChatCacheCache")
                        .setMaxSizeConfig(new MaxSizeConfig(300, MaxSizeConfig.MaxSizePolicy.FREE_HEAP_SIZE))
                        .setEvictionPolicy(EvictionPolicy.LRU)
                        .setTimeToLiveSeconds(2000));
    }
}
