package com.planwith.planwith_fo_chat.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka")
public record ChatKafkaProperties(
		boolean enabled,
		boolean consumerEnabled,
		String createdTopic,
		String completedTopic,
		String disbandedTopic,
		String participationChangedTopic,
		String messageCreatedTopic
) {
}
