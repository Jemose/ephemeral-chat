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
import com.underarmour.interview.chat.model.TimedMessage;
import com.underarmour.interview.chat.repository.ColdChatRepository;
import com.underarmour.interview.chat.repository.HotChatRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;

/**
 * Chat service layer that persists and moves messages from hot to cold storage.
 *
 * @author david.moore
 */
@Service
public class ChatService {
    private static Logger log = LoggerFactory.getLogger(ChatService.class);
    private IMap<Integer, HotMessage> hotChatCache;
    private HotChatRepository hotChatRepository;
    private ColdChatRepository coldChatRepository;
    private ModelMapper modelMapper;
    private MessageFacilitator messageFacilitator;

    /**
     * Constructor.
     *
     * @param hotChatRepository Hot Chat Repository
     * @param coldChatRepository Cold Chat Repository
     * @param modelMapper ModelMapper to convert objects to and from
     * @param instance Hazelcast instance to initialize cache imap
     * @param messageFacilitator Message Facilitator for backend async operations
     */
    @Autowired
    public ChatService(HotChatRepository hotChatRepository, ColdChatRepository coldChatRepository,
                       ModelMapper modelMapper, HazelcastInstance instance, MessageFacilitator messageFacilitator) {
        this.hotChatRepository = hotChatRepository;
        this.coldChatRepository = coldChatRepository;
        this.modelMapper = modelMapper;
        this.messageFacilitator = messageFacilitator;

        hotChatCache = instance.getMap("hotChatCache");
    }

    /**
     * Converts the TimedMessage into the HotMessage with the calculated expiration date,
     * then persists it to the Hot Storage and Cache.
     *
     * @param timedMessage Message with data to be converted
     * @return HotMessage
     */
    public HotMessage saveMessage(TimedMessage timedMessage) {
        // Expiration datetime is the number of seconds from TimedMessage 'timeout' field, from now.
        LocalDateTime expireTime = LocalDateTime.now().plusSeconds(timedMessage.getTimeout());

        // Create a new Hot Message and persist it to the Hot Storage. The message returned contains populated
        // 'id' primary key identifier from JPA. Store this message in the cache.
        HotMessage hotMessage = new HotMessage(timedMessage.getUsername(), timedMessage.getText(), expireTime);
        hotMessage = hotChatRepository.save(hotMessage);
        hotChatCache.put(hotMessage.getId(), hotMessage);

        return hotMessage;
    }

    /**
     * Get chat message regardless if expired or unexpired. Therefore cache should be checked first,
     * then backing cache in case cache was not populated, then cold storage.
     *
     * @param id Message ID to retrieve
     * @return HotMessage
     */
    public HotMessage getMessage(int id) {
        // Attempt message retrieval from cache first
        HotMessage message = hotChatCache.get(id);
        if (message != null) {
            return message;
        }

        // Attempt message retrieval from backend persisted Hot Storage
        message = hotChatRepository.findById(id).orElse(null);
        if (message != null) {
            // Found a message in the repo but not cache, add it back into the cache
            hotChatCache.put(message.getId(), message);
            return message;
        }

        // Finally, attempt message retrieval from cold storage
        ColdMessage coldMessage = coldChatRepository.findById(id).orElse(null);
        if (coldMessage != null) {
            // Convert Cold to Hot for rest response
            message = new HotMessage(coldMessage.getUsername(), coldMessage.getText(), coldMessage.getExpirationDate());
        }

        return message;
    }

    /**
     * Get unexpired chat messages by username
     *
     * @param username Username to search for
     * @return Collection of HotMessages found by username
     */
    public Collection<HotMessage> getMessageByUsername(String username) {
        // Create a predicate to find messages in cache by username
        Predicate predicate = Predicates.equal("username", username);
        Collection<HotMessage> hotMessages = hotChatCache.values(predicate);

        // If no messages were found, check the backing persistence storage.
        if (hotMessages.isEmpty()) {
            log.debug("No messages found in cache, checking backing persistence storage now");
            hotMessages = hotChatRepository.findByUsername(username);
        }

        // Unexpired messages that are found by username are immediately expired, move to cold storage
        messageFacilitator.moveHotCacheToColdStorage(hotMessages);
        return hotMessages;
    }
}