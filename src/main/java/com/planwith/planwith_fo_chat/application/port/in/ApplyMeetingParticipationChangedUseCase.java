package com.planwith.planwith_fo_chat.application.port.in;

import java.time.Instant;
import java.util.UUID;

import com.planwith.planwith_fo_chat.domain.chat.ChatMember;

public interface ApplyMeetingParticipationChangedUseCase {

	ChatMember apply(Command command);

	record Command(
			String eventId,
			UUID meetingUuid,
			UUID memberUuid,
			String status,
			Instant occurredAt
	) {
	}
}
