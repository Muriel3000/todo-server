package com.example.model;

import java.util.logging.Logger;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

public class UserManager {
	private final Logger logger = Logger.getLogger(ToDoManager.class.getName());
	private final UserDAO dao;

	// 応答用のレコード
	public record PutResult(User todo, String error) implements Result {}

	private UserManager(String dbPath) {
		dao = new UserDAO("jdbc:sqlite:" + dbPath);
	}

	private static class SingletonHolder {
		private static UserManager singleton;
	}

	public static UserManager getInstance(String dbPath) {
		if (SingletonHolder.singleton == null) {
			SingletonHolder.singleton = new UserManager(dbPath);
		}
		return SingletonHolder.singleton;
	}

	public PutResult putField(String userName, String fieldName, User params) {
		try {
			Argon2 argon2 = Argon2Factory.create();
			var hash = argon2.hash(3. 65536,1,params.password());	
		}
	}
}
