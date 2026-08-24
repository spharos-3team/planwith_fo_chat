package com.planwith.planwith_fo_chat.adapter.in.web.dto;

import java.time.Instant;
import java.util.List;

public record ChatMessageListResponse(
		List<ChatMessageResponse> content,
		Instant nextBefore
) {
}
