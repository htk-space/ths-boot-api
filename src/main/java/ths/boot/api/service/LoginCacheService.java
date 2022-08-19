package ths.boot.api.service;


/**
 * 用户信息缓存操作
 * 第三方如有内置用户存储和注销的自定义功能，可以实现该接口
 */

public interface LoginCacheService{
    /**
     * 用户token验证操作成功后续获取用户操作
     * @param userToken 用户token
     * @param loginName 登录名
     */
    void loginInfoCache(String userToken,String loginName);

    /**
     * 清空用户登录信息
     * @param userToken
     * @param loginName 登录名
     */
    void clearLoginCache(String userToken,String loginName);
}
