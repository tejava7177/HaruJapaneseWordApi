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

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public User(Long id, String nickname, WordLevel learningLevel) {
        this(id, nickname, learningLevel, null, null, null, null, false);
    }

    public User(Long id, String nickname, WordLevel learningLevel, String buddyCode) {
        this(id, nickname, learningLevel, buddyCode, null, null, null, false);
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
        this.id = id;
        this.nickname = nickname;
        this.learningLevel = learningLevel;
        this.buddyCode = buddyCode;
        this.profileImageUrl = profileImageUrl;
        this.instagramId = instagramId;
        this.bio = bio;
        this.randomMatchingEnabled = randomMatchingEnabled;
    }

    public void changeLearningLevel(WordLevel learningLevel) {
        this.learningLevel = learningLevel;
    }

    public void changeRandomMatchingEnabled(boolean randomMatchingEnabled) {
        this.randomMatchingEnabled = randomMatchingEnabled;
    }
}
