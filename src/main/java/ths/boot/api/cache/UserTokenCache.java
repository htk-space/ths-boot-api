package ths.boot.api.cache;



import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户token缓存
 */
public class UserTokenCache {
	/**
	 * 存储用户userToken缓存，key是userToke
	 */
	private static Map<String,String> userTokenCache = new ConcurrentHashMap<String, String>();

	/**
	 * 根据userToken key获取缓存数据
	 * @param key userToken
	 * @return
	 */
	public static String getUserTokenCache(String key) {
		if (StringUtils.isEmpty(key)) {
			return null;
		}
		String value = userTokenCache.get(key);
		return value;
	}

	/**
	 * 清空指定KEY用户缓存
	 * @param key userToken
	 */
	public static void clearUserTokenCacheByKey(String key) {
		String value = getUserTokenCache(key);
		if (StringUtils.isNotEmpty(value)) {
			userTokenCache.remove(key);
		}
	}

	/**
	 * 存储用户userToken
	 * @param key userToken
	 * @param value userToken
	 */
	public static void putUserTokenCache(String key,String value) {
		userTokenCache.put(key, value);
	}

}
