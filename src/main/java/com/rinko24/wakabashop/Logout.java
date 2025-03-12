package com.rinko24.wakabashop;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "Logout", value = "/logout")
public class Logout extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 获取当前 session，如果不存在，则返回 null
        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate(); // 让 session 失效
        }

        // 设置响应类型
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        // JavaScript 提示退出成功，并跳转回登录页面
        out.println("<script type='text/javascript'>");
        out.println("alert('您已成功退出！');");
        out.println("window.location.href = 'login';"); // 跳转回登录页面
        out.println("</script>");
    }
}