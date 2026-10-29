
package com.findit.servlet;

import com.findit.dao.UserDAO;
import com.findit.util.PasswordUtil;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/register.jsp")
           .forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req,
                          HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        String name = clean(req.getParameter("fullName"));
        String email = clean(req.getParameter("email")).toLowerCase();
        String password = req.getParameter("password");
        String confirm = req.getParameter("confirmPassword");

        if (name.isBlank() || email.isBlank()
                || password == null
                || password.length() < 8
                || password.length() > 128) {
            req.setAttribute("error",
                    "Enter your name and email, and use a password between 8 and 128 characters.");
            doGet(req, resp);
            return;
        }

        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            req.setAttribute("error", "Enter a valid email address.");
            doGet(req, resp);
            return;
        }

        if (!password.equals(confirm)) {
            req.setAttribute("error", "Passwords do not match.");
            doGet(req, resp);
            return;
        }

        try {
            userDAO.register(name, email, PasswordUtil.hash(password));
            resp.sendRedirect(req.getContextPath() + "/login?registered=1");

        } catch (SQLException ex) {
            log("FindIt registration SQL error. SQLState="
                    + ex.getSQLState()
                    + ", ErrorCode=" + ex.getErrorCode()
                    + ", Message=" + ex.getMessage(), ex);

            if ("23000".equals(ex.getSQLState())) {
                req.setAttribute("error",
                        "An account with this email already exists.");
            } else {
                req.setAttribute("error",
                        "Registration failed. Check the NetBeans Output window for the database error.");
            }

            doGet(req, resp);

        } catch (Exception ex) {
            log("FindIt registration error", ex);
            req.setAttribute("error",
                    "Registration failed due to a server error. Check the NetBeans Output window.");
            doGet(req, resp);
        }
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
