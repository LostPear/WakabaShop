package com.rinko24.wakabashop;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@WebServlet("/changepassword")
public class ChangePassword extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        response.setContentType("text/html;charset=UTF-8");

        if (session == null || session.getAttribute("username") == null) {
            System.out.println("No session found");
            response.sendRedirect("login.html");
            return;
        }

        String username = (String) session.getAttribute("username");
        String newPassword = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        if (newPassword == null || confirmPassword == null || !newPassword.equals(confirmPassword)) {
            System.out.println("Password is not same");
            response.getWriter().write("<script>alert('密码不一致，请重新输入'); window.history.back();</script>");
            return;
        }

        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "UPDATE users SET password = ? WHERE name = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, newPassword);
                pstmt.setString(2, username);
                int updated = pstmt.executeUpdate();


                if (updated > 0) {
                    System.out.println("Change Password Success");
                    response.getWriter().write("<script>alert('密码修改成功'); window.location.href='profile';</script>");
                } else {
                    System.out.println("Change Password Failed");
                    response.getWriter().write("<script>alert('修改失败，请重试'); window.history.back();</script>");
                }
            }
        } catch (SQLException e) {
            throw new ServletException("数据库更新错误", e);
        }
    }
}
