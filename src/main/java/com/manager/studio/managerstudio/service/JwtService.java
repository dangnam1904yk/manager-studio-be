package com.manager.studio.managerstudio.service;

import com.manager.studio.managerstudio.exception.BusinessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${security.jwt.expiration}")
    private long jwtExpiration;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver resolver;

    @PostConstruct
    public void init() throws Exception {
        this.privateKey = loadPrivateKey("certs/private_key.pem");
        this.publicKey = loadPublicKey("certs/public_key.pem");
    }

    private PrivateKey loadPrivateKey(String path) throws Exception {
        // 1. Đọc toàn bộ nội dung file
        String key = new String(StreamUtils.copyToByteArray(new ClassPathResource(path).getInputStream()));

        // 2. Dọn dẹp sạch sẽ bằng Regex
        String privateKeyPEM = key
                // Loại bỏ header: -----BEGIN PRIVATE KEY-----
                .replaceAll("-----BEGIN (.*)-----", "")
                // Loại bỏ footer: -----END PRIVATE KEY-----
                .replaceAll("-----END (.*)-----", "")
                // Loại bỏ tất cả ký tự xuống dòng, khoảng trắng, tab...
                .replaceAll("\\s+", "");

        // 3. Giải mã
        byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(encoded));
    }

    private PublicKey loadPublicKey(String path) throws Exception {
        // 1. Đọc nội dung file từ Resources
        String key = new String(StreamUtils.copyToByteArray(new ClassPathResource(path).getInputStream()));

        // 2. Dùng Regex để xóa header, footer và TẤT CẢ khoảng trắng/xuống dòng
        String publicKeyPEM = key
                .replaceAll("-----BEGIN (.*)-----", "")
                .replaceAll("-----END (.*)-----", "")
                .replaceAll("\\s+", ""); // \s+ sẽ xóa sạch space, tab, \n, \r

        // 3. Giải mã Base64
        byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);

        // 4. Tạo đối tượng PublicKey (Dùng X509EncodedKeySpec cho Public Key)
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(new X509EncodedKeySpec(encoded));
    }


    // Tạo Access Token (15 phút)
    public String generateToken(UserDetails userDetails, Long jwtExpiration) {
        return buildToken(new HashMap<>(), userDetails, jwtExpiration);
    }

    // Tạo Refresh Token (7 ngày)
    public String generateRefreshToken(UserDetails userDetails, Long jwtExpiration) {
        return buildToken(new HashMap<>(), userDetails, jwtExpiration);
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        String jti = UUID.randomUUID().toString();
        return Jwts.builder()
                .setClaims(extraClaims)
                .setId(jti)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                // Sử dụng privateKey và thuật toán RS512
                .signWith(privateKey, SignatureAlgorithm.RS512)
                .compact();
    }

    // 1. Trích xuất Username từ token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // 2. Trích xuất một thông tin bất kỳ (Claim)
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(publicKey) // Dùng Public Key để verify RS512
                .build()
                .parseClaimsJws(token)
                .getBody();
    }



    // 4. Kiểm tra Token có hợp lệ không
//    public boolean isTokenValid(String token, UserDetails userDetails) {
//        final String username = extractUsername(token);
//        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
//    }
    public boolean isTokenValid(String token, UserDetails userDetails) throws BusinessException {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
        } catch (ExpiredJwtException e) {
            // Ném BusinessException với mã 401
            throw new BusinessException("Phiên đăng nhập đã hết hạn", HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            throw new BusinessException("Token không hợp lệ", HttpStatus.UNAUTHORIZED);
        }
    }

    public String extractJti(String token) {
        return extractClaim(token, Claims::getId);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

}