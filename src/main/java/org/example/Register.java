package org.example;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "Register", value = "/register")
public class Register extends HttpServlet {

    // 数据库连接参数

    private static final String DB_URL = "jdbc:oracle:thin:@//localhost:1521/xe";
    private static final String DB_USERNAME = "system";
    private static final String DB_PASSWORD = "oracle";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("register.html").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        try {
            // 加载 Oracle JDBC 驱动
            Class.forName("oracle.jdbc.OracleDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Oracle JDBC Driver not found.");
            return;
        }
        // 获取表单数据
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirm_password");

        // 校验密码是否一致
        if (!password.equals(confirmPassword)) {
            request.setAttribute("error", "密码和确认密码不一致");
            request.getRequestDispatcher("register.html").forward(request, response);
            return;
        }

        // 校验邮箱格式
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            request.setAttribute("error", "邮箱格式不正确");
            request.getRequestDispatcher("register.html").forward(request, response);
            return;
        }

        // 插入数据库
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            String sql = "INSERT INTO users (NAME, EMAIL, PASSWORD) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, name);
                stmt.setString(2, email);
                stmt.setString(3, password);

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    response.sendRedirect("login");
                } else {
                    request.setAttribute("error", "注册失败，请稍后再试");
                    request.getRequestDispatcher("register.html").forward(request, response);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("error", "数据库错误，请稍后再试");
            request.getRequestDispatcher("register.html").forward(request, response);
        }
    }
}
