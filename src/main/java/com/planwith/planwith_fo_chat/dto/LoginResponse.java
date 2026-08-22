package com.planwith.planwith_fo_chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Login response")
public record LoginResponse(
		@Schema(description = "Authenticated user ID", example = "test-001")
		String id,

		@Schema(description = "Result message", example = "로그인에 성공했습니다.")
		String message
) {
}
