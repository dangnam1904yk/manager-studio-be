package com.manager.studio.managerstudio.util;

public class Constants {
    public static final String PREFIX_API_PUBLIC ="/api/v1/public";
    public static final String PREFIX_API_PRIVATE ="/api/v1/private";

    public static final String[] WHITE_LIST_URL = {
            PREFIX_API_PUBLIC + "/auth/**",
            PREFIX_API_PUBLIC+"/driver/**",
            "/v2/api-docs",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };
}
