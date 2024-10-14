package com.example.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import com.example.exceptions.RecordNotFoundException;

/**
 * DAO for ToDo App
 */
public class UserDAO {
	private final Logger logger = Logger.getLogger(UserDAO.class.getName());
	private final String url;

	public UserDAO(String url) {
		this.url = url;
		// DriverManger に org.sqlite.JDBC クラス(JDBCドライバ)を登録する処理		
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			logger.severe("JDBCドライバが見つかりません。");
		}
	}

	public User getUser(String name) throws SQLException {
		try (
				Connection conn = DriverManager.getConnection(url);
				PreparedStatement pstmt = conn.prepareStatement("SELECT user_name FROM users where user_name=?");
		) {
			pstmt.setString(1, name);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				return new User(rs.getString("user_name"), "");
			}
		}
		return null;
	}

	private User updateUserField(String query, String name, Object value) throws SQLException, RecordNotFoundException {
		try (
				Connection conn = DriverManager.getConnection(url);
				PreparedStatement pstmt = conn.prepareStatement(query);
		) {
			pstmt.setObject(1, value);
			pstmt.setString(2, name);
			int num = pstmt.executeUpdate();
			if (num <= 0) {
				throw new RecordNotFoundException("user_name " + name + " does not exist.");
			}
		}
		return getUser(name);
	}

	public User updatePassword(String user_name, String password) throws SQLException, RecordNotFoundException {
		return updateUserField("UPDATE users SET password=? WHERE user_name=?", user_name, password);
	}
}
