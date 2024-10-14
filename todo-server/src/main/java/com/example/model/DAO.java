package com.example.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.example.exceptions.RecordNotFoundException;

/**
 * DAO for ToDo App
 */
public class DAO {
	private final Logger logger = Logger.getLogger(DAO.class.getName());
	private final String url;

	public DAO(String url) {
		this.url = url;
		// DriverManger に org.sqlite.JDBC クラス(JDBCドライバ)を登録する処理		
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			logger.severe("JDBCドライバが見つかりません。");
		}
	}

	public ToDo get(int id) throws SQLException {
		try (
				Connection conn = DriverManager.getConnection(url);
				PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM todo where id=?");
			) {
			pstmt.setInt(1, id);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				return new ToDo(
						rs.getInt("id"),
						rs.getString("title"),
						LocalDate.parse(rs.getString("date")),
						rs.getInt("priority"),
						rs.getInt("completed") == 1
				);
			}
		}
		return null;
	}

	public ArrayList<ToDo> getAll() throws SQLException {
		var todos = new ArrayList<ToDo>();
		try (
				Connection conn = DriverManager.getConnection(url);
				PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM todo");
			) {
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				todos.add(new ToDo(
						rs.getInt("id"),
						rs.getString("title"),
						LocalDate.parse(rs.getString("date")),
						rs.getInt("priority"),
						rs.getInt("completed") == 1
				));
			}
		}
		return todos;
	}

	public ToDo create(String title, LocalDate date, int priority, boolean completed) throws SQLException {
		try (
				Connection conn = DriverManager.getConnection(url);
				PreparedStatement pstmt = conn.prepareStatement(
						"INSERT INTO todo(title, date, priority, completed) VALUES(?, ?, ?, ?)",
						Statement.RETURN_GENERATED_KEYS);
			) {
			pstmt.setString(1, title);
			pstmt.setString(2, date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
			pstmt.setInt(3, priority);
			pstmt.setInt(4, completed ? 1 : 0);
			pstmt.executeUpdate();

			// AUTOINCREMENTで生成された id を取得します。
			ResultSet rs = pstmt.getGeneratedKeys();
			rs.next();
			return new ToDo(
					rs.getInt(1),
					title,
					date,
					priority,
					completed
			);
		}
	}

    private ToDo updateField(String query, int id, Object value) throws SQLException, RecordNotFoundException {
        try (
        		Connection conn = DriverManager.getConnection(url);
        		PreparedStatement pstmt = conn.prepareStatement(query);
        	) {
            pstmt.setObject(1, value);
            pstmt.setInt(2, id);
            int num = pstmt.executeUpdate();
            if (num <= 0) {
                throw new RecordNotFoundException("id " + id + " does not exist.");
            }
        }
        return get(id);
    }
    
	public ToDo updateTitle(int id, String title) throws SQLException, RecordNotFoundException {
		return updateField("UPDATE todo SET title=? WHERE id=?", id, title);
    }

    public ToDo updateDate(int id, LocalDate date) throws SQLException, RecordNotFoundException {
        return updateField("UPDATE todo SET date=? WHERE id=?", id, date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    }

    public ToDo updateCompleted(int id, boolean completed) throws SQLException, RecordNotFoundException {
        return updateField("UPDATE todo SET completed=? WHERE id=?", id, completed ? 1 : 0);
    }
    
	public ToDo updatePriority(int id, int priority) throws SQLException, RecordNotFoundException {
		return updateField("UPDATE todo SET priority=? WHERE id=?", id, priority);
	}

    public void delete(int id) throws SQLException, RecordNotFoundException {
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM todo WHERE id=?")) {
            pstmt.setInt(1, id);
            int num = pstmt.executeUpdate();
            if (num <= 0) {
                throw new RecordNotFoundException("id " + id + " does not exist.");
            }
        }
    }
    
    public void deleteAll() throws SQLException {
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM todo")) {
            pstmt.executeUpdate();
        }
    }
}
