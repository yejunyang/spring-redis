package com.yejy.springredis.log;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author yejunyang2012@163.com
 * @date 2021/9/21 19:16
 **/
@Slf4j
@WebFilter(filterName = "logFilter" , urlPatterns = "/*")
public class LogFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("logFilter init");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        MyReaderHttpServletRequestWrapper myReaderHttpServletRequestWrapper = new MyReaderHttpServletRequestWrapper(request);
        filterChain.doFilter(myReaderHttpServletRequestWrapper,response);
    }
}
