package com.manager.studio.managerstudio.request.auth;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthenticationResponse {

    String token;

    String refreshToken;
}
