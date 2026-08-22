package com.planwith.planwith_fo_chat.adapter.in.kafka.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MeetingCreatedPayload(
		UUID meetingUuid,
		UUID hostMemberUuid,
		String title
) {
}
