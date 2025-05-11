package com.business.business.config;

import com.business.business.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service//Dont forget to add this
public class JwtService {

    @Value("${jwt.security.access-token-expiry}")
    private long jwtAuthTokenExpiration;
    @Value("${jwt.security.refresh-token-expiry}")
    private long refreshTokenExpiration;

    @Value("${jwt.security.secret-key}")
    private String SECRETE_KEY;


    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String generateAccessToken(UserDetails userDetails){
        Map<String, Object> claims = new HashMap<>();
        String role = ((User) userDetails).getRole().toString();
        claims.put("role", role);
        return generateToken(claims, userDetails, jwtAuthTokenExpiration);
    }
    public String generateRefreshToken(UserDetails userDetails){
        return generateToken(new HashMap<>(), userDetails, jwtAuthTokenExpiration);
    }

    public String generateToken(
            Map<String, Object>  extractClaims,
            UserDetails userDetails,
            long expiration
    ){
//        Map<String, Object> claims = new HashMap<>();
//        return createToken(claims, userDetails.getUsername());
        /*return Jwts.builder()
                .setClaims(extractClaims)
                .setSubject(userDetails.getUsername())//username in this case is the user's email
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000*60*24)) //ie refreshToke is valid for 24 hours + 1000ms
                .signWith(getSignInKey(), SignatureAlgorithm.ES256)
                .compact();*/

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .signWith(getSignInKey())
                .claims(extractClaims)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();

//        return Jwts.builder().setClaims(extractClaims).setSubject(userDetails.getUsername())
//                .setIssuedAt(new Date(System.currentTimeMillis()))
//                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
//                .signWith(SignatureAlgorithm.HS256, SECRETE_KEY).compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails){
        //we need userdetails to check if the refreshToke is for the userDetails
        final String username = extractUsername(token);
        if(
                username.equals(userDetails.getUsername())
                && !isTokenExpired(token)
        ){
            return true;
        }
        return false;
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token){
        return Jwts
                .parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRETE_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
//        return NoAuy
    }
}
