package org.example;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/profile")
public class Profile extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect("login");
            return;
        }

        String username = (String) session.getAttribute("username");

        // 将用户名存入 HTML5 SessionStorage（前端可用）
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(
                "<script>sessionStorage.setItem('username', '" + username + "'); " +
                "window.location.href='profile.html';</script>"
        );
    }
}
