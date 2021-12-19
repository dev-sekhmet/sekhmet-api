package com.sekhmet.sekhmetapi.repository;

import com.sekhmet.sekhmetapi.domain.Chat;
import java.util.UUID;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the Chat entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ChatRepository extends JpaRepository<Chat, UUID> {}
