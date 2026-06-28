package com.very.relink.notification.domain.model;

public enum PushProvider {
    CHROME,
    SAFARI,
    EDGE,
    FIREFOX,
    UNKNOWN;

    public static PushProvider fromUserAgent(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return UNKNOWN;
        }

        String normalized = userAgent.toLowerCase();
        if (normalized.contains("edg/")) {
            return EDGE;
        }
        if (normalized.contains("firefox/")) {
            return FIREFOX;
        }
        if (normalized.contains("safari/") && !normalized.contains("chrome/") && !normalized.contains("chromium/")) {
            return SAFARI;
        }
        if (normalized.contains("chrome/") || normalized.contains("chromium/")) {
            return CHROME;
        }

        return UNKNOWN;
    }
}
