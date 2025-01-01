package com.oauth_login.oauth_login.authentication.security.DoSPrevention;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

@Service
public class RateLimitingService {
    
    private Map<String, Integer> requestCounts = new ConcurrentHashMap<>();

    private final int MAX_REQUESTS_PER_MINUTE = 60;

    public RateLimitingService() {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            requestCounts.clear();
        }, 1, 1, TimeUnit.MINUTES);
    }

    public boolean isRequestAllowed(String username) {
        requestCounts.merge(username, 1, Integer::sum);

        return requestCounts.get(username) <= MAX_REQUESTS_PER_MINUTE;
    }
}
