package com.haru.api.reviewword.domain;

import com.haru.api.user.domain.User;
import com.haru.api.word.domain.Word;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Entity
@Table(
        name = "review_word",
        uniqueConstraints = @UniqueConstraint(name = "uk_review_word_user_word", columnNames = {"user_id", "word_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewWord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", nullable = false)
    private Word word;

    @CreationTimestamp
    @jakarta.persistence.Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private ReviewWord(User user, Word word) {
        this.user = user;
        this.word = word;
    }

    public static ReviewWord create(User user, Word word) {
        return new ReviewWord(user, word);
    }
}
