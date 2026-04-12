package com.haru.api.reviewword.dto;

import java.util.List;

public record ReviewWordListResponse(
        Long userId,
        List<Long> wordIds
) {
}
