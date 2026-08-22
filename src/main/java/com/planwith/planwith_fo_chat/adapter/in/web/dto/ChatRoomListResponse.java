package com.planwith.planwith_fo_chat.adapter.in.web.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ChatRoomListResponse(
		List<ChatRoomListItemResponse> content,
		Instant nextCursorAt,
		UUID nextCursorChatRoomUuid
) {
}
