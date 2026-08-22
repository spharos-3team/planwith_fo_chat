package com.planwith.planwith_fo_chat.application.port.in;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.planwith.planwith_fo_chat.domain.chat.ChatFile;
import com.planwith.planwith_fo_chat.domain.chat.ChatMessage;

public interface SaveChatMessageUseCase {

	ChatMessage save(Command command);

	record Command(
			UUID chatRoomUuid,
			UUID senderUuid,
			String messageType,
			String content,
			List<ChatFile> files,
			Instant occurredAt
	) {
	}
}
