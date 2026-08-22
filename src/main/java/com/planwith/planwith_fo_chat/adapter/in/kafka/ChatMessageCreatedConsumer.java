package com.planwith.planwith_fo_chat.adapter.in.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_chat.adapter.in.kafka.dto.ChatMessageCreatedPayload;
import com.planwith.planwith_fo_chat.adapter.in.kafka.dto.EventEnvelope;
import com.planwith.planwith_fo_chat.application.port.in.ApplyChatMessageCreatedUseCase;

@Component
@ConditionalOnProperty(prefix = "app.kafka", name = "consumer-enabled", havingValue = "true")
public class ChatMessageCreatedConsumer {

	private static final Logger log = LoggerFactory.getLogger(ChatMessageCreatedConsumer.class);

	private final ApplyChatMessageCreatedUseCase applyChatMessageCreatedUseCase;
	private final ObjectMapper objectMapper;

	public ChatMessageCreatedConsumer(
			ApplyChatMessageCreatedUseCase applyChatMessageCreatedUseCase,
			ObjectMapper objectMapper
	) {
		this.applyChatMessageCreatedUseCase = applyChatMessageCreatedUseCase;
		this.objectMapper = objectMapper;
	}

	@KafkaListener(topics = "${app.kafka.message-created-topic}")
	public void consume(
			@Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
			@Payload String payload
	) {
		EventEnvelope<ChatMessageCreatedPayload> envelope = parse(payload);
		if (envelope == null || envelope.payload() == null) {
			log.error("ChatMessageCreatedConsumer : consume : skip invalid payload - topic={}", topic);
			return;
		}
		ChatMessageCreatedPayload body = envelope.payload();
		try {
			applyChatMessageCreatedUseCase.apply(new ApplyChatMessageCreatedUseCase.Command(
					envelope.eventId(),
					body.chatRoomUuid(),
					body.messageUuid(),
					body.senderUuid(),
					body.content(),
					envelope.occurredAt()
			));
		}
		catch (IllegalArgumentException exception) {
			log.error("ChatMessageCreatedConsumer : consume : skip invalid event - eventId={}", envelope.eventId());
		}
		catch (RuntimeException exception) {
			log.error("ChatMessageCreatedConsumer : consume : retry later - eventId={}", envelope.eventId());
			throw exception;
		}
	}

	private EventEnvelope<ChatMessageCreatedPayload> parse(String payload) {
		if (payload == null || payload.isBlank()) {
			return null;
		}
		try {
			return objectMapper.readValue(payload, new TypeReference<>() {
			});
		}
		catch (JsonProcessingException exception) {
			log.error("ChatMessageCreatedConsumer : parse : invalid EventEnvelope JSON");
			return null;
		}
	}
}
