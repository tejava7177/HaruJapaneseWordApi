package com.haru.api.user.repository;

import com.haru.api.user.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByBuddyCode(String buddyCode);

    Optional<User> findByAppleSubject(String appleSubject);

    Optional<User> findTopByOrderByIdDesc();

    List<User> findByRandomMatchingEnabledTrueOrderByIdAsc();
}
