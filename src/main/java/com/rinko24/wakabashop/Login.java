package com.rinko24.wakabashop;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet(name = "Login", value = "/login")
public class Login extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("Login 收到 GET 请求");
        request.getRequestDispatcher("login.html").forward(request, response);
    }

    private static final String URL = "jdbc:oracle:thin:@//localhost:1521/xe";
    private static final String USER = "system";
    private static final String PASSWORD = "oracle";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        String username = authenticateUser(email, password);

        if (username != null) {
            // 1. 创建 session 记录用户信息
            HttpSession session = request.getSession();
            session.setAttribute("username", username);
            session.setAttribute("email", email);

            // 2. 登录成功，跳转到个人页面
            response.sendRedirect("profile");
        } else {
            // 3. 登录失败，返回错误提示
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println("<script type='text/javascript'>");
            out.println("alert('登录失败，请检查邮箱和密码！');"); // 弹窗提示
            out.println("window.history.back();"); // 返回上一页
            out.println("</script>");
        }
    }

    private String authenticateUser(String email, String password) {
        String username = null;
        String sql = "SELECT name FROM users WHERE email = ? AND password = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    username = rs.getString("name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return username;
    }
}