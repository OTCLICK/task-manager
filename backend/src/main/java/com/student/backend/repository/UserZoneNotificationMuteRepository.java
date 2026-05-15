package com.student.backend.repository;

import com.student.backend.model.UserZoneNotificationMute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserZoneNotificationMuteRepository extends JpaRepository<UserZoneNotificationMute, String> {

    boolean existsByUser_IdAndZone_Id(String userId, String zoneId);

    List<UserZoneNotificationMute> findByUser_Id(String userId);

    @Modifying
    @Query("delete from UserZoneNotificationMute m where m.user.id = :userId")
    void deleteAllByUserId(@Param("userId") String userId);

    @Modifying
    @Query("delete from UserZoneNotificationMute m where m.user.id = :userId and m.zone.id = :zoneId")
    void deleteByUserIdAndZoneId(@Param("userId") String userId, @Param("zoneId") String zoneId);
}
