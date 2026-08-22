package com.planwith.planwith_fo_chat.application.port.in;

import java.time.Instant;
import java.util.UUID;

public interface ApplyChatMessageCreatedUseCase {

	void apply(Command command);

	record Command(
			String eventId,
			UUID chatRoomUuid,
			UUID messageUuid,
			UUID senderUuid,
			String content,
			Instant occurredAt
	) {
	}
}
