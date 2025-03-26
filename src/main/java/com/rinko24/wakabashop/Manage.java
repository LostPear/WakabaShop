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
    private static final int PAGE_SIZE = 5;
    private static final String DB_URL = "jdbc:oracle:thin:@//localhost:1521/xe";
    private static final String DB_USER = "system";
    private static final String DB_PASS = "oracle";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");

        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("无法加载 Oracle JDBC 驱动", e);
        }

        String action = request.getParameter("action");
        if ("delete".equals(action)) {
            handleDeleteUser(request, response);
            return;
        }

        handleListUsers(request, response);
    }

    private void handleListUsers(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int page = 1;
        String pageParam = request.getParameter("page");
        String searchName = request.getParameter("search");

        if (pageParam != null) {
            try {
                page = Integer.parseInt(pageParam);
                page = Math.max(1, page);
            } catch (NumberFormatException e) {
                page = 1;
            }
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            // Modify SQL to support search functionality
            String sql = "SELECT name, email FROM (" +
                    "    SELECT name, email, ROWNUM AS rn FROM (" +
                    "        SELECT name, email FROM users " +
                    (searchName != null && !searchName.trim().isEmpty()
                            ? "WHERE LOWER(name) LIKE LOWER(?)"
                            : "") +
                    "        ORDER BY name" +
                    "    ) WHERE ROWNUM <= ?" +
                    ") WHERE rn > ?";

            int upper = page * PAGE_SIZE;
            int lower = (page - 1) * PAGE_SIZE;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int paramIndex = 1;
                if (searchName != null && !searchName.trim().isEmpty()) {
                    stmt.setString(paramIndex++, "%" + searchName.trim() + "%");
                }
                stmt.setInt(paramIndex++, upper);
                stmt.setInt(paramIndex, lower);

                try (ResultSet rs = stmt.executeQuery()) {
                    int totalRecords = getTotalRecords(conn, searchName);
                    int totalPages = (int) Math.ceil(totalRecords / (double) PAGE_SIZE);
                    boolean hasPrev = page > 1;
                    boolean hasNext = page < totalPages;

                    // 生成HTML页面
                    generateUserListHtml(response, rs, page, hasPrev, hasNext, searchName);
                }
            }
        } catch (SQLException e) {
            throw new ServletException("数据库错误", e);
        }
    }

    private int getTotalRecords(Connection conn, String searchName) throws SQLException {
        String countSql = "SELECT COUNT(*) FROM users " +
                (searchName != null && !searchName.trim().isEmpty()
                        ? "WHERE LOWER(name) LIKE LOWER(?)"
                        : "");

        try (PreparedStatement countStmt = conn.prepareStatement(countSql)) {
            if (searchName != null && !searchName.trim().isEmpty()) {
                countStmt.setString(1, "%" + searchName.trim() + "%");
            }

            try (ResultSet countRs = countStmt.executeQuery()) {
                return countRs.next() ? countRs.getInt(1) : 0;
            }
        }
    }

    private void handleDeleteUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String userName = request.getParameter("name");
        if (userName == null || userName.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "未指定用户名");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String deleteSql = "DELETE FROM users WHERE name = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
                stmt.setString(1, userName);
                int affectedRows = stmt.executeUpdate();

                if (affectedRows > 0) {
                    // 关键：使用 request.getContextPath() 拼接 URL
                    response.sendRedirect(request.getContextPath() + "/manage");
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "未找到指定用户");
                }
            }
        } catch (SQLException e) {
            throw new ServletException("删除用户失败", e);
        }
    }

    private void generateUserListHtml(HttpServletResponse response, ResultSet rs,
                                      int currentPage, boolean hasPrev, boolean hasNext,
                                      String searchName)
            throws IOException, SQLException {
        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html lang='zh-CN'>");
        out.println("<head>");
        out.println("  <meta charset='UTF-8' />");
        out.println("  <title>用户管理</title>");
        out.println("  <script src='https://cdn.tailwindcss.com'></script>");
        out.println("  <script>");
        out.println("    function confirmDelete(name) {");
        out.println("      if (confirm('确定要删除用户 ' + name + ' 吗？')) {");
        out.println("        fetch('manage?action=delete&name=' + encodeURIComponent(name), {");
        out.println("          method: 'GET'");
        out.println("        })");
        out.println("        .then(response => {");
        out.println("          if (response.ok) {");
        out.println("            window.location.reload();");
        out.println("          } else {");
        out.println("            alert('删除用户失败');");
        out.println("          }");
        out.println("        })");
        out.println("        .catch(error => {");
        out.println("          console.error('删除错误:', error);");
        out.println("          alert('删除过程中发生错误');");
        out.println("        });");
        out.println("      }");
        out.println("    }");
        out.println("  </script>");
        out.println("</head>");
        out.println("<body class='bg-gray-100'>");
        out.println("  <div class='max-w-4xl mx-auto mt-12 p-6 bg-white rounded-lg shadow'>");
        out.println("    <h2 class='text-2xl font-semibold mb-4'>用户管理</h2>");

        // 搜索表单
        out.println("    <form action='manage' method='get' class='mb-4 flex'>");
        out.println("      <input type='text' name='search' placeholder='按用户名搜索' " +
                "value='" + (searchName != null ? searchName : "") + "' " +
                "class='flex-grow px-3 py-2 border rounded-l-md focus:outline-none focus:ring-2 focus:ring-blue-500'>");
        out.println("      <button type='submit' class='bg-blue-500 text-white px-4 py-2 rounded-r-md hover:bg-blue-600'>搜索</button>");
        out.println("    </form>");

        out.println("    <table class='w-full text-left border-collapse'>");
        out.println("      <thead class='bg-gray-50 border-b'>");
        out.println("        <tr>");
        out.println("          <th class='px-4 py-2 text-gray-600 font-medium'>姓名</th>");
        out.println("          <th class='px-4 py-2 text-gray-600 font-medium'>邮箱</th>");
        out.println("          <th class='px-4 py-2 text-gray-600 font-medium'>操作</th>");
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
            out.println("          <td class='px-4 py-2'>");
            out.println("            <button onclick='confirmDelete(\"" + name + "\")' " +
                    "class='bg-red-500 text-white px-2 py-1 rounded hover:bg-red-600'>删除</button>");
            out.println("          </td>");
            out.println("        </tr>");
        }

        if (!hasData) {
            out.println("        <tr>");
            out.println("          <td colspan='3' class='px-4 py-2 text-center text-gray-500'>没有找到匹配的用户。</td>");
            out.println("        </tr>");
        }

        out.println("      </tbody>");
        out.println("    </table>");

        // 分页导航
        out.println("    <div class='mt-4 flex justify-between'>");
        String searchParam = searchName != null && !searchName.trim().isEmpty()
                ? "&search=" + searchName
                : "";
        if (hasPrev) {
            out.println("      <a href='?page=" + (currentPage - 1) + searchParam + "' " +
                    "class='px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600'>上一页</a>");
        } else {
            out.println("      <span class='px-4 py-2 bg-gray-300 text-white rounded'>上一页</span>");
        }
        if (hasNext) {
            out.println("      <a href='?page=" + (currentPage + 1) + searchParam + "' " +
                    "class='px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600'>下一页</a>");
        } else {
            out.println("      <span class='px-4 py-2 bg-gray-300 text-white rounded'>下一页</span>");
        }
        out.println("    </div>");

        out.println("  </div>");
        out.println("</body>");
        out.println("</html>");
    }
}