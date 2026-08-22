package com.planwith.planwith_fo_chat.application.port.out;

import java.time.Instant;
import java.util.UUID;

public interface ProcessedChatEventPort {

	boolean existsByEventId(String eventId);

	void save(String eventId, String eventType, UUID meetingUuid, Instant processedAt);
}
