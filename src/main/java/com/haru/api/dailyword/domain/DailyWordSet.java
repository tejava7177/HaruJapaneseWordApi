package com.haru.api.dailyword.domain;

import com.haru.api.user.domain.User;
import com.haru.api.word.domain.Word;
import com.haru.api.word.domain.WordLevel;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Entity
@Table(
        name = "daily_word_set",
        uniqueConstraints = @UniqueConstraint(name = "uk_daily_word_set_user_date", columnNames = {"user_id", "target_date"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyWordSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "target_date", nullable = false)
    private LocalDate targetDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WordLevel level;

    @OneToMany(mappedBy = "dailyWordSet", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex asc")
    private List<DailyWordItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private DailyWordSet(User user, LocalDate targetDate, WordLevel level) {
        this.user = user;
        this.targetDate = targetDate;
        this.level = level;
    }

    public static DailyWordSet create(User user, LocalDate targetDate, WordLevel level) {
        return new DailyWordSet(user, targetDate, level);
    }

    public void addItem(Word word, int orderIndex) {
        this.items.add(new DailyWordItem(this, word, orderIndex));
    }
}
