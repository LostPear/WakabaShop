package com.rinko24.wakabashop;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/manage")
public class Manage extends HttpServlet {
    private static final int PAGE_SIZE = 5;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");

        handleListUsers(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        if ("delete".equals(action)) {
            handleDeleteUser(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "不支持的操作");
        }
    }

    private void handleListUsers(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int page = 1;
        String pageParam = request.getParameter("page");
        String searchName = request.getParameter("search");

        if (pageParam != null) {
            try {
                page = Math.max(1, Integer.parseInt(pageParam));
            } catch (NumberFormatException e) {
                page = 1;
            }
        }

        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "SELECT name, email FROM (" +
                    "  SELECT name, email, ROWNUM rn FROM (" +
                    "    SELECT name, email FROM users " +
                    (searchName != null && !searchName.trim().isEmpty() ? "WHERE LOWER(name) LIKE LOWER(?) " : "") +
                    "    ORDER BY name" +
                    "  ) WHERE ROWNUM <= ?" +
                    ") WHERE rn > ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int paramIndex = 1;
                if (searchName != null && !searchName.trim().isEmpty()) {
                    stmt.setString(paramIndex++, "%" + searchName.trim() + "%");
                }
                stmt.setInt(paramIndex++, page * PAGE_SIZE);
                stmt.setInt(paramIndex, (page - 1) * PAGE_SIZE);

                try (ResultSet rs = stmt.executeQuery()) {
                    int totalRecords = getTotalRecords(conn, searchName);
                    int totalPages = (int) Math.ceil(totalRecords / (double) PAGE_SIZE);
                    boolean hasPrev = page > 1;
                    boolean hasNext = page < totalPages;

                    generateUserListHtml(response, rs, page, hasPrev, hasNext, searchName);
                }
            }
        } catch (SQLException e) {
            throw new ServletException("数据库错误", e);
        }
    }

    private int getTotalRecords(Connection conn, String searchName) throws SQLException {
        String countSql = "SELECT COUNT(*) FROM users " +
                (searchName != null && !searchName.trim().isEmpty() ? "WHERE LOWER(name) LIKE LOWER(?)" : "");

        try (PreparedStatement stmt = conn.prepareStatement(countSql)) {
            if (searchName != null && !searchName.trim().isEmpty()) {
                stmt.setString(1, "%" + searchName.trim() + "%");
            }

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private void handleDeleteUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String name = request.getParameter("name");

        if (name == null || name.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "用户名不能为空");
            return;
        }

        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "DELETE FROM users WHERE name = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, name);
                int rows = stmt.executeUpdate();

                response.setContentType("application/json");
                PrintWriter out = response.getWriter();
                if (rows > 0) {
                    out.print("{\"success\": true}");
                } else {
                    out.print("{\"success\": false, \"error\": \"未找到用户\"}");
                }
            }
        } catch (SQLException e) {
            throw new ServletException("删除失败", e);
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
        out.println("        fetch('manage', {");
        out.println("          method: 'POST',");
        out.println("          headers: {'Content-Type': 'application/x-www-form-urlencoded'},");
        out.println("          body: 'action=delete&name=' + encodeURIComponent(name)");
        out.println("        })");
        out.println("        .then(res => res.json())");
        out.println("        .then(data => {");
        out.println("          if (data.success) { window.location.reload(); }");
        out.println("          else { alert('删除失败: ' + (data.error || '未知错误')); }");
        out.println("        })");
        out.println("        .catch(err => alert('请求失败: ' + err));");
        out.println("      }");
        out.println("    }");
        out.println("  </script>");
        out.println("</head>");
        out.println("<body class='bg-gray-100'>");
        out.println("<div class='max-w-4xl mx-auto mt-12 p-6 bg-white rounded-lg shadow'>");
        out.println("<h2 class='text-2xl font-semibold mb-4'>用户管理</h2>");

        out.println("<form action='manage' method='get' class='mb-4 flex'>");
        out.println("  <input type='text' name='search' value='" + (searchName != null ? searchName : "") + "' placeholder='按用户名搜索' class='flex-grow px-3 py-2 border rounded-l-md focus:outline-none focus:ring-2 focus:ring-blue-500'>");
        out.println("  <button type='submit' class='bg-blue-500 text-white px-4 py-2 rounded-r-md hover:bg-blue-600'>搜索</button>");
        out.println("</form>");

        out.println("<table class='w-full text-left border-collapse'>");
        out.println("  <thead class='bg-gray-50 border-b'>");
        out.println("    <tr><th class='px-4 py-2'>姓名</th><th class='px-4 py-2'>邮箱</th><th class='px-4 py-2'>操作</th></tr>");
        out.println("  </thead><tbody class='divide-y divide-gray-100'>");

        boolean hasData = false;
        while (rs.next()) {
            hasData = true;
            String name = rs.getString("name");
            String email = rs.getString("email");
            out.println("<tr>");
            out.println("  <td class='px-4 py-2'>" + name + "</td>");
            out.println("  <td class='px-4 py-2'>" + email + "</td>");
            out.println("  <td class='px-4 py-2'><button onclick='confirmDelete(\"" + name + "\")' class='bg-red-500 text-white px-2 py-1 rounded hover:bg-red-600'>删除</button></td>");
            out.println("</tr>");
        }

        if (!hasData) {
            out.println("<tr><td colspan='3' class='px-4 py-2 text-center text-gray-500'>没有匹配的用户</td></tr>");
        }

        out.println("</tbody></table>");

        // 分页按钮
        out.println("<div class='mt-4 flex justify-between'>");
        String searchParam = (searchName != null && !searchName.trim().isEmpty()) ? "&search=" + searchName : "";
        if (hasPrev) {
            out.println("<a href='?page=" + (currentPage - 1) + searchParam + "' class='px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600'>上一页</a>");
        } else {
            out.println("<span class='px-4 py-2 bg-gray-300 text-white rounded'>上一页</span>");
        }
        if (hasNext) {
            out.println("<a href='?page=" + (currentPage + 1) + searchParam + "' class='px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600'>下一页</a>");
        } else {
            out.println("<span class='px-4 py-2 bg-gray-300 text-white rounded'>下一页</span>");
        }
        out.println("</div>");

        out.println("</div></body></html>");
    }
}
