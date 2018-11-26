/*
 * Chat Service Client Library
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
package com.underarmour.interview.chat.model;

/**
 * Incoming API event to persist a message to Hot Storage
 *
 * @author david.moore
 */
public class TimedMessage {
    private String username;
    private String text;
    private int timeout = 60;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public String toString() {
        return "TimedMessage{" +
                "username='" + username + '\'' +
                ", text='" + text + '\'' +
                ", timeout=" + timeout +
                '}';
    }
}
