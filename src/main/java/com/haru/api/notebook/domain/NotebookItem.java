package com.haru.api.notebook.domain;

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
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Entity
@Table(
        name = "notebook_item",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_notebook_item_notebook_word",
                columnNames = {"notebook_id", "word_id"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotebookItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notebook_id", nullable = false)
    private Notebook notebook;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    private NotebookItemType itemType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id")
    private Word word;

    @Column(nullable = false)
    private String expression;

    @Column
    private String reading;

    @Column(nullable = false)
    private String meaning;

    @Column(name = "memo")
    private String memo;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private NotebookItem(
            NotebookItemType itemType,
            Word word,
            String expression,
            String reading,
            String meaning,
            String memo,
            Integer sortOrder
    ) {
        this.itemType = itemType;
        this.word = word;
        this.expression = expression;
        this.reading = reading;
        this.meaning = meaning;
        this.memo = memo;
        this.sortOrder = sortOrder;
    }

    public static NotebookItem create(
            NotebookItemType itemType,
            Word word,
            String expression,
            String reading,
            String meaning,
            String memo,
            Integer sortOrder
    ) {
        return new NotebookItem(itemType, word, expression, reading, meaning, memo, sortOrder);
    }

    public void update(
            NotebookItemType itemType,
            Word word,
            String expression,
            String reading,
            String meaning,
            String memo,
            Integer sortOrder
    ) {
        this.itemType = itemType;
        this.word = word;
        this.expression = expression;
        this.reading = reading;
        this.meaning = meaning;
        this.memo = memo;
        this.sortOrder = sortOrder;
    }

    public void assignNotebook(Notebook notebook) {
        this.notebook = notebook;
    }
}
