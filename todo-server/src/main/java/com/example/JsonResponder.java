package com.example;

import java.io.IOException;

import com.example.model.HttpErrors;
import com.example.model.Result;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;
import jakarta.servlet.http.HttpServletResponse;

public class JsonResponder {
	private final Jsonb jsonb = JsonbBuilder.create();

	private JsonResponder() {}

	private static class SingletonHolder {
		private static JsonResponder singleton;
	}

	public static JsonResponder getInstance() {
		if (SingletonHolder.singleton == null) {
			SingletonHolder.singleton = new JsonResponder();
		}
		return SingletonHolder.singleton;
	}

	public void sendJson(HttpServletResponse response, int successCode, Result result)
			throws JsonbException, IOException {
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json");
		response.setStatus(getStatusCode(successCode, result.error()));
		response.getWriter().write(jsonb.toJson(result));
	}

	private int getStatusCode(int successCode, String error) {
		return switch (error) {
			case null -> successCode;
			case HttpErrors.INVALID_JSON_ERROR -> HttpServletResponse.SC_BAD_REQUEST;
			case HttpErrors.NOT_FOUND_ERROR -> HttpServletResponse.SC_NOT_FOUND;
			case HttpErrors.FORBIDDEN_ERROR -> HttpServletResponse.SC_FORBIDDEN;
			default -> HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
		};
	}
}
