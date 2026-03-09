package com.haru.api.word.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "word")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Word {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String expression;

    @Column(nullable = false)
    private String reading;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WordLevel level;

    @OneToMany(mappedBy = "word", fetch = FetchType.LAZY)
    @OrderBy("ord asc")
    private List<Meaning> meanings = new ArrayList<>();

    public Word(String expression, String reading, WordLevel level) {
        this.expression = expression;
        this.reading = reading;
        this.level = level;
    }
}
