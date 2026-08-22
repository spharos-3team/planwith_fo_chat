package com.planwith.planwith_fo_chat.adapter.in.web.dto;

import java.util.UUID;

public record MarkChatRoomReadRequest(
		UUID lastReadMessageUuid
) {
}
