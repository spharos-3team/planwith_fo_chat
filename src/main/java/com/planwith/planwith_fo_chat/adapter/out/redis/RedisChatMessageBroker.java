package com.planwith.planwith_fo_chat.adapter.out.redis;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_chat.application.chat.ChatRealtimePayload;
import com.planwith.planwith_fo_chat.application.port.out.ChatMessageBrokerPort;
import com.planwith.planwith_fo_chat.config.ChatRedisProperties;

@Component
@Profile("!test")
@ConditionalOnProperty(name = "app.redis.enabled", havingValue = "true")
public class RedisChatMessageBroker implements ChatMessageBrokerPort {

	private final StringRedisTemplate stringRedisTemplate;
	private final ObjectMapper objectMapper;
	private final ChatRedisProperties properties;

	public RedisChatMessageBroker(
			StringRedisTemplate stringRedisTemplate,
			ObjectMapper objectMapper,
			ChatRedisProperties properties
	) {
		this.stringRedisTemplate = stringRedisTemplate;
		this.objectMapper = objectMapper;
		this.properties = properties;
	}

	@Override
	public void publish(ChatRealtimePayload payload) {
		try {
			stringRedisTemplate.convertAndSend(
					properties.channel(payload.chatRoomUuid().toString()),
					objectMapper.writeValueAsString(payload)
			);
		}
		catch (JsonProcessingException exception) {
			throw new IllegalStateException("Failed to publish chat message.", exception);
		}
	}
}
