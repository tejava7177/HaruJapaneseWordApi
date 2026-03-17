package com.haru.api.user.init;

import com.haru.api.user.domain.User;
import com.haru.api.user.repository.UserRepository;
import com.haru.api.user.service.BuddyCodeService;
import com.haru.api.word.domain.WordLevel;
import java.util.List;
import java.util.Objects;
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

    private static final List<SeedUserSpec> SEED_USERS = List.of(
            new SeedUserSpec(1L, "심주흔", WordLevel.N3, "@simjuheun", "JLPT N3 같이 준비해요", "7H2KQ9MP", true),
            new SeedUserSpec(2L, "김민성", WordLevel.N2, "@minsung_jp", "매일 한 문장씩 일본어 연습 중", "8TR4XK6N", true),
            new SeedUserSpec(3L, "김기범", WordLevel.N4, "@gibeom_study", "초급 회화부터 같이 해요", "5NC7PW2H", false),
            new SeedUserSpec(4L, "김정훈", WordLevel.N1, "@junghoon.nihongo", "고급 표현과 뉴스 일본어 좋아해요", "9LM3QV7R", true)
    );

    private final UserRepository userRepository;
    private final BuddyCodeService buddyCodeService;

    @Override
    @Transactional
    public void run(String... args) {
        seedUsers();
    }

    @Transactional
    public List<User> seedUsers() {
        return SEED_USERS.stream()
                .map(this::upsertSeedUser)
                .toList();
    }

    public int seedUserCount() {
        return SEED_USERS.size();
    }

    private User upsertSeedUser(SeedUserSpec seedUserSpec) {
        String resolvedBuddyCode = seedUserSpec.buddyCode() != null
                ? seedUserSpec.buddyCode()
                : buddyCodeService.generateUniqueBuddyCode();

        User desired = new User(
                seedUserSpec.id(),
                seedUserSpec.nickname(),
                seedUserSpec.learningLevel(),
                resolvedBuddyCode,
                null,
                seedUserSpec.instagramId(),
                seedUserSpec.bio(),
                seedUserSpec.randomMatchingEnabled()
        );

        return userRepository.findById(seedUserSpec.id())
                .map(existing -> {
                    if (sameUser(existing, desired)) {
                        log.info("[init] test user ready: id={}, nickname={}, buddyCode={}",
                                seedUserSpec.id(), seedUserSpec.nickname(), resolvedBuddyCode);
                        return existing;
                    }

                    User savedUser = userRepository.save(new User(
                            desired.getId(),
                            desired.getNickname(),
                            desired.getLearningLevel(),
                            desired.getBuddyCode(),
                            existing.getProfileImageUrl(),
                            desired.getInstagramId(),
                            desired.getBio(),
                            desired.isRandomMatchingEnabled()
                    ));
                    log.info("[init] test user updated: id={}, nickname={}, buddyCode={}",
                            seedUserSpec.id(), seedUserSpec.nickname(), resolvedBuddyCode);
                    return savedUser;
                })
                .orElseGet(() -> {
                    User savedUser = userRepository.save(desired);
                    log.info("[init] test user created: id={}, nickname={}, buddyCode={}",
                            seedUserSpec.id(), seedUserSpec.nickname(), resolvedBuddyCode);
                    return savedUser;
                });
    }

    private boolean sameUser(User existing, User desired) {
        return existing.getNickname().equals(desired.getNickname())
                && existing.getLearningLevel() == desired.getLearningLevel()
                && existing.getBuddyCode().equals(desired.getBuddyCode())
                && Objects.equals(existing.getInstagramId(), desired.getInstagramId())
                && Objects.equals(existing.getBio(), desired.getBio())
                && existing.isRandomMatchingEnabled() == desired.isRandomMatchingEnabled();
    }

    private record SeedUserSpec(
            Long id,
            String nickname,
            WordLevel learningLevel,
            String instagramId,
            String bio,
            String buddyCode,
            boolean randomMatchingEnabled
    ) {
    }
}
