package com.planwith.planwith_fo_chat.application.port.in;

import java.time.Instant;
import java.util.UUID;

import com.planwith.planwith_fo_chat.domain.chat.ChatMessage;

public interface UpdateChatMessageUseCase {

	ChatMessage modify(ModifyCommand command);

	ChatMessage delete(DeleteCommand command);

	record ModifyCommand(
			UUID messageUuid,
			UUID requesterUuid,
			String content,
			Instant occurredAt
	) {
	}

	record DeleteCommand(
			UUID messageUuid,
			UUID requesterUuid,
			Instant occurredAt
	) {
	}
}
