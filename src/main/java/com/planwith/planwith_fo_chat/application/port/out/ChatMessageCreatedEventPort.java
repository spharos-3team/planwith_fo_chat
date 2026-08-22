package com.planwith.planwith_fo_chat.application.port.out;

import java.time.Instant;
import java.util.UUID;

public interface ChatMessageCreatedEventPort {

	void publish(Event event);

	record Event(
			String eventId,
			UUID chatRoomUuid,
			UUID messageUuid,
			UUID senderUuid,
			String content,
			Instant occurredAt
	) {
	}
}
