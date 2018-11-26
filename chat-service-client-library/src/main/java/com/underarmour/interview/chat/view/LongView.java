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
package com.underarmour.interview.chat.view;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * View for REST call response that contains the relevant fields of the hot/cold messages
 *
 * @author david.moore
 */
public class LongView implements Serializable {
    private String username;
    private String text;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expirationDate;

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

    @JsonProperty("expiration_date")
    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

    @Override
    public String toString() {
        return "LongView{" +
                "username='" + username + '\'' +
                ", text='" + text + '\'' +
                ", expirationDate=" + expirationDate +
                '}';
    }
}
