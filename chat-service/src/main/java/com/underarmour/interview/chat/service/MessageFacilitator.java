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
package com.underarmour.interview.chat.service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;
import com.underarmour.interview.chat.entity.ColdMessage;
import com.underarmour.interview.chat.entity.HotMessage;
import com.underarmour.interview.chat.repository.ColdChatRepository;
import com.underarmour.interview.chat.repository.HotChatRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class MessageFacilitator {
    private static Logger log = LoggerFactory.getLogger(MessageFacilitator.class);
    private IMap<Integer, HotMessage> hotChatCache;
    private HotChatRepository hotChatRepository;
    private ColdChatRepository coldChatRepository;
    private ModelMapper modelMapper;

    @SuppressWarnings("FieldCanBeLocal") // Minimize GC on the tick method
    private Predicate expirationPredicate;
    @SuppressWarnings("FieldCanBeLocal") // Minimize GC on the tick method
    private Collection<HotMessage> expiringMessages;

    /**
     * Constructor.
     *
     * @param hotChatRepository  Repository for unexpired 'Hot' chat storage
     * @param coldChatRepository Repository for expired 'Cold' chat storage
     * @param modelMapper        ModelMapper bean for converting to and from types of messages
     * @param instance           Hazelcast instance for caching hot storage
     */
    @Autowired
    public MessageFacilitator(HotChatRepository hotChatRepository, ColdChatRepository coldChatRepository,
                              ModelMapper modelMapper, HazelcastInstance instance) {
        this.hotChatRepository = hotChatRepository;
        this.coldChatRepository = coldChatRepository;
        this.modelMapper = modelMapper;

        hotChatCache = instance.getMap("hotChatCache");
    }

    /**
     * Async method that runs on a worker thread to do the eviction process from hot storage to
     * cold storage. Async desired here to increase client observed speed, or 'snappy-ness' since
     * response for client is already populated and this is back-end processing not required
     * to be synchronous.
     *
     * @param hotMessages Hot storage messages
     */
    @Async
    public void moveHotCacheToColdStorage(Collection<HotMessage> hotMessages) {
        log.debug("Removing messages from hot storage and adding into cold storage: {}", hotMessages);

        // Evict the messages from the cache, from the backend hot persistence storage, and move to cold repository
        hotMessages.forEach(m -> hotChatCache.delete(m.getId()));

        hotChatRepository.deleteAll(hotMessages);
        coldChatRepository.saveAll(convertToColdMessages(hotMessages));
    }

    /**
     * Converts collection of Hot Message entities to Cold Message entities
     *
     * @param hotMessages Hot messages to convert
     * @return List of Cold messages
     */
    private List<ColdMessage> convertToColdMessages(Collection<HotMessage> hotMessages) {
        java.lang.reflect.Type targetListType = new TypeToken<List<ColdMessage>>() {}.getType();
        return modelMapper.map(new ArrayList<>(hotMessages), targetListType);
    }

    /**
     * Scheduler executor time delay based that orchestrates an eviction timer tick to periodically
     * check the Hot Chat Storage for any messages that have reached their expiration time. Expiring
     * messages are evicted from the cache and persisted hot storage, the messages are then persisted
     * into cold storage.
     * <p>Scheduler is configured as {@link Scheduled#fixedDelay()} which starts the job the configured
     * number of ms after last job iteration completes. Alternatively, depending on requirements a
     * {@link Scheduled#fixedRate()} can be used to precisely schedule a job to start the configured
     * number of ms after last job iteration started. Guaranteeing once every second, for example.
     * We favor fixedDelay that sacrifices eviction precision in favor for guaranteeing synchronous
     * execution, no two overlapping jobs in the event of a timeout or similar.
     */
    @Scheduled(fixedDelay = 995)
    protected void evictionTick() {
        // These two singletons can only be used with fixedDelay. If fixedRate is used, this will
        // need to be converted to local variable, resulting in more garbage collection.
        expirationPredicate = Predicates.lessEqual("expirationDate", LocalDateTime.now());
        expiringMessages = hotChatCache.values(expirationPredicate);

        // Nothing to do if no messages are expiring this tick
        if (expiringMessages.isEmpty()) {
            return;
        }

        log.debug("{} messages found for eviction. {}", expiringMessages.size(), expiringMessages);
        if (log.isTraceEnabled()) {
            log.trace("Hot Cache before eviction: {}", hotChatCache.values());
            log.trace("Cold Storage before eviction: {}", coldChatRepository.findAll());
        }

        // Move the messages that are beyond expired to the cold storage
        moveHotCacheToColdStorage(expiringMessages);

        if (log.isTraceEnabled()) {
            log.trace("Hot Cache after eviction: {}", hotChatCache.values());
            log.trace("Cold Storage after eviction: {}", coldChatRepository.findAll());
        }
    }
}
