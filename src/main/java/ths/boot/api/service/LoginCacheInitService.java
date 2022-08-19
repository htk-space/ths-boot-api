package ths.boot.api.service;

import io.jsonwebtoken.Claims;
import ths.boot.api.cache.UserTokenCache;
import ths.boot.api.filter.ThsHttpRequestFilter;
import ths.boot.api.utils.JwtUtils;

import java.util.ServiceLoader;

/**
 * 初始化LoginCacheService实现类
 * @author liuzx
 * @version 1.0
 * @date 2022/8/18
 */
public class LoginCacheInitService {
    /**
     * 登录注销实现类集合
     */
    public static ServiceLoader<LoginCacheService> cacheService;
    static {
        // 初始化登录注销实现类
        cacheService = ServiceLoader.load(LoginCacheService.class);
    }

    /**
     * 登录
     * @param userToken
     */
    public static void login(String userToken){
        //存储userToken到本地缓存
        UserTokenCache.putUserTokenCache(userToken,userToken);
        Claims claims = JwtUtils.getClaimsFromToken(userToken, null);
        //通知第三方接口登录成功
        for (LoginCacheService item : cacheService) {
            item.loginInfoCache(userToken,String.valueOf(claims.get("loginName")));
        }
    }
    /**
     * 注销
     * @param userToken
     */
    public static void logout(String userToken){
        //清空本地缓存用户token
        UserTokenCache.clearUserTokenCacheByKey(userToken);
        //通知第三方实现接口用户注销
        if (cacheService != null) {
            Claims claims = JwtUtils.getClaimsFromToken(userToken, null);
            for (LoginCacheService item : cacheService) {
                item.clearLoginCache(userToken,String.valueOf(claims.get("loginName")));
            }
        }
    }
}
