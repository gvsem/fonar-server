package ru.georgii.fonarserver.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUid(String uid);

    @Query(value = "SELECT *  FROM users ORDER BY users.id DESC LIMIT :quantity OFFSET :offset"
            , nativeQuery = true)
    List<User> getUsers(@Param("quantity") Long quantity, @Param("offset") Long offset);

    Optional<User> findByNickname(String nickname);



}