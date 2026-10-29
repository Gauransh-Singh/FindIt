package com.findit.dao;

import com.findit.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
    public boolean register(String name, String email, String passwordHash) throws SQLException {
        String sql = "INSERT INTO users (full_name, email, password_hash, role) VALUES (?, ?, ?, 'USER')";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name); ps.setString(2, email); ps.setString(3, passwordHash);
            return ps.executeUpdate() == 1;
        }
    }

    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT user_id, full_name, email, password_hash, role FROM users WHERE email = ?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new User(rs.getInt("user_id"), rs.getString("full_name"), rs.getString("email"), rs.getString("password_hash"), rs.getString("role"));
            }
        }
    }

    public static class User {
        private final int id; private final String fullName, email, passwordHash, role;
        public User(int id, String fullName, String email, String passwordHash, String role) { this.id=id; this.fullName=fullName; this.email=email; this.passwordHash=passwordHash; this.role=role; }
        public int getId(){return id;} public String getFullName(){return fullName;} public String getEmail(){return email;} public String getPasswordHash(){return passwordHash;} public String getRole(){return role;}
    }
}
