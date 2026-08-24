package com.planwith.planwith_fo_chat.adapter.in.web.dto;

import java.time.Instant;
import java.util.UUID;

import com.planwith.planwith_fo_chat.domain.chat.ChatRoomStatus;

public record ChatRoomListItemResponse(
		UUID chatRoomUuid,
		UUID meetingUuid,
		String roomName,
		ChatRoomStatus roomStatus,
		LastMessageResponse lastMessage,
		int unreadCount
) {

	public record LastMessageResponse(
			UUID messageUuid,
			String content,
			UUID senderUuid,
			Instant createdAt
	) {
	}
}
