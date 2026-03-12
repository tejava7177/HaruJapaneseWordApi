package com.haru.api.user.init;

import com.haru.api.user.domain.User;
import com.haru.api.user.repository.UserRepository;
import com.haru.api.word.domain.WordLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class UserDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public void run(String... args) {
        upsertTestUser(1L, "juheun", "JUHEUN01");
        upsertTestUser(2L, "buddy2", "BUDDY002");
        upsertTestUser(3L, "buddy3", "BUDDY003");
        upsertTestUser(4L, "buddy4", "BUDDY004");
    }

    private void upsertTestUser(Long id, String nickname, String buddyCode) {
        User desired = new User(id, nickname, WordLevel.N4, buddyCode);

        userRepository.findById(id)
                .ifPresentOrElse(existing -> {
                    if (sameUser(existing, desired)) {
                        log.info("[init] test user ready: id={}, nickname={}, buddyCode={}", id, nickname, buddyCode);
                        return;
                    }

                    userRepository.save(desired);
                    log.info("[init] test user updated: id={}, nickname={}, buddyCode={}", id, nickname, buddyCode);
                }, () -> {
                    userRepository.save(desired);
                    log.info("[init] test user created: id={}, nickname={}, buddyCode={}", id, nickname, buddyCode);
                });
    }

    private boolean sameUser(User existing, User desired) {
        return existing.getNickname().equals(desired.getNickname())
                && existing.getLearningLevel() == desired.getLearningLevel()
                && existing.getBuddyCode().equals(desired.getBuddyCode());
    }
}
