package com.sekhmet.sekhmetapi.repository;

import com.sekhmet.sekhmetapi.domain.Chat;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the Chat entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ChatRepository extends JpaRepository<Chat, UUID> {
    @Query(
        value = "SELECT c.*\n" +
        "FROM Chat c\n" +
        "         INNER JOIN chat_member cm ON cm.chat_id = c.id\n" +
        "         INNER JOIN skh_user u ON u.id = cm.user_id\n" +
        "WHERE u.id = :user1 AND c.chat_type='TWO_USER' \n" +
        "INTERSECT\n" +
        "SELECT c.*\n" +
        "FROM Chat c\n" +
        "         INNER JOIN chat_member cm ON cm.chat_id = c.id\n" +
        "         INNER JOIN skh_user u ON u.id = cm.user_id\n" +
        "WHERE u.id = :user2 AND c.chat_type='TWO_USER'",
        nativeQuery = true
    )
    Optional<Chat> findChatByMembers(@Param("user1") UUID user1, @Param("user2") UUID user2);
}
