package org.example;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
        System.out.println("Login 收到 POST 请求");
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        System.out.println("接收到的邮箱：" + email);
        System.out.println("接收到的密码：" + password);

        if (authenticateUser(email, password)) {
            response.sendRedirect("login_success");
        } else {
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println("<script type='text/javascript'>");
            out.println("alert('登录失败，请检查邮箱和密码！');"); // 弹窗提示
            out.println("window.history.back();"); // 返回上一页
            out.println("</script>");
        }
    }

    private boolean authenticateUser(String email, String password) {
        boolean isValidUser = false;
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE email = ? AND password = ?")) {
                stmt.setString(1, email);
                stmt.setString(2, password);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        isValidUser = true;
                    }
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return isValidUser;
    }
}