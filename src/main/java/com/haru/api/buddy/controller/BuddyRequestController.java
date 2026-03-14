package com.haru.api.buddy.controller;

import com.haru.api.buddy.dto.BuddyRequestActionResponse;
import com.haru.api.buddy.dto.CreateBuddyRequestRequest;
import com.haru.api.buddy.dto.IncomingBuddyRequestResponse;
import com.haru.api.buddy.dto.OutgoingBuddyRequestResponse;
import com.haru.api.buddy.service.BuddyRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/buddy-requests")
@RequiredArgsConstructor
@Tag(name = "Buddy Requests")
public class BuddyRequestController {

    private final BuddyRequestService buddyRequestService;

    @PostMapping
    @Operation(summary = "버디 신청 보내기")
    public BuddyRequestActionResponse createRequest(@Valid @RequestBody CreateBuddyRequestRequest request) {
        return buddyRequestService.createRequest(request.requesterId(), request.targetUserId());
    }

    @GetMapping("/incoming")
    @Operation(summary = "받은 버디 신청 목록 조회")
    public List<IncomingBuddyRequestResponse> getIncomingRequests(@RequestParam Long userId) {
        return buddyRequestService.getIncomingRequests(userId);
    }

    @GetMapping("/outgoing")
    @Operation(summary = "보낸 버디 신청 목록 조회")
    public List<OutgoingBuddyRequestResponse> getOutgoingRequests(@RequestParam Long userId) {
        return buddyRequestService.getOutgoingRequests(userId);
    }

    @PostMapping("/{requestId}/accept")
    @Operation(summary = "버디 신청 수락")
    public BuddyRequestActionResponse acceptRequest(@PathVariable Long requestId) {
        return buddyRequestService.acceptRequest(requestId);
    }

    @PostMapping("/{requestId}/reject")
    @Operation(summary = "버디 신청 거절")
    public BuddyRequestActionResponse rejectRequest(@PathVariable Long requestId) {
        return buddyRequestService.rejectRequest(requestId);
    }
}
