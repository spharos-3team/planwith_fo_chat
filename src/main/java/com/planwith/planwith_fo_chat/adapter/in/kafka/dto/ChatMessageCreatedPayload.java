package com.planwith.planwith_fo_chat.adapter.in.kafka.dto;

import java.util.UUID;

public record ChatMessageCreatedPayload(
		UUID chatRoomUuid,
		UUID messageUuid,
		UUID senderUuid,
		String content
) {
}
