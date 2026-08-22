package com.planwith.planwith_fo_chat.adapter.in.kafka.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EventEnvelope<T>(
		String eventId,
		String eventType,
		Instant occurredAt,
		String aggregateId,
		int version,
		T payload
) {
}
