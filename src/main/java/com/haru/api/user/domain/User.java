package com.haru.api.user.domain;

import com.haru.api.word.domain.WordLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    public User(Long id, String nickname, WordLevel learningLevel) {
        this(id, nickname, learningLevel, generateBuddyCode());
    }

    public User(Long id, String nickname, WordLevel learningLevel, String buddyCode) {
        this.id = id;
        this.nickname = nickname;
        this.learningLevel = learningLevel;
        this.buddyCode = buddyCode;
    }

    private static String generateBuddyCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
