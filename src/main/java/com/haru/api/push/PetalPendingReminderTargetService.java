package com.haru.api.push;

import com.haru.api.tsuntsun.domain.TsunTsunStatus;
import com.haru.api.tsuntsun.repository.TsunTsunRepository;
import com.haru.api.user.domain.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PetalPendingReminderTargetService {

    private final TsunTsunRepository tsunTsunRepository;

    public List<User> findCandidateUsers() {
        return tsunTsunRepository.findDistinctReceiversByStatus(TsunTsunStatus.SENT);
    }
}
