package com.sekhmet.sekhmetapi.repository;

import com.sekhmet.sekhmetapi.domain.ChatMember;
import java.util.UUID;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the ChatMember entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ChatMemberRepository extends JpaRepository<ChatMember, UUID> {}
