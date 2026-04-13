package com.haru.api.tsuntsun.domain;

import com.haru.api.buddy.domain.BuddyRelationship;
import com.haru.api.dailyword.domain.DailyWordItem;
import com.haru.api.user.domain.User;
import com.haru.api.word.domain.Word;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Entity
@Table(name = "tsuntsun")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TsunTsun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", nullable = false)
    private Word word;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_word_item_id", nullable = false)
    private DailyWordItem dailyWordItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buddy_relationship_id", nullable = false)
    private BuddyRelationship buddyRelationship;

    @Column(name = "target_date", nullable = false)
    private LocalDate targetDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TsunTsunStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "quiz_type", nullable = false)
    private TsunTsunQuizType quizType;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private TsunTsun(
            User sender,
            User receiver,
            Word word,
            DailyWordItem dailyWordItem,
            BuddyRelationship buddyRelationship,
            LocalDate targetDate,
            TsunTsunQuizType quizType
    ) {
        this.sender = sender;
        this.receiver = receiver;
        this.word = word;
        this.dailyWordItem = dailyWordItem;
        this.buddyRelationship = buddyRelationship;
        this.targetDate = targetDate;
        this.status = TsunTsunStatus.SENT;
        this.quizType = quizType;
    }

    public static TsunTsun sent(
            User sender,
            User receiver,
            Word word,
            DailyWordItem dailyWordItem,
            BuddyRelationship buddyRelationship,
            LocalDate targetDate,
            TsunTsunQuizType quizType
    ) {
        Objects.requireNonNull(buddyRelationship, "buddyRelationship must not be null");
        Objects.requireNonNull(quizType, "quizType must not be null");
        return new TsunTsun(sender, receiver, word, dailyWordItem, buddyRelationship, targetDate, quizType);
    }

    public void markAnswered() {
        this.status = TsunTsunStatus.ANSWERED;
    }
}
