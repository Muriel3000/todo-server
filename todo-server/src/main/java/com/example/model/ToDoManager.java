package com.example.model;

import java.util.List;
import java.util.logging.Logger;

import com.example.exceptions.RecordNotFoundException;

public class ToDoManager {
	private final Logger logger = Logger.getLogger(ToDoManager.class.getName());
	private final ToDoDAO dao;

	// 応答用のレコード
	public record GetResult(List<ToDo> todos, String error) implements Result {}

	public record PostResult(ToDo todo, String error) implements Result {}

	public record PutResult(ToDo todo, String error) implements Result {}

	public record DeleteResult(int id, String error) implements Result {}

	private ToDoManager(String dbPath) {
		dao = new ToDoDAO("jdbc:sqlite:" + dbPath);
	}

	private static class SingletonHolder {
		private static ToDoManager singleton;
	}

	public static ToDoManager getInstance(String dbPath) {
		if (SingletonHolder.singleton == null) {
			SingletonHolder.singleton = new ToDoManager(dbPath);
		}
		return SingletonHolder.singleton;
	}

	public GetResult getAll() {
		try {
			return new GetResult(dao.getAll(), null);
		} catch (Exception e) {
			logger.severe(e.getMessage());
			return new GetResult(null, HttpErrors.INTERNAL_SERVER_ERROR);
		}
	}

	public PostResult post(ToDo todoParams) {
		try {
			return new PostResult(
					dao.create(
							todoParams.title(),
							todoParams.date(),
							todoParams.priority(),
							todoParams.completed()
					),
					null
			);
		} catch (Exception e) {
			logger.severe(e.getMessage());
			return new PostResult(null, HttpErrors.INTERNAL_SERVER_ERROR);
		}
	}

	public PutResult putField(int id, String fieldName, ToDo params) {
		try {
			var updatedToDo = switch (fieldName) {
				case "title" -> dao.updateTitle(id, params.title());
				case "date" -> dao.updateDate(id, params.date());
				case "priority" -> dao.updatePriority(id, params.priority());
				case "completed" -> dao.updateCompleted(id, params.completed());
				default -> null;
			};
			return new PutResult(updatedToDo, null);
		} catch (Exception e) {
			logger.severe(e.getMessage());
			return new PutResult(null, HttpErrors.INTERNAL_SERVER_ERROR);
		}
	}

	public DeleteResult delete(int id) {
		try {
			dao.delete(id);
			return new DeleteResult(id, null);
		} catch (RecordNotFoundException e) {
			logger.warning(e.getMessage());
			return new DeleteResult(id, HttpErrors.NOT_FOUND_ERROR);
		} catch (Exception e) {
			logger.severe(e.getMessage());
			return new DeleteResult(id, HttpErrors.INTERNAL_SERVER_ERROR);
		}
	}

	public DeleteResult deleteAll() {
		try {
			dao.deleteAll();
			return new DeleteResult(-1, null);
		} catch (Exception e) {
			logger.severe(e.getMessage());
			return new DeleteResult(-1, HttpErrors.INTERNAL_SERVER_ERROR);
		}
	}
}
