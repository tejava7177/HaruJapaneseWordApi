package com.haru.api.buddy.domain;

import com.haru.api.user.domain.User;
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

@Getter
@Entity
@Table(
        name = "buddy",
        uniqueConstraints = @UniqueConstraint(name = "uk_buddy_user_pair", columnNames = {"user_id", "buddy_user_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Buddy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buddy_user_id", nullable = false)
    private User buddyUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BuddyStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private Buddy(User user, User buddyUser, BuddyStatus status) {
        this.user = user;
        this.buddyUser = buddyUser;
        this.status = status;
    }

    public static Buddy active(User user, User buddyUser) {
        return new Buddy(user, buddyUser, BuddyStatus.ACTIVE);
    }
}
