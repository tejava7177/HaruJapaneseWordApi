package com.haru.api.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.haru.api.user.domain.User;
import com.haru.api.user.repository.UserRepository;
import com.haru.api.word.domain.WordLevel;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BuddyCodeServiceTest {

    @Mock
    private BuddyCodeGenerator buddyCodeGenerator;

    @Mock
    private UserRepository userRepository;

    private BuddyCodeService buddyCodeService;

    @BeforeEach
    void setUp() {
        buddyCodeService = new BuddyCodeService(buddyCodeGenerator, userRepository);
    }

    @Test
    void generateUniqueBuddyCode_retriesUntilUniqueCodeIsFound() {
        User existingUser = new User(1L, "juheun", WordLevel.N4, "AAAA1111");

        given(buddyCodeGenerator.generate()).willReturn("AAAA1111", "BBBB2222");
        given(userRepository.findByBuddyCode("AAAA1111")).willReturn(Optional.of(existingUser));
        given(userRepository.findByBuddyCode("BBBB2222")).willReturn(Optional.empty());

        String code = buddyCodeService.generateUniqueBuddyCode();

        assertThat(code).isEqualTo("BBBB2222");
    }
}
