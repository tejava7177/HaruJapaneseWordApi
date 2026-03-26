package com.haru.api.push;

import java.util.List;
import java.util.Map;

public interface PushSender {

    void send(List<String> deviceTokens, String title, String body, Map<String, String> data);
}
