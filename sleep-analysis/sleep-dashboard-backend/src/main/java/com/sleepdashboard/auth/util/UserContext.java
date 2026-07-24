package com.sleepdashboard.auth.util;

import java.util.HashMap;
import java.util.Map;

public class UserContext {

    private static final ThreadLocal<Map<String, Object>> threadLocal = ThreadLocal.withInitial(HashMap::new);

    public static void setUserId(Long userId) {
        threadLocal.get().put("userId", userId);
    }

    public static Long getUserId() {
        return (Long) threadLocal.get().get("userId");
    }

    public static void setUsername(String username) {
        threadLocal.get().put("username", username);
    }

    public static String getUsername() {
        return (String) threadLocal.get().get("username");
    }

    public static void clear() {
        threadLocal.remove();
    }
}
