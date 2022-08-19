package ths.boot.api.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtils {
    private static Logger log = LoggerFactory.getLogger(JwtUtils.class);

    /**
     * 通用偏移量
     */
    private static final String  DEFAULTSECRET = "solutionsolution";

    /**
     * 创建jwt
     * @param claims 载体
     * @param secret 私钥
     * @param exp 过期时间,毫秒
     * @return
     */
    public static String createJwtToken(HashMap<String,Object> claims, String secret, long exp){
        if(StringUtils.isEmpty(secret)){
            secret = JwtUtils.DEFAULTSECRET;
        }
        //当前时间+过期时间单位为毫秒
        exp = System.currentTimeMillis() + exp;
        Map headerMap = new HashMap<String,String>();
        headerMap.put("alg", "HS256");
        headerMap.put("typ", "JWT");

        JwtBuilder builder= Jwts.builder()
                .setHeader(headerMap)
                .setClaims(claims)
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS256,secret)  // 支持的算法详见：https://github.com/jwtk/jjwt#features
                .setExpiration(new Date(exp));//设置过期时间

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        System.out.println(sdf.format(exp));

        return builder.compact();
    }

    /**
     * 判断token是否非法
     * @param token token
     * @return 未过期返回true，否则返回false
     */
    public static Boolean validateToken(String token,String secret) {
        if(StringUtils.isEmpty(secret)){
            secret = JwtUtils.DEFAULTSECRET;
        }
        return !isTokenExpired(token,secret);
    }

    /**
     * 从token中获取claim
     * @param token token
     * @return claim
     */
    public static Claims getClaimsFromToken(String token,String secret) {
        try {
            if(StringUtils.isEmpty(secret)){
                secret = JwtUtils.DEFAULTSECRET;
            }
            return Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("token解析失败");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取token的过期时间
     * @param token token
     * @return 过期时间
     */
    private static Date getExpirationDateFromToken(String token,String secret) {
        Date expiration = null;
        if(StringUtils.isEmpty(secret)){
            secret = JwtUtils.DEFAULTSECRET;
        }
        Claims claims = getClaimsFromToken(token,secret);
        if(claims!=null){
            expiration = claims.getExpiration();
        }
        return expiration;
    }

    /**
     * 判断token是否过期
     *
     * @param token token
     * @return 已过期返回true，未过期返回false
     */
    private static Boolean isTokenExpired(String token,String secret) {
        Boolean isTokenExpired = true;
        if(StringUtils.isEmpty(secret)){
            secret = JwtUtils.DEFAULTSECRET;
        }
        Date expiration = getExpirationDateFromToken(token,secret);
        if(expiration!=null){
            isTokenExpired = expiration.before(new Date());
        }
       return isTokenExpired;
    }

    public static void main(String[] args) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

        System.out.println(sdf.format(System.currentTimeMillis()));
        System.out.println(sdf.format(System.currentTimeMillis()+6000000));

        Claims claims = JwtUtils.getClaimsFromToken("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhcHBUb2tlbiI6bnVsbCwibG9naW5OYW1lIjoiYWRtaW4iLCJ1c2VyTmFtZSI6Iui2hee6p-euoeeQhuWRmCIsImV4cCI6OTIyMzM3MjAzNjg1NDY3NSwidXNlcklkIjoiYWRtaW4iLCJpYXQiOjE2NDcyMzYyMDB9.H2cNLD92CUfMvSRK1VhIOjbX6H4KKjUK05Lu6PNqV0g","");
        String securityKey = String.valueOf(claims.get("userName"));
        System.out.println(securityKey);
    }
}