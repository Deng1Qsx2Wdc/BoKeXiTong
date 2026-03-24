package com.example.demo.common;

public class UserContext {
    private static final ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    public  static void setThreadLocal(Long value) {

        threadLocal.set(value);
    }
    public static Long getThreadLocal() {

        return threadLocal.get();
    }
    public static void removeThreadLocal() {

        threadLocal.remove();
    }
}
