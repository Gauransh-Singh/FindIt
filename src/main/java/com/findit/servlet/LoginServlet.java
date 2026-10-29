package com.findit.servlet;

import com.findit.dao.UserDAO;
import com.findit.util.PasswordUtil;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
    }
    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String email = req.getParameter("email"); String password = req.getParameter("password");
        if (email == null || password == null || email.isBlank() || password.isEmpty()) { req.setAttribute("error", "Enter your email and password."); doGet(req, resp); return; }
        try {
            UserDAO.User user = userDAO.findByEmail(email.trim().toLowerCase());
            if (user == null || !PasswordUtil.verify(password, user.getPasswordHash())) { req.setAttribute("error", "Invalid email or password."); doGet(req, resp); return; }
            HttpSession old = req.getSession(false); if (old != null) old.invalidate();
            HttpSession session = req.getSession(true); session.setAttribute("userId", user.getId()); session.setAttribute("fullName", user.getFullName()); session.setAttribute("email", user.getEmail()); session.setAttribute("role", user.getRole()); session.setMaxInactiveInterval(30 * 60);
            resp.sendRedirect(req.getContextPath() + "/home");
        } catch (SQLException ex) { log("Login failed", ex); req.setAttribute("error", "Could not sign in. Check the database connection and try again."); doGet(req, resp); }
    }
}
