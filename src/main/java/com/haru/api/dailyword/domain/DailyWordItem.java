package com.haru.api.dailyword.domain;

import com.haru.api.word.domain.Word;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "daily_word_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyWordItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_word_set_id", nullable = false)
    private DailyWordSet dailyWordSet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", nullable = false)
    private Word word;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    DailyWordItem(DailyWordSet dailyWordSet, Word word, Integer orderIndex) {
        this.dailyWordSet = dailyWordSet;
        this.word = word;
        this.orderIndex = orderIndex;
    }
}
