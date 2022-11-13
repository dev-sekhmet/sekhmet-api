package com.sekhmet.sekhmetapi.repository;

import com.sekhmet.sekhmetapi.domain.User;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the {@link User} entity.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    String USERS_BY_LOGIN_CACHE = "usersByLogin";
    String USERS_BY_ID_CACHE = "usersById";

    String USERS_BY_EMAIL_CACHE = "usersByEmail";

    Optional<User> findOneByActivationKey(String activationKey);

    Optional<User> findOneByPhoneNumber(String phoneNumber);

    List<User> findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(Instant dateTime);

    Optional<User> findOneByResetKey(String resetKey);

    Optional<User> findOneByEmailIgnoreCase(String email);

    Optional<User> findOneByLogin(String login);

    @EntityGraph(attributePaths = "authorities")
    @Cacheable(cacheNames = USERS_BY_LOGIN_CACHE)
    Optional<User> findOneWithAuthoritiesByLogin(String login);

    @EntityGraph(attributePaths = "authorities")
    @Cacheable(cacheNames = USERS_BY_ID_CACHE)
    Optional<User> findById(UUID id);

    @EntityGraph(attributePaths = "authorities")
    @Cacheable(cacheNames = USERS_BY_EMAIL_CACHE)
    Optional<User> findOneWithAuthoritiesByEmailIgnoreCase(String email);

    Page<User> findAllByIdNotNullAndActivatedIsTrue(Pageable pageable);

    // find all using uses @Query where firstname ignorecase like '%'+firstname+'%' or lastname like '%'+lastname+'%' or email like '%'+email+'%' or phone like '%'+phone+'%'
    @Query(
        "select u from User u" +
        " where (LOWER(u.firstName) like %:search% or LOWER(u.lastName) like %:search% or LOWER(u.email) like %:search% or LOWER(u.phoneNumber) like %:search%)" +
        " and u.activated=true"
    )
    Page<User> findAllByFirstNameOrLastNameOrEmailOrPhone(@Param("search") String searchLowerCase, Pageable pageable);
}
