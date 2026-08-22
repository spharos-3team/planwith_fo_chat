package com.planwith.planwith_fo_chat.adapter.in.web.dto;

import java.time.Instant;
import java.util.UUID;

public record ChatRoomReadResponse(
		UUID chatRoomUuid,
		UUID lastReadMessageUuid,
		int unreadCount,
		Instant updatedAt
) {
}
