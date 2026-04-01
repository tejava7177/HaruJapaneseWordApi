package com.haru.api.userdevice.repository;

import com.haru.api.userdevice.domain.UserDeviceToken;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDeviceTokenRepository extends JpaRepository<UserDeviceToken, Long> {

    Optional<UserDeviceToken> findByDeviceToken(String deviceToken);

    Optional<UserDeviceToken> findByUserIdAndDeviceToken(Long userId, String deviceToken);

    List<UserDeviceToken> findByUserIdAndPushEnabledTrue(Long userId);

    boolean existsByUserId(Long userId);

    boolean existsByUserIdAndPushEnabledTrue(Long userId);
}
