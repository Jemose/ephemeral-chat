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
package com.underarmour.interview.chat.repository;

import com.underarmour.interview.chat.entity.HotMessage;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Hot Chat Repository, backend storage for cache
 *
 * @author david.moore
 */
@Repository
public interface HotChatRepository extends CrudRepository<HotMessage, Integer> {
    List<HotMessage> findByUsername(String username);
}
