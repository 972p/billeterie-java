package utils;

import models.Client;

public class SessionManager {
    private static Client currentUser;

    public static Client getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(Client user) {
        currentUser = user;
    }

    public static void clearSession() {
        currentUser = null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static boolean isAdmin() {
        return currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole());
    }

    public static boolean isUser() {
        return currentUser != null && "USER".equalsIgnoreCase(currentUser.getRole());
    }
}
