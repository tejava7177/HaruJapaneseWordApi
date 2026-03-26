package com.haru.api.userdevice.domain;

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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Entity
@Table(name = "user_device_token")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserDeviceToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "device_token", nullable = false, unique = true, length = 512)
    private String deviceToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DevicePlatform platform;

    @Column(name = "push_enabled", nullable = false)
    private boolean pushEnabled;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

    private UserDeviceToken(User user, String deviceToken, DevicePlatform platform, boolean pushEnabled) {
        this.user = user;
        this.deviceToken = deviceToken;
        this.platform = platform;
        this.pushEnabled = pushEnabled;
        this.lastSeenAt = LocalDateTime.now();
    }

    public static UserDeviceToken create(User user, String deviceToken, DevicePlatform platform, boolean pushEnabled) {
        return new UserDeviceToken(user, deviceToken, platform, pushEnabled);
    }

    public void register(User user, DevicePlatform platform, boolean pushEnabled) {
        this.user = user;
        this.platform = platform;
        this.pushEnabled = pushEnabled;
        this.lastSeenAt = LocalDateTime.now();
    }

    public void unregister() {
        this.pushEnabled = false;
        this.lastSeenAt = LocalDateTime.now();
    }
}
