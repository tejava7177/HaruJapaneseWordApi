package com.haru.api.buddy.dto;

import java.util.List;

public record DevBuddyResetResponse(
        int usersReset,
        long buddyRowsDeleted,
        long buddyRelationshipsDeleted,
        long buddyRequestsDeleted,
        long tsunTsunDeleted,
        long tsunTsunAnswersDeleted,
        long dailyWordSetsDeleted,
        long dailyWordItemsDeleted,
        List<SeededDevUserResponse> seededUsers
) {
    public static DevBuddyResetResponse of(
            int usersReset,
            long buddyRowsDeleted,
            long buddyRelationshipsDeleted,
            long buddyRequestsDeleted,
            long tsunTsunDeleted,
            long tsunTsunAnswersDeleted,
            long dailyWordSetsDeleted,
            long dailyWordItemsDeleted,
            List<SeededDevUserResponse> seededUsers
    ) {
        return new DevBuddyResetResponse(
                usersReset,
                buddyRowsDeleted,
                buddyRelationshipsDeleted,
                buddyRequestsDeleted,
                tsunTsunDeleted,
                tsunTsunAnswersDeleted,
                dailyWordSetsDeleted,
                dailyWordItemsDeleted,
                seededUsers
        );
    }
}
