package com.example.mojabanka_graficky.security;

import com.example.mojabanka_graficky.model.User;

public class Session {
    private static User current;

    public static void set(User u) { current = u; }
    public static User get() { return current; }
    public static boolean isAdmin() { return current != null && "ADMIN".equals(current.getRole()); }
    public static void clear() { current = null; }
}
