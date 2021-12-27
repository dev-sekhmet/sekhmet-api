package com.sekhmet.sekhmetapi.repository;

import com.sekhmet.sekhmetapi.domain.Chat;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the Chat entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ChatRepository extends JpaRepository<Chat, UUID> {
    @Query("select c from Chat c join c.members m where m.user.login = :login1 and m.user.login = :login2")
    Optional<Chat> findContainingUsers(String login1, String login2);
}
