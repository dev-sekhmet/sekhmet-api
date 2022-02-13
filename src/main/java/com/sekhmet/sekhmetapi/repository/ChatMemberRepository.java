package com.sekhmet.sekhmetapi.repository;

import com.sekhmet.sekhmetapi.domain.ChatMember;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the ChatMember entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ChatMemberRepository extends JpaRepository<ChatMember, UUID> {
    @Query("select cm from ChatMember cm inner join cm.chat c where c.id = :chatId")
    Page<ChatMember> findAllByChat(@Param("chatId") UUID chatId, Pageable pageable);
}
