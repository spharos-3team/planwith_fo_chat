package com.planwith.planwith_fo_chat.application.port.in;

import java.time.Instant;
import java.util.UUID;

import com.planwith.planwith_fo_chat.domain.chat.ChatRoom;

public interface ApplyMeetingCreatedUseCase {

	ChatRoom apply(Command command);

	record Command(
			String eventId,
			UUID meetingUuid,
			UUID hostMemberUuid,
			String title,
			Instant occurredAt
	) {
	}
}
