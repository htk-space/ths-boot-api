package ths.boot.api.filter;

import io.jsonwebtoken.Claims;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ths.boot.api.cache.UserTokenCache;
import ths.boot.api.service.LoginCacheInitService;
import ths.boot.api.service.LoginCacheService;
import ths.boot.api.utils.HttpRequestUtil;
import ths.boot.api.utils.JsonUtils;
import ths.boot.api.utils.JwtUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 过滤http请求
 * @author liuzx
 * @version 1.0
 * @date 2022/8/18
 */
public class ThsHttpRequestFilter implements Filter {
    private Logger log = LoggerFactory.getLogger(ThsHttpRequestFilter.class);
    /**
     * ou地址
     */
    private String ouAddr;
    /**
     * 不拦截地址
     */
    private String[] excludePathPatterns;
    private Pattern[] excludePathObjPatterns;
    /**
     * OPTIONS请求标识
     */
    private final static String OPTIONS = "OPTIONS";
    /**
     * 远程用户管理平台返回状态，1加签成功
     */
    private final static String success_status = "1";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 初始化忽略请求Pattern
        if (excludePathPatterns!=null&&excludePathPatterns.length>0) {
            excludePathObjPatterns = new Pattern[excludePathPatterns.length];
            for (int i = 0;i<excludePathPatterns.length;i++) {
                excludePathObjPatterns[i] = Pattern.compile(excludePathPatterns[i],Pattern.CASE_INSENSITIVE);
            }
        }
        log.info("ThsHttpRequestFilter初始化完成");
    }
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        response.setCharacterEncoding("UTF-8");
        response.setHeader("content-type", "text/htm1; charset=UTF-8");
        // 忽略不拦截地址
        if (!checkexcludePathPatterns(request)){
            filterChain.doFilter(request, response);
        }
        String userToken = request.getParameter("userToken");
        String headerUserToken = request.getHeader("userToken");
        if (StringUtils.isEmpty(userToken)&&StringUtils.isEmpty(headerUserToken)) {
            response.getWriter().write("{\"success\":404,\"msg\":\"非法请求\"}");
            return;
        }
        if (StringUtils.isEmpty(userToken)) {
            userToken = headerUserToken;
        }
        //验证userToken
        try {
            String validate = validateLogin(userToken);
            if (StringUtils.isNotEmpty(validate)) {
                response.getWriter().write(validate);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("{\"success\":404,\"msg\":\"非法请求\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }
    @Override
    public void destroy() {

    }

    /**
     * 用户登录验证
     * @param userToken
     * @return
     */
    public String validateLogin(String userToken) throws Exception {
        //本地缓存不存在token则验证token是否有效
        String userTokenValue = UserTokenCache.getUserTokenCache(userToken);
        if (StringUtils.isEmpty(userTokenValue)) {
            //调用OU接口登陆
            String url = ouAddr+"/ouapi/api/login/security/auth/sign?userToken="+userToken;
            String result = HttpRequestUtil.sendGet(url,null,HttpRequestUtil.charset);
            if (StringUtils.isEmpty(result)) {
                //验证token不成功提示客户端
                log.error("用户token验证失败：{}",userToken);
                return "{\"success\":404,\"msg\":\"用户token验证失败\"}";
            }
            Map<String,Object> resultMap = JsonUtils.json2map(result);
            String status = String.valueOf(resultMap.get("status"));
            if (!ThsHttpRequestFilter.success_status.equals(status)) {
                //验证token不成功提示客户端
                return "{\"success\":403,\"msg\":\"用户已注销或userToken不合法\"}";
            } else {
                //登录成功
                LoginCacheInitService.login(userToken);
            }
        }
        return null;
    }
    /**
     * 忽略url请求,true忽略
     * @param request
     * @return
     */
    private boolean checkexcludePathPatterns(HttpServletRequest request){
        if (excludePathObjPatterns!=null&&excludePathObjPatterns.length>0) {
            String uri = request.getRequestURI();
            for (Pattern excludePathObjPattern:excludePathObjPatterns) {
                Matcher m = excludePathObjPattern.matcher(uri);
                if(!m.find()){
                    return true;
                }
            }
        }
        return false;
    }

    public void setOuAddr(String ouAddr) {
        this.ouAddr = ouAddr;
    }

    public void setExcludePathPatterns(String ... excludePathPatterns) {
        this.excludePathPatterns = excludePathPatterns;
    }

}
