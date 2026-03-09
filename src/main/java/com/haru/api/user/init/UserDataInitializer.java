package com.haru.api.user.init;

import com.haru.api.user.domain.User;
import com.haru.api.user.repository.UserRepository;
import com.haru.api.word.domain.WordLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UserDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (!userRepository.existsById(1L)) {
            userRepository.save(new User(1L, "juheun", WordLevel.N4));
        }
    }
}
