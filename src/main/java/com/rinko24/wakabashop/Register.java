package com.rinko24.wakabashop;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "Register", value = "/register")
public class Register extends HttpServlet {

    private static final String DB_URL = "jdbc:oracle:thin:@//localhost:1521/xe";
    private static final String DB_USERNAME = "system";
    private static final String DB_PASSWORD = "oracle";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("register.html").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // 获取表单数据
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirm_password");

        // **校验用户名格式**
        if (!name.matches("^[a-zA-Z0-9_]+$")) {
            sendAlert(response, "用户名只能包含字母、数字和下划线！");
            return;
        }

        // **校验密码是否一致**
        if (!password.equals(confirmPassword)) {
            sendAlert(response, "两次输入的密码不一致！");
            return;
        }

        // **校验邮箱格式**
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            sendAlert(response, "邮箱格式不正确！");
            return;
        }

        try {
            // **加载 JDBC 驱动**
            Class.forName("oracle.jdbc.OracleDriver");

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {

                // **查找是否有重复用户**
                if (isUserExists(conn, name)) {
                    sendAlert(response, "该用户已被注册！");
                    return;
                }

                // **插入数据库**
                String sql = "INSERT INTO users (NAME, EMAIL, PASSWORD) VALUES (?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, name);
                    stmt.setString(2, email);
                    stmt.setString(3, password);

                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        response.sendRedirect("login");
                    } else {
                        sendAlert(response, "注册失败，请稍后再试！");
                    }
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            sendAlert(response, "数据库错误，请稍后再试！");
        }
    }

    // **检查用户是否已存在**
    private boolean isUserExists(Connection conn, String name) {
        String query = "SELECT 1 FROM users WHERE name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // **如果查询有结果，说明用户已存在**
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // **查询失败，返回 false**
        }
    }

    // **封装 alert 提示**
    private void sendAlert(HttpServletResponse response, String message) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println("<script type='text/javascript'>");
        out.println("alert('" + message + "');");
        out.println("window.history.back();");
        out.println("</script>");
    }
}
