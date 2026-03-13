package com.haru.api.buddy.service;

import com.haru.api.buddy.dto.DevBuddyResetResponse;
import com.haru.api.buddy.repository.BuddyRelationshipRepository;
import com.haru.api.buddy.repository.BuddyRepository;
import com.haru.api.tsuntsun.repository.TsunTsunAnswerRepository;
import com.haru.api.tsuntsun.repository.TsunTsunRepository;
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
    private final BuddyRepository buddyRepository;
    private final BuddyRelationshipRepository buddyRelationshipRepository;

    @Transactional
    public DevBuddyResetResponse resetBuddyData() {
        log.info("[buddy/dev-reset] reset started: delete order = tsuntsun_answer -> tsuntsun -> buddy -> buddy_relationship");
        tsunTsunAnswerRepository.deleteAllInBatch();
        tsunTsunRepository.deleteAllInBatch();
        buddyRepository.deleteAllInBatch();
        buddyRelationshipRepository.deleteAllInBatch();
        log.info("[buddy/dev-reset] reset completed");
        return DevBuddyResetResponse.success();
    }
}
