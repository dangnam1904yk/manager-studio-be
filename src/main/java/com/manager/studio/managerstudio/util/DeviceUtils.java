package com.manager.studio.managerstudio.util;

import jakarta.servlet.http.HttpServletRequest;

public class DeviceUtils {
    public static boolean isMobile(HttpServletRequest request) {
        // 1. Ưu tiên kiểm tra Header tùy chỉnh (Độ chính xác 100% nếu Client tuân thủ)
        String clientType = request.getHeader("X-Client-Type");
        if (clientType != null) {
            return clientType.equalsIgnoreCase("MOBILE") ||
                    clientType.equalsIgnoreCase("APP") ||
                    clientType.equalsIgnoreCase("IOS") ||
                    clientType.equalsIgnoreCase("ANDROID");
        }

        // 2. Kiểm tra Header X-Requested-With (Thường có trong Android Webview)
        String requestedWith = request.getHeader("X-Requested-With");
        if (requestedWith != null && requestedWith.contains("com.")) {
            return true;
        }

        // 3. Kiểm tra User-Agent (Dùng để "đoán" khi không có Header trên)
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) return false;

        String uaLow = userAgent.toLowerCase();
        return uaLow.contains("android") ||
                uaLow.contains("iphone") ||
                uaLow.contains("ipad") ||
                uaLow.contains("mobile"); // Thêm "mobile" để bắt các dòng máy khác
    }
}
