package com.example;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.example.model.HttpErrors;
import com.example.model.User;
import com.example.model.UserManager;
import com.example.model.UserManager.PutResult;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class UsersNameApi extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final Jsonb jsonb = JsonbBuilder.create();
	private final Logger logger = Logger.getLogger(UsersNameApi.class.getName());
	private UserManager manager;

	@Override
	public void init() throws ServletException {
		super.init();
		var dbPath = getServletContext().getInitParameter("dbPath");
		manager = UserManager.getInstance(dbPath);
	}

	// 「/api/users」より後ろのパスとマッチ
	private final Pattern PUT_PATTERN = Pattern.compile("^/(.+?)/(password)$");

	protected void doPut(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String path = request.getPathInfo();
		logger.info("PUT: " + path);
		
		PutResult putResult;
		try {
			var params = jsonb.fromJson(new InputStreamReader(request.getInputStream()), User.class);
			var mat = PUT_PATTERN.matcher(path);
			if (mat.matches()) {
				var name = mat.group(1);
				var fieldName = mat.group(2);
				putResult = manager.putField(name, fieldName, params);
			} else {
				putResult = new PutResult(null, HttpErrors.NOT_FOUND_ERROR);
			}
		} catch (JsonbException e) {
			putResult = new PutResult(null, HttpErrors.INVALID_JSON_ERROR);
		}
		JsonResponder.getInstance().sendJson(response, HttpServletResponse.SC_OK, putResult);
	}

}
