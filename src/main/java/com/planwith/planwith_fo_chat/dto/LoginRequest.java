package com.planwith.planwith_fo_chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Login request")
public record LoginRequest(
		@Schema(description = "User ID", example = "test-001")
		@NotBlank(message = "아이디는 필수입니다.")
		String id,

		@Schema(description = "Password", example = "1234")
		@NotBlank(message = "비밀번호는 필수입니다.")
		String pw
) {
}
