package com.haru.api.push;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LoggingApnsPushService implements ApnsPushService {

    @Override
    public void sendTsunTsunPush(Long receiverId, Long tsuntsunId) {
        log.info("[APNS-STUB] send tsuntsun push: receiverId={}, tsuntsunId={}", receiverId, tsuntsunId);
    }
}
