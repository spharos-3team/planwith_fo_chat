package com.planwith.planwith_fo_chat.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.redis")
public record ChatRedisProperties(
		boolean enabled,
		String host,
		int port,
		String channelPrefix,
		Duration presenceTtl
) {
	public String channel(String chatRoomUuid) {
		return channelPrefix() + chatRoomUuid;
	}
}
