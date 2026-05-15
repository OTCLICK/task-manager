package com.student.backend.repository;

import com.student.backend.model.UserFcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserFcmTokenRepository extends JpaRepository<UserFcmToken, String> {

    List<UserFcmToken> findByUser_Id(String userId);

    void deleteByToken(String token);

    @Modifying
    @Query("delete from UserFcmToken t where t.user.id = :userId")
    void deleteAllByUserId(@Param("userId") String userId);

    @Modifying
    @Query("delete from UserFcmToken t where t.user.id = :userId and t.token = :token")
    void deleteByUserIdAndToken(@Param("userId") String userId, @Param("token") String token);
}
