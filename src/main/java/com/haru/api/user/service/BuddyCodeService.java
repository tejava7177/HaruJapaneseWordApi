package com.haru.api.user.service;

import com.haru.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BuddyCodeService {

    private final BuddyCodeGenerator buddyCodeGenerator;
    private final UserRepository userRepository;

    public String generateUniqueBuddyCode() {
        String candidate = buddyCodeGenerator.generate();
        while (userRepository.findByBuddyCode(candidate).isPresent()) {
            candidate = buddyCodeGenerator.generate();
        }
        return candidate;
    }
}
