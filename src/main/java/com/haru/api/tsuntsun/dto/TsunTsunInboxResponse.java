package com.haru.api.tsuntsun.dto;

import java.util.List;

public record TsunTsunInboxResponse(
        Long userId,
        int unansweredCount,
        List<TsunTsunInboxItemResponse> items
) {
}
