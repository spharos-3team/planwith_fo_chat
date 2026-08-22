package com.planwith.planwith_fo_chat.application.port.in;

import java.time.Instant;
import java.util.UUID;

import com.planwith.planwith_fo_chat.domain.chat.ChatRoomMemberRead;

public interface MarkChatRoomReadUseCase {

	ChatRoomMemberRead markRead(Command command);

	record Command(
			UUID memberUuid,
			UUID chatRoomUuid,
			UUID lastReadMessageUuid,
			Instant occurredAt
	) {
	}
}
