package com.planwith.planwith_fo_chat.adapter.in.web.dto;

import java.time.Instant;
import java.util.Map;

public record ApiResponse<T>(
		boolean success,
		T data,
		ApiError error,
		Instant timestamp
) {

	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(true, data, null, Instant.now());
	}

	public static ApiResponse<Void> failure(String code, String message, Map<String, String> fieldErrors) {
		return new ApiResponse<>(false, null, new ApiError(code, message, fieldErrors), Instant.now());
	}

	public record ApiError(String code, String message, Map<String, String> fieldErrors) {
		public ApiError {
			fieldErrors = fieldErrors == null ? Map.of() : Map.copyOf(fieldErrors);
		}
	}
}
