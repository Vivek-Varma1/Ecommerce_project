package com.ecommerce.project.security.jwt;

import com.ecommerce.project.security.jwt.service.CustomUserDetailsImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

//import static jdk.internal.org.jline.keymap.KeyMap.key;


@Component
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);


    @Value("${spring.app.jwtSecret}")
    private String jwtSecret;
    @Value("${spring.app.jwtExpirationMs}")
    private Long jwtExpirationMs;
    @Value("${spring.app.jwtCookieName}")
    private String jwtCookie;

//    public String getJwtFromHeader(HttpServletRequest request) {
//        String bearerToken=request.getHeader("Authorization");
//        logger.debug("Authorization header: {}",bearerToken);
//
//        if(bearerToken !=null && bearerToken.startsWith("Bearer ")){
//            return bearerToken.substring(7);//remove bearer prefix
//
//        }
//        return  null;
//    }

    //for coockie based authentication
    public String getJwtFromCookie(HttpServletRequest request){
        Cookie cookie= WebUtils.getCookie(request,jwtCookie);
        if(cookie!=null)
            return cookie.getValue();
        else
            return null;
    }

    public String generateTokenFromUserName(String username){
      //  String username=userDetails.getUsername();
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime()+jwtExpirationMs))
                .signWith(key())
                .compact();
    }

    //for coockie based authentication
    public ResponseCookie genereateCookie(CustomUserDetailsImpl userDetails){
        String jwt=generateTokenFromUserName(userDetails.getUsername());
        ResponseCookie cookie=ResponseCookie.from(jwtCookie,jwt).path("/api").maxAge(24*60*60)
                .httpOnly(false)
                .build();
        return cookie;
    }

    //for  cleaning coockie when signout
    public ResponseCookie getCleanCookie(){
        ResponseCookie cookie=ResponseCookie.from(jwtCookie,null)
                .path("/api")
                .build();
        return cookie;
    }

    public String getUserNameFromJwtToken(String token){
        return Jwts.parser().verifyWith((SecretKey) key()).build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
    private Key key(){
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }




    public boolean validateJwtToken(String authToken){

        try {
            System.out.println("Validate");
            Jwts.parser().verifyWith((SecretKey) key()).build().parseSignedClaims(authToken);
            return true;
        } catch (MalformedJwtException e){
            logger.error("Invalid JWT Token : {}",e.getMessage());
        } catch (ExpiredJwtException e){
            logger.error("JWT Token is Expired : {}",e.getMessage());
        }catch (UnsupportedJwtException e){
            logger.error("JWT Token Unsupported : {}",e.getMessage());
        } catch (IllegalArgumentException e){
            logger.error("JWT Claims String is empty : {}",e.getMessage());
        }
        return false;
    }

}
