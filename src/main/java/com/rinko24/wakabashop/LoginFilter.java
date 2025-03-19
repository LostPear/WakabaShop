package com.rinko24.wakabashop;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter(filterName = "LoginFilter", urlPatterns = {"/profile.html", "/profile"})
public class LoginFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);

        // 检查用户是否已登录
        if (session == null || session.getAttribute("username") == null) {
            httpResponse.sendRedirect("login"); // 未登录，重定向到 /login
            return; // 重要！阻止继续执行
        }

        // 继续处理请求
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}


