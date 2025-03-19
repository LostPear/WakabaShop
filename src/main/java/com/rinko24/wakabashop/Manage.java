package com.rinko24.wakabashop;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/manage")
public class Manage extends HttpServlet {
    private static final int PAGE_SIZE = 5;  // 每页显示5条

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");

        // 1. 获取当前页码参数（默认为1）
        String pageParam = request.getParameter("page");
        int page = 1;
        if (pageParam != null) {
            try {
                page = Integer.parseInt(pageParam);
            } catch (NumberFormatException e) {
                page = 1;
            }
            if (page < 1) {
                page = 1;
            }
        }

        // 2. 连接数据库并执行分页查询
        // 数据库连接参数
        String dbURL = "jdbc:oracle:thin:@//localhost:1521/xe";
        String dbUser = "system";
        String dbPass = "oracle";

            try {
                Class.forName("oracle.jdbc.driver.OracleDriver");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("无法加载 Oracle JDBC 驱动", e);
            }


        // 定义分页查询SQL：
        // 使用子查询和 ROWNUM 实现 Oracle 11g 的分页。
        // 内层子查询按 name 排序并取前 page*5 条，外层查询再过滤掉前 (page-1)*5 条，剩下当前页记录。
        String sql =
                "SELECT name, email FROM (" +
                        "    SELECT name, email, ROWNUM AS rn FROM (" +
                        "        SELECT name, email FROM users ORDER BY name" +
                        "    ) WHERE ROWNUM <= ?" +
                        ") WHERE rn > ?";

        // 计算 ROWNUM 上限和下限
        int upper = page * PAGE_SIZE;
        int lower = (page - 1) * PAGE_SIZE;

        try (Connection conn = DriverManager.getConnection(dbURL, dbUser, dbPass);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            // 加载Oracle驱动（可选，新版JDBC可自动加载）
            Class.forName("oracle.jdbc.driver.OracleDriver");
            // 设置参数并执行查询
            stmt.setInt(1, upper);
            stmt.setInt(2, lower);
            ResultSet rs = stmt.executeQuery();

            // 额外查询总记录数以确定是否有下一页（用于分页导航）
            int totalRecords = 0;
            try (Statement countStmt = conn.createStatement();
                 ResultSet countRs = countStmt.executeQuery("SELECT COUNT(*) FROM users")) {
                if (countRs.next()) {
                    totalRecords = countRs.getInt(1);
                }
            }
            int totalPages = (int) Math.ceil(totalRecords / (double) PAGE_SIZE);
            boolean hasPrev = page > 1;
            boolean hasNext = page < totalPages;

            // 3. 生成 HTML 页面输出
            PrintWriter out = response.getWriter();
            out.println("<!DOCTYPE html>");
            out.println("<html lang='zh-CN'>");
            out.println("<head>");
            out.println("  <meta charset='UTF-8' />");
            out.println("  <title>用户管理</title>");
            // 引入 Tailwind CSS（使用 CDN 引入脚本方式）
            out.println("  <script src='https://cdn.tailwindcss.com'></script>");
            out.println("</head>");
            out.println("<body class='bg-gray-100'>");
            out.println("  <div class='max-w-4xl mx-auto mt-12 p-6 bg-white rounded-lg shadow'>");
            out.println("    <h2 class='text-2xl font-semibold mb-4'>用户管理</h2>");
            out.println("    <table class='w-full text-left border-collapse'>");
            out.println("      <thead class='bg-gray-50 border-b'>");
            out.println("        <tr>");
            out.println("          <th class='px-4 py-2 text-gray-600 font-medium'>姓名</th>");
            out.println("          <th class='px-4 py-2 text-gray-600 font-medium'>邮箱</th>");
            out.println("        </tr>");
            out.println("      </thead>");
            out.println("      <tbody class='divide-y divide-gray-100'>");

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                String name = rs.getString("name");
                String email = rs.getString("email");
                out.println("        <tr>");
                out.println("          <td class='px-4 py-2'>" + name + "</td>");
                out.println("          <td class='px-4 py-2'>" + email + "</td>");
                out.println("        </tr>");
            }
            if (!hasData) {
                // 当前页无数据时的提示（例如超出页数范围或表为空）
                out.println("        <tr>");
                out.println("          <td colspan='2' class='px-4 py-2 text-center text-gray-500'>没有用户数据。</td>");
                out.println("        </tr>");
            }

            out.println("      </tbody>");
            out.println("    </table>");

            // 分页按钮导航
            out.println("    <div class='mt-4 flex justify-between'>");
            if (hasPrev) {
                out.println("      <a href='?page=" + (page - 1) + "' " +
                        "class='px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600'>上一页</a>");
            } else {
                // 如果没有上一页，用空白元素占位使布局对齐
                out.println("      <span></span>");
            }
            if (hasNext) {
                out.println("      <a href='?page=" + (page + 1) + "' " +
                        "class='px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600'>下一页</a>");
            } else {
                out.println("      <span></span>");
            }
            out.println("    </div>");

            out.println("  </div>");
            out.println("</body>");
            out.println("</html>");
        } catch (ClassNotFoundException | SQLException e) {
            throw new ServletException(e);
        }
    }
}
