package com.haru.api.push;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Slf4j
@Primary
@Component
public class NoopPushSender implements PushSender {

    @Override
    public void send(List<String> deviceTokens, String title, String body, Map<String, String> data) {
        log.info("[Push] sender noop result=sent tokenCount={} title={} data={}", deviceTokens.size(), title, data);
    }
}
