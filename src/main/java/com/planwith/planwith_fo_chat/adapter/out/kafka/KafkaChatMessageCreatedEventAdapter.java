package com.planwith.planwith_fo_chat.adapter.out.kafka;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_chat.adapter.in.kafka.dto.ChatMessageCreatedPayload;
import com.planwith.planwith_fo_chat.adapter.in.kafka.dto.EventEnvelope;
import com.planwith.planwith_fo_chat.application.chat.ApplyChatMessageCreatedService;
import com.planwith.planwith_fo_chat.application.port.out.ChatMessageCreatedEventPort;
import com.planwith.planwith_fo_chat.config.ChatKafkaProperties;

@Component
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "true")
public class KafkaChatMessageCreatedEventAdapter implements ChatMessageCreatedEventPort {

	private static final Logger log = LoggerFactory.getLogger(KafkaChatMessageCreatedEventAdapter.class);

	private static final int VERSION = 1;

	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;
	private final ChatKafkaProperties properties;

	public KafkaChatMessageCreatedEventAdapter(
			KafkaTemplate<String, String> kafkaTemplate,
			ObjectMapper objectMapper,
			ChatKafkaProperties properties
	) {
		this.kafkaTemplate = kafkaTemplate;
		this.objectMapper = objectMapper;
		this.properties = properties;
	}

	@Override
	public void publish(Event event) {
		EventEnvelope<ChatMessageCreatedPayload> envelope = new EventEnvelope<>(
				event.eventId(),
				ApplyChatMessageCreatedService.EVENT_TYPE,
				event.occurredAt(),
				event.chatRoomUuid().toString(),
				VERSION,
				new ChatMessageCreatedPayload(
						event.chatRoomUuid(),
						event.messageUuid(),
						event.senderUuid(),
						event.content()
				)
		);
		String body;
		try {
			body = objectMapper.writeValueAsString(envelope);
		}
		catch (JsonProcessingException exception) {
			log.error("KafkaChatMessageCreatedEventAdapter : publish : serialize failed - chatRoomUuid={}",
					event.chatRoomUuid());
			return;
		}
		CompletableFuture<?> sendResult = kafkaTemplate.send(
				properties.messageCreatedTopic(),
				event.chatRoomUuid().toString(),
				body
		);
		sendResult.whenComplete((result, exception) -> {
			if (exception != null) {
				log.error("KafkaChatMessageCreatedEventAdapter : publish : send failed - chatRoomUuid={}",
						event.chatRoomUuid());
				return;
			}
			log.info("KafkaChatMessageCreatedEventAdapter : publish : sent - chatRoomUuid={}", event.chatRoomUuid());
		});
	}
}
