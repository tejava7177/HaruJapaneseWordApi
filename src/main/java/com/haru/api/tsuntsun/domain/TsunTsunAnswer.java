package com.haru.api.tsuntsun.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Entity
@Table(name = "tsuntsun_answer")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TsunTsunAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tsuntsun_id", nullable = false)
    private TsunTsun tsuntsun;

    @Column(name = "selected_meaning", nullable = false)
    private String selectedMeaning;

    @Column(name = "is_correct", nullable = false)
    private boolean isCorrect;

    @CreationTimestamp
    @Column(name = "answered_at", nullable = false, updatable = false)
    private LocalDateTime answeredAt;

    private TsunTsunAnswer(TsunTsun tsuntsun, String selectedMeaning, boolean isCorrect) {
        this.tsuntsun = tsuntsun;
        this.selectedMeaning = selectedMeaning;
        this.isCorrect = isCorrect;
    }

    public static TsunTsunAnswer of(TsunTsun tsuntsun, String selectedMeaning, boolean isCorrect) {
        return new TsunTsunAnswer(tsuntsun, selectedMeaning, isCorrect);
    }
}
