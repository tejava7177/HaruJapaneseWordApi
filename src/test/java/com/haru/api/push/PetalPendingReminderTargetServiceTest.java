package com.haru.api.push;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.haru.api.tsuntsun.domain.TsunTsunStatus;
import com.haru.api.tsuntsun.repository.TsunTsunRepository;
import com.haru.api.user.domain.User;
import com.haru.api.word.domain.WordLevel;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PetalPendingReminderTargetServiceTest {

    @Mock
    private TsunTsunRepository tsunTsunRepository;

    @InjectMocks
    private PetalPendingReminderTargetService petalPendingReminderTargetService;

    @Test
    void findCandidateUsers_returnsUsersWithPendingPetals() {
        User receiver = new User(2L, "receiver", WordLevel.N4, "BBBB2222", null, null, null, false, true);
        given(tsunTsunRepository.findDistinctReceiversByStatus(TsunTsunStatus.SENT)).willReturn(List.of(receiver));

        List<User> result = petalPendingReminderTargetService.findCandidateUsers();

        assertThat(result).containsExactly(receiver);
        verify(tsunTsunRepository).findDistinctReceiversByStatus(TsunTsunStatus.SENT);
    }
}
