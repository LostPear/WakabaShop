package com.rinko24.wakabashop;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter(filterName = "ProfileFilter", urlPatterns = {"/login", "/login.html"})
public class ProfileFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);

        // 如果用户已登录，则重定向到 profile
        if (session != null && session.getAttribute("username") != null) {
            httpResponse.sendRedirect("profile");
            return; // 重要！阻止继续执行
        }

        // 用户未登录，允许访问 login 页面
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}

