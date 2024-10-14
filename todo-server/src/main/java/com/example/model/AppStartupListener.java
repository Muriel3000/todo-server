package com.example.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppStartupListener implements ServletContextListener {
	private final Logger logger = Logger.getLogger(AppStartupListener.class.getName());

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		logger.log(Level.INFO, "アプリを起動しました。");

		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			logger.severe("JDBCドライバが見つかりません。");
		}

		// 初期化処理
		String dbPath = sce.getServletContext().getInitParameter("dbPath");
		String url = "jdbc:sqlite:" + dbPath;
		logger.log(Level.INFO, "[AppStartupListener] DB Location: " + url);

		executeUpdate(
				url, "CREATE TABLE IF NOT EXISTS todo ("
						+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
						+ "title TEXT,"
						+ "date	TEXT,"
						+ "priority	INTEGER,"
						+ "completed INTEGER"
						+ ")"
		);

		executeUpdate(
				url, "CREATE TABLE IF NOT EXISTS users ("
						+ "user_name TEXT PRIMARY KEY,"
						+ "password TEXT NOT NULL)"
		);

		executeUpdate(
				url, "CREATE TABLE IF NOT EXISTS user_roles ("
						+ "user_name TEXT NOT NULL PRIMARY KEY,"
						+ "role_name TEXT NOT NULL)"
		);

		if (isEmpty(url, "SELECT COUNT(*) cnt FROM todo")) {
			executeUpdate(
					url,
					"INSERT INTO todo (title, date, priority, completed) VALUES ('The first task', '2024-01-01', 3, 0)"
			);
		}
		if (isEmpty(url, "SELECT COUNT(*) cnt FROM users")) {
			/**
			 * 初期ユーザが登録されていなければ登録
			 * 初期パスワードはすぐに変更すること
			 */
			Argon2 argon2 = Argon2Factory.create();
			var hash = argon2.hash(3, 65536, 1, "foo");
			executeUpdate(url, "INSERT INTO users VALUES ('user', '" + hash + "')");
			executeUpdate(url, "INSERT INTO users VALUES ('admin', '" + hash + "')");
		}
		if (isEmpty(url, "SELECT COUNT(*) cnt FROM user_roles")) {
			executeUpdate(url, "INSERT INTO user_roles VALUES ('user', 'USER')");
			executeUpdate(url, "INSERT INTO user_roles VALUES ('admin', 'ADMIN')");
		}
	}

	private boolean isEmpty(String url, String sql) {
		int count = -1;
		try (
				Connection conn = DriverManager.getConnection(url);
				PreparedStatement pstmt = conn.prepareStatement(sql);
		) {
			ResultSet rs = pstmt.executeQuery();
			rs.next();
			count = rs.getInt("cnt");
		} catch (Exception e) {
			logger.log(java.util.logging.Level.SEVERE, "Failed to count users", e);
		}

		if (count == 0) {
			return true;
		}
		return false;
	}

	private void executeUpdate(String url, String query) {
		try (
				Connection conn = DriverManager.getConnection(url);
				PreparedStatement statement = conn.prepareStatement(query);
		) {
			statement.executeUpdate();
		} catch (SQLException e) {
			logger.log(Level.SEVERE, e.getMessage());
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		logger.log(Level.INFO, "アプリを停止しました。");
	}
}
