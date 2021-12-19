package com.sekhmet.sekhmetapi.repository;

import com.sekhmet.sekhmetapi.domain.Message;
import java.util.UUID;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the Message entity.
 */
@SuppressWarnings("unused")
@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {}
