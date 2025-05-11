package com.business.business.token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TokenRepository extends JpaRepository<Token, UUID> {

    @Query(value = """
      select t from Token t inner join User u\s
      on t.user.id = u.id\s
      where u.id = :id\s
      """)
    List<Token> findAllTokenByUser(UUID id);

    Optional<Token> findByValue(String token);

    @Query("""
            SELECT CASE WHEN COUNT(t) > 0 THEN true
            ELSE false END
            FROM Token t WHERE t.value = :value
            """)
    boolean existsBValue(String value);

    @Modifying
    @Transactional
    @Query(value = """
      delete from token
      where user_id = :id
      """, nativeQuery = true)
    void deleteAllByUserId(UUID id);

}