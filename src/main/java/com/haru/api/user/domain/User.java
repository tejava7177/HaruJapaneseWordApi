package com.haru.api.user.domain;

import com.haru.api.word.domain.WordLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    private Long id;

    @Column(nullable = false)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "learning_level", nullable = false)
    private WordLevel learningLevel;

    @Column(name = "buddy_code", nullable = false, unique = true, length = 8)
    private String buddyCode;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "instagram_id")
    private String instagramId;

    @Column(name = "bio")
    private String bio;

    @Column(name = "random_matching_enabled", nullable = false)
    private boolean randomMatchingEnabled;

    @Column(name = "petal_notifications_enabled", nullable = false)
    private boolean petalNotificationsEnabled;

    @Column(name = "apple_subject", unique = true)
    private String appleSubject;

    @Column(name = "auth_email")
    private String authEmail;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "last_active_at")
    private LocalDateTime lastActiveAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public User(Long id, String nickname, WordLevel learningLevel) {
        this(id, nickname, learningLevel, null, null, null, null, false, true, null, null, null, null, null);
    }

    public User(Long id, String nickname, WordLevel learningLevel, String buddyCode) {
        this(id, nickname, learningLevel, buddyCode, null, null, null, false, true, null, null, null, null, null);
    }

    public User(
            Long id,
            String nickname,
            WordLevel learningLevel,
            String buddyCode,
            String profileImageUrl,
            String instagramId,
            String bio,
            boolean randomMatchingEnabled
    ) {
        this(id, nickname, learningLevel, buddyCode, profileImageUrl, instagramId, bio, randomMatchingEnabled,
                true, null, null, null, null, null);
    }

    public User(
            Long id,
            String nickname,
            WordLevel learningLevel,
            String buddyCode,
            String profileImageUrl,
            String instagramId,
            String bio,
            boolean randomMatchingEnabled,
            boolean petalNotificationsEnabled
    ) {
        this(id, nickname, learningLevel, buddyCode, profileImageUrl, instagramId, bio, randomMatchingEnabled,
                petalNotificationsEnabled, null, null, null, null, null);
    }

    public User(
            Long id,
            String nickname,
            WordLevel learningLevel,
            String buddyCode,
            String profileImageUrl,
            String instagramId,
            String bio,
            boolean randomMatchingEnabled,
            boolean petalNotificationsEnabled,
            String appleSubject,
            String authEmail,
            String displayName,
            LocalDateTime lastLoginAt,
            LocalDateTime lastActiveAt
    ) {
        this.id = id;
        this.nickname = nickname;
        this.learningLevel = learningLevel;
        this.buddyCode = buddyCode;
        this.profileImageUrl = profileImageUrl;
        this.instagramId = instagramId;
        this.bio = bio;
        this.randomMatchingEnabled = randomMatchingEnabled;
        this.petalNotificationsEnabled = petalNotificationsEnabled;
        this.appleSubject = appleSubject;
        this.authEmail = authEmail;
        this.displayName = displayName;
        this.lastLoginAt = lastLoginAt;
        this.lastActiveAt = lastActiveAt;
    }

    public User(
            Long id,
            String nickname,
            WordLevel learningLevel,
            String buddyCode,
            String profileImageUrl,
            String instagramId,
            String bio,
            boolean randomMatchingEnabled,
            String appleSubject,
            String authEmail,
            String displayName,
            LocalDateTime lastLoginAt
    ) {
        this(id, nickname, learningLevel, buddyCode, profileImageUrl, instagramId, bio, randomMatchingEnabled,
                true, appleSubject, authEmail, displayName, lastLoginAt, null);
    }

    public void changeLearningLevel(WordLevel learningLevel) {
        this.learningLevel = learningLevel;
    }

    public void changeRandomMatchingEnabled(boolean randomMatchingEnabled) {
        this.randomMatchingEnabled = randomMatchingEnabled;
    }

    public void changePetalNotificationsEnabled(boolean petalNotificationsEnabled) {
        this.petalNotificationsEnabled = petalNotificationsEnabled;
    }

    public void changeProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void updateProfile(String nickname, String bio, String instagramId) {
        if (nickname != null) {
            this.nickname = nickname;
        }
        this.bio = bio;
        this.instagramId = instagramId;
    }

    public void linkAppleAuth(String appleSubject, String authEmail, String displayName, LocalDateTime lastLoginAt) {
        this.appleSubject = appleSubject;
        this.authEmail = authEmail;
        this.displayName = displayName;
        this.lastLoginAt = lastLoginAt;
    }

    public void updateLastActiveAt(LocalDateTime lastActiveAt) {
        this.lastActiveAt = lastActiveAt;
    }
}
