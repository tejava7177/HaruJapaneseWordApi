package com.haru.api.user.domain;

import com.haru.api.word.domain.WordLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

    public User(Long id, String nickname, WordLevel learningLevel) {
        this.id = id;
        this.nickname = nickname;
        this.learningLevel = learningLevel;
    }
}
