package com.haru.api.buddy.service;

import com.haru.api.buddy.dto.DevBuddyResetResponse;
import com.haru.api.buddy.dto.SeededDevUserResponse;
import com.haru.api.buddy.repository.BuddyRequestRepository;
import com.haru.api.buddy.repository.BuddyRelationshipRepository;
import com.haru.api.buddy.repository.BuddyRepository;
import com.haru.api.dailyword.repository.DailyWordItemRepository;
import com.haru.api.dailyword.repository.DailyWordSetRepository;
import com.haru.api.tsuntsun.repository.TsunTsunAnswerRepository;
import com.haru.api.tsuntsun.repository.TsunTsunRepository;
import com.haru.api.user.init.UserDataInitializer;
import com.haru.api.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BuddyDevelopmentService {

    private final TsunTsunAnswerRepository tsunTsunAnswerRepository;
    private final TsunTsunRepository tsunTsunRepository;
    private final BuddyRequestRepository buddyRequestRepository;
    private final BuddyRepository buddyRepository;
    private final BuddyRelationshipRepository buddyRelationshipRepository;
    private final DailyWordItemRepository dailyWordItemRepository;
    private final DailyWordSetRepository dailyWordSetRepository;
    private final UserRepository userRepository;
    private final UserDataInitializer userDataInitializer;

    @Transactional
    public DevBuddyResetResponse resetBuddyData() {
        long tsunTsunAnswersDeleted = tsunTsunAnswerRepository.count();
        long tsunTsunDeleted = tsunTsunRepository.count();
        long buddyRequestsDeleted = buddyRequestRepository.count();
        long buddyRowsDeleted = buddyRepository.count();
        long buddyRelationshipsDeleted = buddyRelationshipRepository.count();
        long dailyWordItemsDeleted = dailyWordItemRepository.count();
        long dailyWordSetsDeleted = dailyWordSetRepository.count();

        log.info("[buddy/dev-reset] reset started: delete order = tsuntsun_answer -> tsuntsun -> buddy_request -> buddy -> buddy_relationship -> daily_word_item -> daily_word_set -> users");
        tsunTsunAnswerRepository.deleteAllInBatch();
        tsunTsunRepository.deleteAllInBatch();
        buddyRequestRepository.deleteAllInBatch();
        buddyRepository.deleteAllInBatch();
        buddyRelationshipRepository.deleteAllInBatch();
        dailyWordItemRepository.deleteAllInBatch();
        dailyWordSetRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        List<SeededDevUserResponse> seededUsers = userDataInitializer.seedUsers().stream()
                .map(SeededDevUserResponse::from)
                .toList();

        log.info("[buddy/dev-reset] reset completed: usersReset={}", seededUsers.size());
        return DevBuddyResetResponse.of(
                seededUsers.size(),
                buddyRowsDeleted,
                buddyRelationshipsDeleted,
                buddyRequestsDeleted,
                tsunTsunDeleted,
                tsunTsunAnswersDeleted,
                dailyWordSetsDeleted,
                dailyWordItemsDeleted,
                seededUsers
        );
    }
}
