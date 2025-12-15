package com.nhnacademy._vidiabookstoreservice.global.common;

// 1. Context 클래스
public class UserContext {
    private static final ThreadLocal<UserContext> context = new ThreadLocal<>();

    private Long userId;
    private Long guestId;
    private String userRole;

    private UserContext(Long userId, Long guestId, String userRole) {
        this.userId = userId;
        this.guestId = guestId;
        this.userRole = userRole;
    }

    public static void set(Long userId, Long guestId, String userRole) {
        context.set(new UserContext(userId, guestId, userRole));
    }

    public static UserContext get() {
        return context.get();
    }

    public static void clear() {
        context.remove();
    }

    public Long getUserId() { return userId; }
    public Long getGuestId() { return guestId; }
    public String getUserRole() { return userRole; }
}
