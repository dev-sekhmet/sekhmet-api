package com.sekhmet.sekhmetapi.repository;

import com.sekhmet.sekhmetapi.domain.Message;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the Message entity.
 */
@SuppressWarnings("unused")
@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {
    @Query("select m from Message m inner join m.chat c where c.id = :chatId")
    Page<Message> findAllByChat(@Param("chatId") UUID chatId, Pageable pageable);
}
