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
 * 过滤cas注销请求
 * @author liuzx
 * @version 1.0
 * @date 2022/8/18
 */
public class ThsCasLogoutFilter implements Filter {
    private Logger log = LoggerFactory.getLogger(ThsCasLogoutFilter.class);
    /**
     * OPTIONS请求标识
     */
    private final static String OPTIONS = "OPTIONS";
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("ThsHttpRequestFilter初始化完成");
    }
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        //response.setContentType("text/html; charset=GBK");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, x-requested-with, X-Custom-Header, Authorization,userToken");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
        // 忽略OPTIONS请求校验
        if(OPTIONS.equalsIgnoreCase(request.getMethod())) {
            return;
        }
        String userToken = request.getParameter("userToken");
        //注销标识
        String casLogoutFlag = request.getParameter("casLogoutFlag");
        //注销
        if (StringUtils.isNotEmpty(userToken)&&StringUtils.isNotEmpty(casLogoutFlag)) {
            LoginCacheInitService.logout(userToken);
            return;
        }
        filterChain.doFilter(request, response);
    }
    @Override
    public void destroy() {

    }
}
