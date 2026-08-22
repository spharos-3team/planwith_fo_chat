package com.planwith.planwith_fo_chat.application.port.in;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.planwith.planwith_fo_chat.domain.chat.ChatMessage;

public interface ListChatMessagesUseCase {

	List<ChatMessage> list(Command command);

	record Command(
			UUID chatRoomUuid,
			UUID requesterUuid,
			Instant before,
			int size
	) {
	}
}
