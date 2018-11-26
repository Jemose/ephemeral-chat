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
package com.underarmour.interview.chat.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Hot Storage Message entity
 *
 * @author david.moore
 */
@Entity
public class HotMessage implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String username;
    private String text;
    private LocalDateTime expirationDate;

    public HotMessage(String username, String text, LocalDateTime expirationDate) {
        this.username = username;
        this.text = text;
        this.expirationDate = expirationDate;
    }

    public HotMessage() {
    }

    @JsonIgnore
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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
        return "HotMessage{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", text='" + text + '\'' +
                ", expirationDate=" + expirationDate +
                '}';
    }
}
