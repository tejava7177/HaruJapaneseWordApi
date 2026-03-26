package com.haru.api.auth.service;

import com.haru.api.auth.dto.AppleAuthRequest;
import com.haru.api.auth.dto.AppleAuthResponse;
import com.haru.api.user.domain.User;
import com.haru.api.user.repository.UserRepository;
import com.haru.api.user.service.BuddyCodeService;
import com.haru.api.word.domain.WordLevel;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppleAuthService {

    private static final WordLevel DEFAULT_LEARNING_LEVEL = WordLevel.N5;

    private final UserRepository userRepository;
    private final BuddyCodeService buddyCodeService;
    private final AppleIdentityTokenParser appleIdentityTokenParser;

    @Transactional
    public AppleAuthResponse authenticate(AppleAuthRequest request) {
        ResolvedAppleAuth resolvedAuth = resolveAppleAuth(request);

        return userRepository.findByAppleSubject(resolvedAuth.appleSubject())
                .map(existingUser -> {
                    existingUser.linkAppleAuth(
                            resolvedAuth.appleSubject(),
                            resolvedAuth.email(),
                            resolvedAuth.displayName(),
                            LocalDateTime.now()
                    );
                    return AppleAuthResponse.from(existingUser, false);
                })
                .orElseGet(() -> createNewUser(resolvedAuth));
    }

    private ResolvedAppleAuth resolveAppleAuth(AppleAuthRequest request) {
        if ((request.identityToken() == null || request.identityToken().isBlank())
                && (request.appleUserId() == null || request.appleUserId().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "identityToken or appleUserId is required");
        }

        AppleIdentityTokenPayload tokenPayload = null;
        String resolvedAppleSubject = normalize(request.appleUserId());
        if (request.identityToken() != null && !request.identityToken().isBlank()) {
            tokenPayload = appleIdentityTokenParser.parse(request.identityToken());
            resolvedAppleSubject = tokenPayload.subject();
        }

        if (resolvedAppleSubject == null || resolvedAppleSubject.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to extract Apple subject");
        }

        String fallbackAppleUserId = normalize(request.appleUserId());
        if (tokenPayload != null && fallbackAppleUserId != null && !resolvedAppleSubject.equals(fallbackAppleUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "identityToken subject does not match appleUserId");
        }

        String resolvedEmail = normalize(request.email());
        if (resolvedEmail == null && tokenPayload != null) {
            resolvedEmail = normalize(tokenPayload.email());
        }

        return new ResolvedAppleAuth(
                resolvedAppleSubject,
                resolvedEmail,
                normalize(request.displayName())
        );
    }

    private AppleAuthResponse createNewUser(ResolvedAppleAuth resolvedAuth) {
        Long nextUserId = userRepository.findTopByOrderByIdDesc()
                .map(user -> user.getId() + 1L)
                .orElse(1L);

        User newUser = new User(
                nextUserId,
                generateNickname(nextUserId, resolvedAuth.displayName()),
                DEFAULT_LEARNING_LEVEL,
                buddyCodeService.generateUniqueBuddyCode(),
                null,
                null,
                null,
                false,
                resolvedAuth.appleSubject(),
                resolvedAuth.email(),
                resolvedAuth.displayName(),
                LocalDateTime.now()
        );

        try {
            User savedUser = userRepository.save(newUser);
            log.info("[AppleAuth] created new user userId={} appleSubject={}", savedUser.getId(), savedUser.getAppleSubject());
            return AppleAuthResponse.from(savedUser, true);
        } catch (DataIntegrityViolationException exception) {
            log.warn("[AppleAuth] duplicate appleSubject conflict appleSubject={}", resolvedAuth.appleSubject(), exception);
            return userRepository.findByAppleSubject(resolvedAuth.appleSubject())
                    .map(existingUser -> AppleAuthResponse.from(existingUser, false))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate Apple user conflict"));
        } catch (RuntimeException exception) {
            log.error("[AppleAuth] failed to save user appleSubject={}", resolvedAuth.appleSubject(), exception);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save Apple auth user");
        }
    }

    private String generateNickname(Long userId, String displayName) {
        String normalizedDisplayName = normalize(displayName);
        if (normalizedDisplayName != null) {
            return normalizedDisplayName;
        }
        return "사용자" + userId;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private record ResolvedAppleAuth(
            String appleSubject,
            String email,
            String displayName
    ) {
    }
}
