package com.haru.api.buddy.dto;

import com.haru.api.user.domain.User;
import java.time.LocalDate;

public final class BuddyProfileTextFormatter {

    private BuddyProfileTextFormatter() {
    }

    public static String lastSeenText(User user) {
        if (user.getUpdatedAt() == null) {
            return "최근 활동 확인 중";
        }

        long days = java.time.temporal.ChronoUnit.DAYS.between(user.getUpdatedAt().toLocalDate(), LocalDate.now());
        if (days <= 0) {
            return "오늘 활동";
        }
        return days + "일 전";
    }
}
