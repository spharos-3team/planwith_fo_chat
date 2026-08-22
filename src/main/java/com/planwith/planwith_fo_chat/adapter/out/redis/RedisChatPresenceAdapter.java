package com.planwith.planwith_fo_chat.adapter.out.redis;

import java.time.Duration;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_chat.application.port.out.ChatPresencePort;
import com.planwith.planwith_fo_chat.config.ChatRedisProperties;

@Component
@Profile("!test")
@ConditionalOnProperty(name = "app.redis.enabled", havingValue = "true")
public class RedisChatPresenceAdapter implements ChatPresencePort {

	private static final String KEY_PREFIX = "chat:online:";

	private final StringRedisTemplate stringRedisTemplate;
	private final ChatRedisProperties properties;

	public RedisChatPresenceAdapter(
			StringRedisTemplate stringRedisTemplate,
			ChatRedisProperties properties
	) {
		this.stringRedisTemplate = stringRedisTemplate;
		this.properties = properties;
	}

	@Override
	public void markOnline(UUID memberUuid) {
		Duration ttl = properties.presenceTtl() == null ? Duration.ofMinutes(5) : properties.presenceTtl();
		stringRedisTemplate.opsForValue().set(KEY_PREFIX + memberUuid, "1", ttl);
	}
}
