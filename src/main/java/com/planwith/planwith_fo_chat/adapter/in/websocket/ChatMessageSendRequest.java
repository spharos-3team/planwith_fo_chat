package com.planwith.planwith_fo_chat.adapter.in.websocket;

import java.util.List;

import jakarta.validation.constraints.NotBlank;

public record ChatMessageSendRequest(
		@NotBlank String messageType,
		String content,
		List<ChatFileSendRequest> files
) {

	public record ChatFileSendRequest(
			@NotBlank String fileType,
			@NotBlank String url,
			String name
	) {
	}
}
