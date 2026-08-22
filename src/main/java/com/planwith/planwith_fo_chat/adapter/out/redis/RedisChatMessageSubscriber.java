package com.planwith.planwith_fo_chat.adapter.out.redis;

import java.nio.charset.StandardCharsets;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_chat.application.chat.ChatRealtimePayload;
import com.planwith.planwith_fo_chat.application.port.out.ChatRealtimeFanoutPort;

@Component
@Profile("!test")
@ConditionalOnProperty(name = "app.redis.enabled", havingValue = "true")
public class RedisChatMessageSubscriber implements MessageListener {

	private final ObjectMapper objectMapper;
	private final ChatRealtimeFanoutPort chatRealtimeFanoutPort;

	public RedisChatMessageSubscriber(
			ObjectMapper objectMapper,
			ChatRealtimeFanoutPort chatRealtimeFanoutPort
	) {
		this.objectMapper = objectMapper;
		this.chatRealtimeFanoutPort = chatRealtimeFanoutPort;
	}

	@Override
	public void onMessage(Message message, byte[] pattern) {
		try {
			ChatRealtimePayload payload = objectMapper.readValue(
					new String(message.getBody(), StandardCharsets.UTF_8),
					ChatRealtimePayload.class
			);
			chatRealtimeFanoutPort.deliver(payload);
		}
		catch (Exception exception) {
			throw new IllegalStateException("Failed to fan out chat message.", exception);
		}
	}
}
