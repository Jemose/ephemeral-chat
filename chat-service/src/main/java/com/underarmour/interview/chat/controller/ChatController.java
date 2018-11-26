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
package com.underarmour.interview.chat.controller;

import com.underarmour.interview.chat.entity.HotMessage;
import com.underarmour.interview.chat.model.TimedMessage;
import com.underarmour.interview.chat.service.ChatService;
import com.underarmour.interview.chat.view.IdView;
import com.underarmour.interview.chat.view.LongView;
import com.underarmour.interview.chat.view.TextView;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Chat service main API entry point
 *
 * @author david.moore
 */
@RestController
public class ChatController {
    private static Logger log = LoggerFactory.getLogger(ChatController.class);
    private ModelMapper modelMapper;
    private final ChatService chatService;

    /**
     * Constructor.
     *
     * @param modelMapper ModelMapper for converting objects to and from
     * @param chatService Chat Service for backend logic
     */
    @Autowired
    public ChatController(ModelMapper modelMapper, ChatService chatService) {
        this.modelMapper = modelMapper;
        this.chatService = chatService;
    }

    /**
     * POST REST endpoint to add message into the chat. Accepts body of a {@link TimedMessage}
     * object that will be added to the Hot Storage and cache. {@link TimedMessage#getTimeout()}
     * (in seconds) indicates how long the message will live before expiring and gets evicted from
     * the Hot Storage into Cold Storage.
     *
     * @param timedMessage Incoming message with Timeout
     * @return Json representation of the 'id' of the persisted message. Otherwise, BAD_REQUEST with error message.
     */
    @PostMapping(value = "/chat", produces = "application/json")
    @ResponseBody
    public ResponseEntity addMessage(@RequestBody TimedMessage timedMessage) {
        log.info("POST /chat/ Received. RequestBody: {}", timedMessage);

        // Malformed incoming event check
        if (!StringUtils.hasLength(timedMessage.getUsername())) {
            return new ResponseEntity<>("'Username' field can not be null/empty!", HttpStatus.BAD_REQUEST);
        }
        if (!StringUtils.hasLength(timedMessage.getText())) {
            return new ResponseEntity<>("'Text' field can not be null/empty!", HttpStatus.BAD_REQUEST);
        }
        if (timedMessage.getTimeout() <= 0) {
            return new ResponseEntity<>("'Timeout' must be greater than 0!", HttpStatus.BAD_REQUEST);
        }

        // Save the message to the repository and cache
        HotMessage hotMessage = chatService.saveMessage(timedMessage);

        // Simple 'id' json response
        return new ResponseEntity<>(modelMapper.map(hotMessage, IdView.class), HttpStatus.CREATED);
    }

    /**
     * GET REST endpoint to retrieve a message from the chat by {@code id}. Message retrieval is attempted
     * from the distributed cache, hot storage, then cold storage. Id is consistent across hot and cold storage.
     *
     * @param id ID of message to retrieve
     * @return Chat message from either hot or cold storage.
     */
    @GetMapping("/chat/{id}")
    @ResponseBody
    public ResponseEntity getChat(@PathVariable int id) {
        log.info("GET /chat/{}/ received", id);

        // Retrieve the message from the hot/cold storage
        HotMessage message = chatService.getMessage(id);

        if (message == null) {
            return new ResponseEntity<>("Message id='" + id + "' not found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(modelMapper.map(message, LongView.class), HttpStatus.OK);
    }

    /**
     * GET REST endpoint to retrieve all unexpired (Hot Storage) messages for the given username.
     * Backend logic will move these messages from the hot storage to the cold storage as they are
     * now considered expired messages.
     *
     * @param username Username to retrieve unexpired messages for
     * @return Collection of unexpired messages for user
     */
    @GetMapping("/chats/{username}")
    @ResponseBody
    public ResponseEntity getChatsByUsername(@PathVariable String username) {
        log.info("GET /chats/{}/ received", username);

        // Retrieve the list of unexpired messages for the username
        Collection<HotMessage> messages = chatService.getMessageByUsername(username);

        // Convert the messages to proper format
        java.lang.reflect.Type targetListType = new TypeToken<List<TextView>>() {}.getType();
        List<TextView> textViews = modelMapper.map(new ArrayList<>(messages), targetListType);

        log.info("{} chats found for username='{}' : {}", textViews.size(), username, textViews);
        return new ResponseEntity<>(textViews, HttpStatus.OK);
    }
}
